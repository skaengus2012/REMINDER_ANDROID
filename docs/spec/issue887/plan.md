# 계획: [Task] Add .claude/skills symlink to .agents/skills for Claude Code (#887)

- 이슈: https://github.com/skaengus2012/REMINDER_ANDROID/issues/887
- 요구사항: docs/spec/issue887/requirements.md
- 생성일: 2026-11-02
- 최종 수정일: 2026-11-02

## 배경/목표
- Claude Code는 프로젝트 스킬을 `.claude/skills/`에서만 탐색하므로, `.agents/skills/`의 스킬(`create-pr`, `create-mockup` 등)을 쓸 수 없다.
- `.agents/skills`를 단일 원본으로 유지한 채 Claude Code에서도 스킬을 사용할 수 있게 한다.

## 요구사항
- FR-1: `.claude/skills`를 `../.agents/skills`를 가리키는 심볼릭 링크로 추가
- FR-2: `.agents/skills`를 단일 원본으로 유지 (파일 복제 금지)
- FR-3: 심볼릭 링크를 커밋하여 모든 clone에서 사용 가능

## 아키텍처/설계
- `.claude/` 디렉터리를 만들고 그 안에 상대 경로 심볼릭 링크 `skills -> ../.agents/skills`를 둔다. 상대 경로이므로 worktree/clone 위치와 무관하게 동작한다.
- git은 심볼릭 링크를 mode `120000` 블롭으로 저장하므로 별도 설정 없이 커밋된다.
- 범위 제외: `AGENTS.md` 문서화(사용자 결정), 스킬 내용 변경, Windows 심볼릭 링크 지원 대응(이슈에 언급 없음).

## 작업
1. `.claude/skills` 심볼릭 링크를 생성한다.
   - 명령: `mkdir -p .claude && ln -s ../.agents/skills .claude/skills`
   - **검증**: `readlink .claude/skills`의 출력이 `../.agents/skills`이고, `ls .claude/skills`에 `create-pr`, `create-mockup`, `create-jacoco-report-and-show`, `git-worktree-creator`가 보인다.
2. 링크가 git에 심볼릭 링크로 추적되는지 확인한다.
   - **검증**: `git add .claude/skills && git ls-files -s .claude/skills`의 mode가 `120000`이다. 또한 `git check-ignore .claude/skills`가 아무것도 출력하지 않는다(무시되지 않음).
3. 스킬 파일이 복제되지 않았는지 확인한다.
   - **검증**: `git status --short`에 `.claude/skills` 한 항목만 추가되고 `.agents/skills/` 하위 변경이 없다. `find .claude -type f -not -type l`이 비어 있다(링크 외 실제 파일 없음).
4. 커밋 규칙(`docs/guidelines/commit-rules.md`)을 확인한 뒤 커밋한다.
   - **검증**: `git show --stat HEAD`에 `.claude/skills`(심볼릭 링크)만 포함된다. 요구사항/계획 문서는 별도 또는 동일 커밋 여부를 규칙에 맞춘다.

## 검증
- 새로 clone한 상태를 가정해 `git ls-tree HEAD .claude/`에서 `120000 blob ... skills`가 보이면 FR-3 충족.
- Claude Code를 이 저장소에서 재시작해 `/` 목록에 `create-pr`, `create-mockup` 등이 나타나면 인수 조건 충족.

---

## 변경 이력
| 날짜 | 변경 내용 | 출처 |
|------|-----------|------|
| 2026-11-02 | 최초 작성 (AGENTS.md 문서화는 범위 제외) | requirements.md, 사용자 답변 |
