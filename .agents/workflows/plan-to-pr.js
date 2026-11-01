export const meta = {
  name: 'plan-to-pr',
  description: 'Implement an approved plan with TDD, review it with a separate reviewer, verify, and open a Draft PR',
  whenToUse: 'Called by the issue-to-pr skill after the plan is approved. Never needs user input.',
  phases: [
    { title: 'Implement', detail: 'TDD implementation and self-test' },
    { title: 'Review', detail: 'Reviewer finds issues, implementer fixes or rebuts, bounded rounds' },
    { title: 'Finish', detail: 'Final verification and Draft PR' },
  ],
}

// Input: `args` is the JSON object built by the issue-to-pr skill (see its Step 7-3).
//   issue, rounds, createPr, reviewerModel ('opus' | 'sonnet' | 'inherit'), testStrategy ('unit' | 'unit+ui'),
//   startCommit, baseBranch, remoteBranch, config {...}, dryRun?, dryRunFindings?
// Output: { status, stage, reason, prUrl, headCommit, unresolved, suggestions, uiQa, manualUiCheckRequired, calls }

// ---------- Input validation ----------

const REQUIRED_CONFIG = [
  'specDir', 'testCommand', 'fullTestCommand', 'tddSkillPath', 'reviewSkillPath', 'verifySkillPath',
  'prSkillPath', 'commitRulesPath', 'branchRulesPath', 'specRulesPath',
]

function validateArgs(a) {
  if (!a || typeof a !== 'object') return 'args must be a JSON object'
  if (!a.issue) return 'args.issue is required'
  if (!a.startCommit) return 'args.startCommit is required'
  if (!Number.isInteger(a.rounds) || a.rounds < 0) return 'args.rounds must be an integer >= 0'
  if (!a.config || typeof a.config !== 'object') return 'args.config is required'
  for (const key of REQUIRED_CONFIG) {
    if (!a.config[key]) return `args.config.${key} is required`
  }
  if (a.testStrategy === 'unit+ui' && !a.config.uiQaTool) {
    return 'args.config.uiQaTool is required when testStrategy is unit+ui'
  }
  if (a.createPr && (!a.baseBranch || !a.remoteBranch)) {
    return 'args.baseBranch and args.remoteBranch are required when createPr is true (the workflow cannot ask)'
  }
  return null
}

let calls = 0

function aborted(stage, reason, extra) {
  return Object.assign({
    status: 'aborted', stage, reason, prUrl: null, headCommit: null,
    unresolved: [], suggestions: [], uiQa: null, manualUiCheckRequired: false, calls,
  }, extra || {})
}

const argError = validateArgs(args)
if (argError) {
  return aborted('Input', argError)
}

const cfg = args.config
const issue = args.issue
const dry = args.dryRun === true
const rounds = args.rounds
const createPr = args.createPr === true
const uiQaPlanned = args.testStrategy === 'unit+ui'
const reviewerModel = args.reviewerModel === 'opus' || args.reviewerModel === 'sonnet' ? args.reviewerModel : undefined
const specDir = cfg.specDir

// In dry-run mode no file is read or changed: each agent only echoes a canned value, which still exercises
// args delivery, the model option, schemas and loop control.
async function run(prompt, opts, dryValue) {
  calls++
  if (dry) {
    const echo = 'Dry run. Do not read or modify any file and do not run any command. '
      + 'Call StructuredOutput with exactly this value: ' + JSON.stringify(dryValue)
    return agent(echo, Object.assign({}, opts, { effort: 'low' }))
  }
  return agent(prompt, opts)
}

// ---------- Schemas ----------

const COMMAND_RESULT = {
  type: 'object',
  properties: { command: { type: 'string' }, result: { type: 'string' } },
  required: ['command', 'result'],
}

const IMPL_SCHEMA = {
  type: 'object',
  properties: {
    status: { type: 'string', enum: ['done', 'failed'] },
    summary: { type: 'string' },
    changedModules: { type: 'array', items: { type: 'string' } },
    testsRun: { type: 'array', items: COMMAND_RESULT },
    uiQa: { type: 'string', enum: ['not-planned', 'passed', 'skipped-no-device', 'failed'] },
    headCommit: { type: 'string' },
    failureReason: { type: 'string' },
  },
  required: ['status', 'summary', 'changedModules', 'testsRun', 'uiQa', 'headCommit'],
}

