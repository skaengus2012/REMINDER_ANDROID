---
name: create-mockup
description: >-
  Creates a self-contained HTML mockup for a GitHub issue's UI change and saves it as
  `docs/spec/issue<number>/mockup/<screen>.html`. Reads the issue's requirements and the `core:designsystem`
  tokens (colors, typography, dimens, icons) so the mockup matches the app. Use this skill whenever the user asks
  to design a screen, draw a UI mockup or prototype for an issue, or says things like "시안 만들어줘" /
  "목업 만들어줘" / "디자인 해줘", even if they do not mention 'create-mockup'.
---

# Create Mockup Skill

## Overview

Produces one HTML file per screen under `docs/spec/issue<number>/mockup/`. The mockup is a **visual reference** for
implementation, not a specification: the code remains the source of truth. The file layout and the rule to read
`mockup/` before implementing are defined in `docs/guidelines/spec-docs.md`.

Design values are **never copied into this skill**. Read them from `core:designsystem` on every run, so the mockup
follows the design system even after it changes.

---

## Step 1. Gather Requirements

The argument is an issue number (e.g. `/create-mockup 601`). If it is missing, take it from the branch name
(`feature/issue<N>`), otherwise ask the user.

1. Read `docs/spec/issue<N>/requirements.md` if it exists. If it does not, read the issue with
   `gh issue view <N> --json title,body,comments` and tell the user the requirements document is missing.
2. Read `docs/spec/issue<N>/plan.md` if it exists.
3. List `docs/spec/issue<N>/mockup/`. If a file for the screen already exists, **edit it in place** instead of
   creating a second one.

Decide which screens are needed. If the requirements do not make it clear which screen (or which state of a screen)
to draw, ask the user **one** question before drawing.

## Step 2. Read the Design System

All paths are under `core/designsystem/src/main/`.

| What | Read | Notes |
|------|------|-------|
| Colors | `kotlin/.../compose/theme/Color.kt`, `ColorScheme.kt` | Compose is the source of truth. It has tokens the XML lacks (e.g. `Point4`, `Bg2Layer`, dialog and tag colors). `Color.kt` has light and dark sets. |
| Typography | `kotlin/.../compose/theme/Typography.kt` | `sp` -> `px`, `FontWeight.Normal/Medium/Bold` -> `400/500/700`. See "Typography" below for what CSS cannot reproduce. |
| Dimens | `res/values/dimens.xml` | The only named dimension values (e.g. `horizontal_padding_medium`, `icon_button_size`). `Dimens.kt` holds only a Modifier helper, no numbers. Most other spacing is hard-coded in the components, so read the component code for it. |
| Icons | `kotlin/.../compose/icon/PlaneatIcons.kt`, `res/drawable/`, `kotlin/.../compose/resource/imagevector/` | Only needed to name icons or when the user asks for a real shape. See "Icons" below. |
| Existing UI | `core/component/*` (e.g. `schedulelist`, `tag`, `toolbar`, `usermessage`) and the screen code under `feature/` | Reuse the look and the actual spacing of existing components instead of inventing new ones. |

Only colors and typography are true design-system tokens. Spacing is mostly per-component, so take it from the
existing component code, not from a token file.

If a token does not exist for something the design needs, say so in the reply instead of silently inventing a
color. A mockup may propose a new token, but it must be marked as new (see Step 3).

## Step 3. Write the HTML

Save to `docs/spec/issue<N>/mockup/<screen>.html` (create the folder if needed). `<screen>` is a short
kebab-case name such as `home-new-plan-button`.

**Self-contained**
- One file. No external CDN, font, script or remote image. It must open offline by double-clicking it.
  The only exception is a repo-relative image reference (see "Icons"), which shows only when opened inside the repository.
- CSS in a `<style>` block, no JavaScript unless an interaction needs it.
- Do not embed `res/font/*.ttf`. `Typography.kt` sets no `fontFamily`, so text uses the system default; use
  `font-family: sans-serif`. `sb_aggro_m.ttf` is an extra font used only where `PlaneatExtraFont` is referenced;
  if the design uses it, say so in the header comment instead of embedding it.

**Typography**
- `Typography.kt` disables font padding and sets no `lineHeight`, so CSS cannot reproduce the exact row heights.
  Give rows and buttons a fixed height taken from the component code or `dimens.xml` instead of letting the text
  decide, and note in the header comment that line heights are approximate.

**Tokens**
- Declare every color as a CSS variable on `:root`, named after the `PlaneatColorScheme` property. Split the
  camelCase name into hyphenated words and keep each digit group as its own word: `bg1` -> `--bg-1`,
  `bg1Layer` -> `--bg-1-layer`, `point1Sub` -> `--point-1-sub`, `bgPlaceHolder1` -> `--bg-place-holder-1`,
  `content2Hint` -> `--content-2-hint`. Do not hard-code hex values in component styles.
- Provide both themes: light values on `:root`, dark values under `@media (prefers-color-scheme: dark)`.
- Mark anything that is not in the design system with a `/* NEW */` comment next to its definition.

**Colors**
- The token set is the properties of `PlaneatColorScheme`. `LightPlaneatColorScheme` and `DarkPlaneatColorScheme`
  in `ColorScheme.kt` say which constant in `Color.kt` fills each property in each theme. Follow that wiring; do not
  guess a token from its constant name.
- Convert values when writing the CSS:
  - `Color(0xAARRGGBB)` -> `#RRGGBB`. When `AA` is not `FF`, use `rgba(r, g, b, a)` with `a = AA / 255`.
    Never paste `AARRGGBB` as a CSS hex: CSS 8-digit hex is `#RRGGBBAA`, so the alpha would land in the wrong place.
  - `Color(0x00000000)` (transparent) -> `transparent`.
  - `X.copy(alpha = 0.6f)` -> `rgba(...)` of the resolved `X`.
  - A constant defined from another constant (e.g. `BgLine1Light = Gray400`) -> resolve it to the final value.
  - `Color.White` / `Color.Black` -> `#FFFFFF` / `#000000`.
- Properties with a default value in `PlaneatColorScheme` (`point1`..`point4`, `red1`, `white`, `black`, ...) do not
  change with the theme. Define them once on `:root`, outside the media query.
- Interaction tokens: ripple colors (`bgRipple`, `bg1LayerRipple`, `bgTagRipple`) are translucent overlays drawn on
  top of the element, so show them as an overlay on `:active`, e.g.
  `background-image: linear-gradient(var(--bg-ripple), var(--bg-ripple))`, not as a replacement `background-color`.
  `bgTextSelection` -> `::selection`. `contentTextSelection` (the selection handle color) has no CSS equivalent;
  leave it out.
- Example of the expected shape:

```css
:root {
  --point-1: #1CCF98;           /* theme independent */
  --bg-1: #F2F2F6;             /* light */
  --content-1: #393939;
}
@media (prefers-color-scheme: dark) {
  :root {
    --bg-1: #000000;           /* dark */
    --content-1: #FFFFFF;
  }
}
```

**Layout**
- Draw a phone-sized frame centered on the page (around 360px wide). Use `horizontal_padding_medium` from `dimens.xml` for the horizontal padding.
- Show the states the requirements mention (e.g. empty, selected, error), each labeled, rather than only the happy path.
- Add a header comment with the issue number, the screen name, and which requirement it covers.

**Icons**

Default to a **placeholder**: the layout, size, position and color are what a mockup is checked for, and
converting vector code to SVG is error-prone. Convert a real shape only when the user asks for it or the change is
about the icon shape itself.

- Placeholder: a dashed box of the icon's real size with the icon name as its `title` and a short label, colored
  with a token variable, e.g. `<span class="icon-ph" style="width:24px;height:24px" title="ic_back">←</span>`.
  Take the size from the icon's `android:width`/`android:height` (XML) or `defaultWidth`/`defaultHeight`
  (`ImageVector`). Use `PlaneatIcons.kt` and the component code to find which icon belongs where.