const FINDING = {
  type: 'object',
  properties: {
    id: { type: 'string' },
    file: { type: 'string' },
    line: { type: 'integer' },
    severity: { type: 'string', enum: ['blocking', 'suggestion'] },
    summary: { type: 'string' },
  },
  required: ['id', 'file', 'severity', 'summary'],
}

const REVIEW_SCHEMA = {
  type: 'object',
  properties: {
    findings: { type: 'array', items: FINDING },
    failureReason: { type: 'string' },
  },
  required: ['findings'],
}

const FIX_SCHEMA = {
  type: 'object',
  properties: {
    status: { type: 'string', enum: ['ok', 'failed'] },
    responses: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string' },
          action: { type: 'string', enum: ['fixed', 'rebutted'] },
          note: { type: 'string' },
        },
        required: ['id', 'action', 'note'],
      },
    },
    testsRun: { type: 'array', items: COMMAND_RESULT },
    headCommit: { type: 'string' },
    failureReason: { type: 'string' },
  },
  required: ['status', 'responses', 'testsRun', 'headCommit'],
}

const FINISH_SCHEMA = {
  type: 'object',
  properties: {
    status: { type: 'string', enum: ['pr_created', 'committed', 'failed'] },
    prUrl: { type: 'string' },
    verification: { type: 'array', items: COMMAND_RESULT },
    headCommit: { type: 'string' },
    failureReason: { type: 'string' },
  },
  required: ['status', 'verification', 'headCommit'],
}

// ---------- Prompts ----------

const NO_INPUT = 'No user input is available in this run. If something is ambiguous or blocked, do not guess: '
  + 'stop and report it in failureReason.'

const CLEAN_TREE = `Commit following ${cfg.commitRulesPath} and do not push. When you finish, \`git status --porcelain\` `
  + 'must be empty: commit every file you want to keep and delete temporary files you created.'

function docsToRead() {
  return `Read first: ${specDir}/requirements.md, ${specDir}/plan.md, ${specDir}/mockup/ (only if it exists), `
    + `${cfg.specRulesPath}, ${cfg.branchRulesPath}, ${cfg.commitRulesPath}, and AGENTS.md. Open the files yourself.`
}

function implementPrompt() {
  const uiQa = uiQaPlanned
    ? `UI QA is planned (${cfg.uiQaTool}): load its tools with ToolSearch and check whether a device or emulator is `
      + 'connected. If none is, skip UI QA and return uiQa "skipped-no-device". Otherwise author and run a flow for '
      + 'the changed screens; if it fails, fix once and rerun, then return uiQa "passed" or "failed". '
    : 'UI QA is not planned: return uiQa "not-planned". '
  return [
    `You are the implementer for GitHub issue #${issue}. ${NO_INPUT}`,
    docsToRead(),
    `Follow ${cfg.tddSkillPath} and implement the tasks of plan.md in order, test first. Do not open a PR.`,
    CLEAN_TREE,
    `Self-test: run \`${cfg.testCommand}\` only for the modules you changed; run \`${cfg.fullTestCommand}\` instead `
      + 'when the change is cross-cutting (for example build scripts).',
    uiQa,
    'If a self-test fails, fix it once and rerun. If it still fails, return status "failed" with the failing output '
      + 'summarized in failureReason. Record only commands you actually ran.',
    'Return headCommit as the output of `git rev-parse HEAD`.',
  ].join('\n')
}

function reviewPrompt(fromCommit, previous, responses) {
  const delta = previous && previous.length
    ? `This is a re-review. Previous findings: ${JSON.stringify(previous)}. The implementer responded: `
      + `${JSON.stringify(responses)}. Review only the changes in \`git diff ${fromCommit}..HEAD\` plus the previous `
      + 'findings. Report only findings that are still open (keep their ids) or newly introduced.'
    : `Review the whole change: \`git diff ${fromCommit}..HEAD\`.`
  return [
    `You are an independent code reviewer for GitHub issue #${issue}; you did not write this code. ${NO_INPUT}`,
    'Do not modify any file.',
    docsToRead(),
    `Apply only the review criteria and reviewer guidelines of ${cfg.reviewSkillPath} (logic, edge cases, project `
      + 'standards, regressions, technical focus). Ignore its mechanics: do not launch another reviewer subagent, '
      + 'do not compute SHAs yourself, and do not post comments with `gh` or any other tool. Look only at the diff '
      + 'range given here and return the result only through the schema.',
    delta,
    'Severity: "blocking" = correctness bug, requirement or plan violation, missing or failing test, project rule '
      + 'violation. "suggestion" = everything else. Give each finding a stable id (F1, F2, ...); a new finding in a '
      + 're-review takes the next number after the highest id used so far. '
      + 'Return an empty findings list when nothing is wrong. If you cannot review (for example the requirements are '
      + 'ambiguous), return an empty findings list and explain in failureReason.',
  ].join('\n')
}

function fixPrompt(open) {
  return [
    `You are the implementer for GitHub issue #${issue}, handling review feedback. ${NO_INPUT}`,
    docsToRead(),
    `Findings: ${JSON.stringify(open)}`,
    'For each finding either fix it (with a test when it changes behavior) or rebut it with a concrete reason. '
      + 'Address blocking findings first; fix suggestions only when trivial.',
    CLEAN_TREE,
    `Rerun \`${cfg.testCommand}\` for the affected modules. `
      + 'If a test fails, fix it once and rerun; if it still fails, return status "failed".',
    'Return headCommit as the output of `git rev-parse HEAD`.',
  ].join('\n')
}

function finishPrompt(state) {
  const pr = createPr
    ? [
      `Then follow ${cfg.prSkillPath} to open the Draft PR. Decisions are already made, do not ask: base branch is `
        + `"${args.baseBranch}", remote branch is "${args.remoteBranch}". Select the recommended labels without `
        + 'confirmation. Do not rewrite history; if a commit-rule violation or any other blocker appears, return '
        + 'status "failed" with the reason instead of guessing.',
      state.unresolved.length || state.suggestions.length
        ? 'Add a section "Unresolved review points" to the PR body listing: '
          + JSON.stringify({ blocking: state.unresolved, suggestions: state.suggestions })
        : 'No unresolved review points exist; do not add such a section.',
      state.manualUiCheckRequired
        ? 'UI QA was skipped because no device was available: write "manual UI check required" in the PR '
          + 'Verification section.'
        : 'Do not mention a manual UI check.',
      'Return status "pr_created" with prUrl.',
    ].join('\n')
    : 'Do not push and do not open a PR (the user chose not to). Return status "committed".'
  return [
    `You are the finisher for GitHub issue #${issue}. ${NO_INPUT}`,
    `First follow ${cfg.verifySkillPath}: run \`${cfg.testCommand}\` for the modules changed since `
      + `${args.startCommit} (\`git diff --name-only ${args.startCommit}..HEAD\`). If anything fails, fix it once and `
      + 'rerun; if it still fails return status "failed". Record only commands you actually ran.',
    CLEAN_TREE,
    'Return headCommit as the output of `git rev-parse HEAD` after any fix.',
    pr,
  ].join('\n')
}

// ---------- Dry-run canned values ----------

const DRY_FINDINGS = Array.isArray(args.dryRunFindings)
  ? args.dryRunFindings
  : [[{ id: 'F1', file: 'dry/Run.kt', line: 1, severity: 'blocking', summary: 'dry run finding' }], []]
let dryReviewIndex = 0

// ---------- Phase 1: implement ----------

phase('Implement')
const impl = await run(implementPrompt(), { label: 'implement', phase: 'Implement', schema: IMPL_SCHEMA }, {
  status: 'done', summary: 'dry run', changedModules: [], testsRun: [],
  uiQa: uiQaPlanned ? 'skipped-no-device' : 'not-planned', headCommit: 'dry-impl',
})
if (!impl) return aborted('Implement', 'The implementer agent returned nothing')
if (impl.status !== 'done') {
  return aborted('Implement', impl.failureReason || 'Implementation failed', { headCommit: impl.headCommit || null })
}