- An icon that does not exist yet: the same placeholder, marked `/* NEW */`.
- Raster files (`.png`, `.webp`, including a `layer-list` that wraps one, like `ic_trash_24.xml`): use the
  placeholder. If the image's look matters, reference it by a repo-relative path (e.g.
  `../../../../core/designsystem/src/main/res/drawable-xxhdpi/<name>.webp`); it shows only when the file is opened
  inside the repository and breaks if the image moves, so say that in a comment. Inline a `data:` URI only for a
  small image, never a large one.
- `android:autoMirrored="true"` (e.g. `ic_new_plan`): a placeholder needs no flip, but note "mirrors in RTL" in a
  comment. For a converted icon, flip it with `[dir="rtl"] { transform: scaleX(-1) }`.

When you do convert a vector to inline SVG:
- Size: `viewBox` from `viewportWidth`/`viewportHeight`, `width`/`height` from the `dp` size.
- Android vector XML: `android:pathData` -> `<path d="...">`; `fillType="evenOdd"` -> `fill-rule="evenodd"` (SVG
  defaults to `nonzero`, which fills holes); `strokeColor`/`strokeWidth` -> `stroke`/`stroke-width`.
- Compose `ImageVector`: `moveTo` -> `M`, `lineTo` -> `L`, `horizontalLineTo` -> `H`, `verticalLineTo` -> `V`,
  `curveTo` -> `C`, `quadTo` -> `Q`, `reflectiveCurveTo` -> `S`, `reflectiveQuadTo` -> `T`, `arcTo` -> `A`,
  `close` -> `Z`; the `...Relative` variants are the lowercase letters. `arcTo` takes
  `(rx, ry, theta, isMoreThanHalf, isPositiveArc, x, y)`, the same order as SVG `A rx ry rotation large-arc sweep x y`
  with the booleans written as `1`/`0`. Drop the `f` suffix from numbers.
- `ImageVector` paths can be **stroked**: `stroke` -> `stroke`, `strokeLineWidth` -> `stroke-width`,
  `strokeLineCap`/`strokeLineJoin` -> `stroke-linecap`/`stroke-linejoin`, `strokeLineMiter` -> `stroke-miterlimit`.
  A path with an alpha-0 fill (`Color(0x00000000)`) must use `fill="none"`. `IcHomeCategoryAll` and
  `IcHomeCategoryTimetable` are drawn this way; `fill="currentColor"` would erase or fill them.
- Colors: an `ImageVector` carries its own colors and is often drawn without a tint, so keep its colors (as
  token variables). An XML vector is often tinted by the code that uses it, so check the usage (`Icon(tint = ...)`
  vs `Image(...)`) before choosing the color.
- Not covered: `<group>`, `<clip-path>`, gradients, `fillAlpha`. Fall back to a placeholder for those.

## Step 4. Review with the User

1. Open the file so the user can look at it: `open <path>` on macOS.
2. Summarize in the reply: the file path, the states drawn, and anything marked `NEW` or placeholder.
3. Apply feedback by editing the **same file**. Do not create `v2` copies; git history keeps the earlier versions.

## Step 5. Finish

- Do not commit or push on your own. When the user asks, follow `docs/guidelines/commit-rules.md`
  (use the `Docs` type for a mockup-only change).
- A mockup the user dropped is deleted, not left behind: the folder should hold only adopted mockups.
- Do not edit production code. Implementation is a separate step, and the `mockup/` folder is read by whoever
  implements the issue.

---

## Common Mistakes

1. **Copying token values into the mockup by memory.** Always read the files in Step 2; the XML and Kotlin
   definitions can differ, and Compose wins.
2. **Hard-coding hex colors** in component styles instead of CSS variables, which breaks the dark theme.
3. **Using an external CDN, web font or remote image**, so the file no longer opens offline.
4. **Converting every icon to SVG by default.** Use placeholders unless the shape matters; a wrong conversion
   (stroked paths filled in, `evenOdd` ignored) is worse than a labeled box.
5. **Treating spacing as a token.** Only colors and typography are design-system tokens; read spacing from the
   component code.
6. **Drawing only the happy path** when the requirements describe other states.
7. **Creating a new file for every revision** instead of editing the existing one in place.
8. **Forgetting RTL** for icons with `autoMirrored="true"`.