let head = impl.headCommit
const manualUiCheckRequired = impl.uiQa === 'skipped-no-device'
if (impl.uiQa === 'failed') {
  return aborted('Implement', 'UI QA failed after one retry', { headCommit: head, uiQa: impl.uiQa })
}

// ---------- Phase 2: review loop ----------

phase('Review')
let reviewedAt = args.startCommit
let previous = null
let responses = []
let round = 0
let review = await run(reviewPrompt(reviewedAt, null, []), {
  label: 'review-1', phase: 'Review', schema: REVIEW_SCHEMA, model: reviewerModel,
}, { findings: DRY_FINDINGS[dryReviewIndex++] || [] })
if (!review) return aborted('Review', 'The reviewer agent returned nothing', { headCommit: head })
if (review.failureReason) return aborted('Review', review.failureReason, { headCommit: head })
let open = review.findings

function blocking(list) { return list.filter(f => f.severity === 'blocking') }

while (blocking(open).length > 0 && round < rounds) {
  round++
  log(`Review round ${round}/${rounds}: ${blocking(open).length} blocking finding(s)`)
  const fix = await run(fixPrompt(open), { label: `fix-${round}`, phase: 'Review', schema: FIX_SCHEMA }, {
    status: 'ok', responses: open.map(f => ({ id: f.id, action: 'fixed', note: 'dry run' })),
    testsRun: [], headCommit: `dry-fix-${round}`,
  })
  if (!fix) return aborted('Review', 'The implementer returned nothing while handling feedback', { headCommit: head })
  if (fix.status !== 'ok') return aborted('Review', fix.failureReason || 'Fixing review findings failed', { headCommit: head })
  responses = fix.responses
  previous = open
  reviewedAt = head
  head = fix.headCommit
  review = await run(reviewPrompt(reviewedAt, previous, responses), {
    label: `review-${round + 1}`, phase: 'Review', schema: REVIEW_SCHEMA, model: reviewerModel,
  }, { findings: DRY_FINDINGS[dryReviewIndex++] || [] })
  if (!review) return aborted('Review', 'The reviewer agent returned nothing', { headCommit: head })
  if (review.failureReason) return aborted('Review', review.failureReason, { headCommit: head })
  open = review.findings
}

const unresolved = blocking(open)
const suggestions = open.filter(f => f.severity !== 'blocking')
if (unresolved.length) {
  log(`Discussion cap reached with ${unresolved.length} unresolved blocking finding(s); recorded for the PR body`)
}

// ---------- Phase 3: finish ----------

phase('Finish')
const state = { unresolved, suggestions, manualUiCheckRequired }
const finish = await run(finishPrompt(state), { label: 'finish', phase: 'Finish', schema: FINISH_SCHEMA }, {
  status: createPr ? 'pr_created' : 'committed',
  prUrl: createPr ? 'https://example.invalid/dry-run' : '',
  verification: [],
  headCommit: 'dry-finish',
})
if (!finish) return aborted('Finish', 'The finisher agent returned nothing', { headCommit: head })
head = finish.headCommit || head
if (finish.status === 'failed') {
  return aborted('Finish', finish.failureReason || 'Final verification or PR creation failed', { headCommit: head })
}
if (createPr && (finish.status !== 'pr_created' || !finish.prUrl)) {
  return aborted('Finish', `Expected a created Draft PR but got status "${finish.status}"`, { headCommit: head })
}
if (!createPr && finish.status !== 'committed') {
  return aborted('Finish', `A PR was not requested but the finisher returned status "${finish.status}"`, { headCommit: head })
}

return {
  status: finish.status,
  stage: 'Finish',
  reason: null,
  prUrl: finish.prUrl || null,
  headCommit: head,
  unresolved,
  suggestions,
  uiQa: impl.uiQa,
  manualUiCheckRequired,
  calls,
}
