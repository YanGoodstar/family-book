# 朵攒攒品牌落地 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将已批准的 `朵攒攒` 品牌方向落到小程序头像资源、品牌常量、个人中心/设置页、昵称完善页以及首页/梦想目标关键文案上。

**Architecture:** 保持后端接口和登录流程不变，只在小程序前端做品牌落地。先新增一个共享 `brand` 元数据模块，统一品牌名称、slogan、简介和本地图标路径，再创建新的 `duozanzan-avatar` 资源，并把与品牌直接相关的页面改为消费共享常量，避免后续再次出现“家庭记账 / 梦想储蓄 / 朵攒攒”三套说法混杂。

**Tech Stack:** WeChat Mini Program JS/WXML/WXSS, existing util/helpers, SVG/PNG brand assets, PowerShell + `System.Drawing`, Node built-in `assert`

---

## Execution Notes

- 设计依据固定为 [`2026-03-29-duozanzan-brand-design.md`](/E:/family-book/docs/superpowers/specs/2026-03-29-duozanzan-brand-design.md)，执行中不要再引入新的品牌方向分支。
- 当前阶段优先开发效率，测试保持轻量：只补充品牌元数据、资源存在性和关键页面文案的 Node smoke tests，不要求在本计划里补完整自动化 UI 测试。
- 本计划只覆盖仓库里的前端品牌资源和页面文案，不覆盖微信公众平台后台里“正式小程序名称/头像”的手工配置。
- 已存在的 `family-book-avatar.*` 先保留，不覆盖；新品牌资源统一使用 `duozanzan-avatar.*`，等页面和后台都切完后再决定是否清理旧资源。

## File Structure

- Create: `miniprogram/utils/brand.js`
  统一导出 `朵攒攒` 的品牌名称、解释语、主 slogan、短 slogan、简介和本地图标路径。
- Create: `miniprogram/tests/brand-metadata.test.js`
  轻量校验品牌常量是否完整，防止后续页面再次手写发散文案。
- Create: `miniprogram/assets/images/brand/duozanzan-avatar.svg`
  最终 SVG 主稿，使用“几何花苞 + 隐藏元宝负形”。
- Create: `miniprogram/assets/images/brand/generate-duozanzan-avatar.ps1`
  在 Windows 本机重复生成 PNG 的脚本，避免以后每次微调都手工导出。
- Create: `miniprogram/assets/images/brand/duozanzan-avatar.png`
  小程序内展示和上传微信后台时优先使用的位图版本。
- Create: `miniprogram/tests/brand-assets.test.js`
  校验新 SVG/PNG 资源存在，SVG 包含预期标识，PNG 不为空文件。
- Create: `miniprogram/tests/brand-profile-pages.test.js`
  校验“我的 / 系统设置”页面已经改用共享品牌常量，并移除旧的品牌文案。
- Create: `miniprogram/tests/brand-copy-flow.test.js`
  校验昵称完善页、首页梦想卡片和梦想目标页面使用新的品牌语气。
- Modify: `miniprogram/pages/profile/index/index.js`
  引入共享品牌常量，给页脚和品牌说明区提供数据。
- Modify: `miniprogram/pages/profile/index/index.wxml`
  把页脚 `家庭记账` 文案替换为 `朵攒攒` 品牌信息，并补一个轻量品牌说明区。
- Modify: `miniprogram/pages/profile/index/index.wxss`
  为新增的品牌说明区和 logo 展示补样式。
- Modify: `miniprogram/pages/profile/settings/settings.js`
  引入共享品牌常量，给系统设置页 hero / 产品信息卡提供品牌数据。
- Modify: `miniprogram/pages/profile/settings/settings.wxml`
  用 `朵攒攒` 品牌说明替换当前泛化的“家庭记账”介绍文案，并展示新头像资源。
- Modify: `miniprogram/pages/profile/settings/settings.wxss`
  为品牌图和品牌说明卡补样式，不改已有登录/入口功能。
- Modify: `miniprogram/pages/profile/complete/index.js`
  引入品牌名称，避免昵称完善页继续使用纯功能化说明。
- Modify: `miniprogram/pages/profile/complete/index.wxml`
  将昵称完善页副标题和提示语改成 `朵攒攒` 的品牌语气。
- Modify: `miniprogram/pages/index/index.wxml`
  调整首页梦想目标卡片副标题和空状态文案，使其与品牌主句一致。
- Modify: `miniprogram/pages/dream/list/list.wxml`
  调整梦想目标列表页空状态和归档提示文案。
- Modify: `miniprogram/pages/dream/edit/edit.wxml`
  调整新建/编辑梦想目标页的引导文案。
- Modify: `miniprogram/utils/dreamGoalView.js`
  收敛首页梦想摘要和归档后提示文案，改成更温和的品牌表达。
- Modify: `miniprogram/tests/dream-goal-view.test.js`
  跟随新的摘要提示文案更新断言。

### Task 1: Add Shared Brand Metadata

**Files:**
- Create: `miniprogram/utils/brand.js`
- Create: `miniprogram/tests/brand-metadata.test.js`

- [ ] **Step 1: Write the failing metadata smoke test**

Create `miniprogram/tests/brand-metadata.test.js`:

```javascript
const assert = require('assert');
const {
  BRAND_NAME,
  BRAND_EXPLAINER,
  BRAND_SLOGAN,
  BRAND_SHORT_SLOGAN,
  BRAND_DESCRIPTION,
  BRAND_LOGO
} = require('../utils/brand');

assert.equal(BRAND_NAME, "\u6735\u6512\u6512");
assert.equal(
  BRAND_EXPLAINER,
  ["\u6735\u6512\u6512\uff0c\u5bd3\u610f\u628a\u4e00\u70b9\u70b9\u79ef\u7d2f\uff0c", "\u6162\u6162\u6512\u6210\u4f1a\u5f00\u82b1\u7684\u672a\u6765\u3002"].join("")
);
assert.equal(BRAND_SLOGAN, "\u628a\u6bcf\u4e00\u6b21\u5c0f\u79ef\u7d2f\uff0c\u6162\u6162\u6512\u6210\u4e00\u6735\u82b1\u3002");
assert.equal(BRAND_SHORT_SLOGAN, "\u4eca\u5929\u6512\u4e00\u70b9\uff0c\u672a\u6765\u5f00\u4e00\u6735\u3002");
assert.equal(
  BRAND_DESCRIPTION,
  ["\u6735\u6512\u6512\u662f\u4e00\u6b3e\u5e2e\u52a9\u4f60\u4e3a\u68a6\u60f3\u6162\u6162\u79ef\u7d2f\u7684\u5c0f\u7a0b\u5e8f\uff0c", "\u8bb0\u5f55\u6bcf\u4e00\u6b21\u5b58\u4e0b\u7684\u94b1\uff0c\u770b\u89c1\u76ee\u6807\u4e00\u70b9\u70b9\u5f00\u82b1\u3002"].join("")
);
assert.equal(BRAND_LOGO, "/assets/images/brand/duozanzan-avatar.png");

console.log("brand-metadata.test.js passed");
```

- [ ] **Step 2: Run the metadata test to verify it fails**

Run: `node miniprogram/tests/brand-metadata.test.js`

Expected: FAIL because `miniprogram/utils/brand.js` does not exist yet.

- [ ] **Step 3: Implement the shared brand module**

Create `miniprogram/utils/brand.js`:

```javascript
const BRAND_NAME = "\u6735\u6512\u6512";
const BRAND_EXPLAINER = ["\u6735\u6512\u6512\uff0c\u5bd3\u610f\u628a\u4e00\u70b9\u70b9\u79ef\u7d2f\uff0c", "\u6162\u6162\u6512\u6210\u4f1a\u5f00\u82b1\u7684\u672a\u6765\u3002"].join("");
const BRAND_SLOGAN = "\u628a\u6bcf\u4e00\u6b21\u5c0f\u79ef\u7d2f\uff0c\u6162\u6162\u6512\u6210\u4e00\u6735\u82b1\u3002";
const BRAND_SHORT_SLOGAN = "\u4eca\u5929\u6512\u4e00\u70b9\uff0c\u672a\u6765\u5f00\u4e00\u6735\u3002";
const BRAND_DESCRIPTION = ["\u6735\u6512\u6512\u662f\u4e00\u6b3e\u5e2e\u52a9\u4f60\u4e3a\u68a6\u60f3\u6162\u6162\u79ef\u7d2f\u7684\u5c0f\u7a0b\u5e8f\uff0c", "\u8bb0\u5f55\u6bcf\u4e00\u6b21\u5b58\u4e0b\u7684\u94b1\uff0c\u770b\u89c1\u76ee\u6807\u4e00\u70b9\u70b9\u5f00\u82b1\u3002"].join("");
const BRAND_LOGO = "/assets/images/brand/duozanzan-avatar.png";

module.exports = {
  BRAND_NAME,
  BRAND_EXPLAINER,
  BRAND_SLOGAN,
  BRAND_SHORT_SLOGAN,
  BRAND_DESCRIPTION,
  BRAND_LOGO
};
```

Keep this file narrow:

- only shared brand constants
- no page-specific text assembly
- no side effects
- all pages import from here instead of duplicating strings

- [ ] **Step 4: Run the metadata test again**

Run: `node miniprogram/tests/brand-metadata.test.js`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add miniprogram/utils/brand.js miniprogram/tests/brand-metadata.test.js
git commit -m "feat: add duozanzan brand metadata"
```

### Task 2: Create the Final Brand Avatar Asset

**Files:**
- Create: `miniprogram/assets/images/brand/duozanzan-avatar.svg`
- Create: `miniprogram/assets/images/brand/generate-duozanzan-avatar.ps1`
- Create: `miniprogram/assets/images/brand/duozanzan-avatar.png`
- Create: `miniprogram/tests/brand-assets.test.js`

- [ ] **Step 1: Write the failing asset smoke test**

Create `miniprogram/tests/brand-assets.test.js`:

```javascript
const fs = require('fs');
const path = require('path');
const assert = require('assert');

const brandDir = path.join(__dirname, '..', 'assets', 'images', 'brand');
const svgPath = path.join(brandDir, 'duozanzan-avatar.svg');
const pngPath = path.join(brandDir, 'duozanzan-avatar.png');

assert.ok(fs.existsSync(svgPath), "duozanzan SVG should exist");
assert.ok(fs.existsSync(pngPath), "duozanzan PNG should exist");

const svg = fs.readFileSync(svgPath, 'utf8');
assert.ok(svg.includes('id="bud"'), "SVG should contain the bud marker");
assert.ok(svg.includes('id="ingot-cut"'), "SVG should contain the ingot-cut marker");
assert.ok(svg.includes('linearGradient'), "SVG should contain a gradient");
assert.ok(fs.statSync(pngPath).size > 20000, "PNG should not be empty");

console.log("brand-assets.test.js passed");
```

- [ ] **Step 2: Run the asset test to verify it fails**

Run: `node miniprogram/tests/brand-assets.test.js`

Expected: FAIL because the new `duozanzan-avatar` files do not exist yet.

- [ ] **Step 3: Create the SVG and reproducible PNG export script**

Create `miniprogram/assets/images/brand/duozanzan-avatar.svg` with these constraints:

- canvas `1024 x 1024`
- rounded-square green gradient background
- one centered upright flower bud group with `id="bud"`
- a subtle ingot-shaped negative space group/path with `id="ingot-cut"`
- overall feel: simple geometry, no text, no house icon, no ledger icon

Suggested SVG scaffold:

```xml
<svg width="1024" height="1024" viewBox="0 0 1024 1024" fill="none" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bg" x1="160" y1="128" x2="864" y2="896" gradientUnits="userSpaceOnUse">
      <stop stop-color="#18C26E"/>
      <stop offset="1" stop-color="#0A8A4F"/>
    </linearGradient>
  </defs>

  <rect x="96" y="96" width="832" height="832" rx="224" fill="url(#bg)"/>

  <g id="bud">
    <!-- left petal -->
    <!-- right petal -->
    <!-- center bud -->
  </g>

  <path id="ingot-cut" d="..." fill="#0FA75B"/>
</svg>
```

Create `miniprogram/assets/images/brand/generate-duozanzan-avatar.ps1` that draws the same composition with `System.Drawing` and writes `duozanzan-avatar.png`. Keep it deterministic:

```powershell
Add-Type -AssemblyName System.Drawing

$size = 1024
$output = Join-Path $PSScriptRoot 'duozanzan-avatar.png'
$bitmap = New-Object System.Drawing.Bitmap $size, $size
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

# draw rounded background
# draw left/right/center petals
# draw ingot-shaped inner cutout
# save png to $output

$bitmap.Save($output, [System.Drawing.Imaging.ImageFormat]::Png)
```

Then run:

```bash
powershell -ExecutionPolicy Bypass -File miniprogram/assets/images/brand/generate-duozanzan-avatar.ps1
```

Rules:

- do not overwrite `family-book-avatar.*`
- keep the PNG export path exactly aligned with `BRAND_LOGO`
- the PNG should visibly match the SVG composition, not a different concept

- [ ] **Step 4: Run the asset test again**

Run:

```bash
node miniprogram/tests/brand-assets.test.js
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add miniprogram/assets/images/brand/duozanzan-avatar.svg miniprogram/assets/images/brand/duozanzan-avatar.png miniprogram/assets/images/brand/generate-duozanzan-avatar.ps1 miniprogram/tests/brand-assets.test.js
git commit -m "feat: add duozanzan brand avatar assets"
```

### Task 3: Rebrand the Profile and Settings Surfaces

**Files:**
- Create: `miniprogram/tests/brand-profile-pages.test.js`
- Modify: `miniprogram/pages/profile/index/index.js`
- Modify: `miniprogram/pages/profile/index/index.wxml`
- Modify: `miniprogram/pages/profile/index/index.wxss`
- Modify: `miniprogram/pages/profile/settings/settings.js`
- Modify: `miniprogram/pages/profile/settings/settings.wxml`
- Modify: `miniprogram/pages/profile/settings/settings.wxss`

- [ ] **Step 1: Write the failing page-branding smoke test**

Create `miniprogram/tests/brand-profile-pages.test.js`:

```javascript
const fs = require('fs');
const path = require('path');
const assert = require('assert');

function read(relativePath) {
  return fs.readFileSync(path.join(__dirname, '..', relativePath), 'utf8');
}

const profileJs = read('pages/profile/index/index.js');
const profileWxml = read('pages/profile/index/index.wxml');
const settingsJs = read('pages/profile/settings/settings.js');
const settingsWxml = read('pages/profile/settings/settings.wxml');

assert.ok(profileJs.includes("require('../../../utils/brand')"));
assert.ok(settingsJs.includes("require('../../../utils/brand')"));
assert.ok(profileWxml.includes('{{brandName}}'));
assert.ok(profileWxml.includes('{{brandSlogan}}'));
assert.ok(settingsWxml.includes('{{brandLogo}}'));
assert.ok(settingsWxml.includes('{{brandExplainer}}'));
assert.ok(settingsWxml.includes('{{brandDescription}}'));
assert.equal(profileWxml.includes('\u5bb6\u5ead\u8bb0\u8d26 v1.0'), false);
assert.equal(settingsWxml.includes('\u5bb6\u5ead\u8bb0\u8d26\u56f4\u7ed5\u8d44\u4ea7\u57fa\u7ebf\u3001\u68a6\u60f3\u50a8\u84c4\u548c\u65e5\u5e38\u8bb0\u8d26\u5c55\u5f00\u3002'), false);

console.log("brand-profile-pages.test.js passed");
```

- [ ] **Step 2: Run the branding test to verify it fails**

Run: `node miniprogram/tests/brand-profile-pages.test.js`

Expected: FAIL because the profile and settings pages still use old brand strings and do not import `utils/brand`.

- [ ] **Step 3: Apply the shared brand metadata to the two pages**

In `miniprogram/pages/profile/index/index.js`:

- import `BRAND_NAME`, `BRAND_SLOGAN`, `BRAND_SHORT_SLOGAN`, `BRAND_LOGO`
- expose them on `data`
- keep login, onboarding and navigation behavior unchanged

In `miniprogram/pages/profile/index/index.wxml`:

- replace footer `家庭记账 v1.0` with `{{brandName}} v1.0`
- replace footer slogan with `{{brandSlogan}}`
- add a small non-clickable brand row or badge that shows `{{brandLogo}}` + `{{brandShortSlogan}}`

In `miniprogram/pages/profile/settings/settings.js`:

- import `BRAND_NAME`, `BRAND_EXPLAINER`, `BRAND_DESCRIPTION`, `BRAND_LOGO`
- expose them on `data`
- keep `QUICK_ACTIONS` and login state logic intact

In `miniprogram/pages/profile/settings/settings.wxml`:

- keep page title `系统设置` and the three functional sections unchanged
- in the hero or product-info area, add the new local brand image using `{{brandLogo}}`
- replace generic product copy with:
  - `{{brandName}}`
  - `{{brandExplainer}}`
  - `{{brandDescription}}`

In both WXSS files:

- add only the styles needed for the new brand row / brand card / logo image
- do not disturb the current avatar/login/button layout

- [ ] **Step 4: Run the updated smoke tests and keep existing structure tests green**

Run:

```bash
node miniprogram/tests/brand-profile-pages.test.js
node miniprogram/tests/profile-page-structure.test.js
node miniprogram/tests/settings-page-structure.test.js
```

Expected: all PASS

- [ ] **Step 5: Commit**

```bash
git add miniprogram/tests/brand-profile-pages.test.js miniprogram/pages/profile/index/index.js miniprogram/pages/profile/index/index.wxml miniprogram/pages/profile/index/index.wxss miniprogram/pages/profile/settings/settings.js miniprogram/pages/profile/settings/settings.wxml miniprogram/pages/profile/settings/settings.wxss
git commit -m "feat: apply duozanzan branding to profile surfaces"
```

### Task 4: Roll Out Brand Copy to Profile Completion and Dream Flows

**Files:**
- Create: `miniprogram/tests/brand-copy-flow.test.js`
- Modify: `miniprogram/pages/profile/complete/index.js`
- Modify: `miniprogram/pages/profile/complete/index.wxml`
- Modify: `miniprogram/pages/index/index.wxml`
- Modify: `miniprogram/pages/dream/list/list.wxml`
- Modify: `miniprogram/pages/dream/edit/edit.wxml`
- Modify: `miniprogram/utils/dreamGoalView.js`
- Modify: `miniprogram/tests/dream-goal-view.test.js`

- [ ] **Step 1: Write the failing copy tests**

Create `miniprogram/tests/brand-copy-flow.test.js`:

```javascript
const fs = require('fs');
const path = require('path');
const assert = require('assert');

function read(relativePath) {
  return fs.readFileSync(path.join(__dirname, '..', relativePath), 'utf8');
}

const completeJs = read('pages/profile/complete/index.js');
const completeWxml = read('pages/profile/complete/index.wxml');
const homeWxml = read('pages/index/index.wxml');
const dreamListWxml = read('pages/dream/list/list.wxml');
const dreamEditWxml = read('pages/dream/edit/edit.wxml');

assert.ok(completeJs.includes("require('../../../utils/brand')"));
assert.ok(completeWxml.includes('{{brandName}}'));
assert.ok(homeWxml.includes('\u4eca\u5929\u6512\u4e00\u70b9\uff0c\u672a\u6765\u5f00\u4e00\u6735\u3002'));
assert.ok(dreamListWxml.includes('\u628a\u6bcf\u4e00\u6b21\u5c0f\u79ef\u7d2f\u6162\u6162\u6512\u6210\u4e00\u6735\u82b1'));
assert.ok(dreamEditWxml.includes('\u540e\u9762\u7684\u6bcf\u4e00\u6b21\u79ef\u7d2f\u624d\u6709\u65b9\u5411'));
assert.equal(dreamListWxml.includes('\u5148\u5efa\u4e00\u4e2a\u76ee\u6807\uff0c\u518d\u5f00\u59cb\u4e00\u7b14\u4e00\u7b14\u5730\u5b58\u94b1\u3002'), false);

console.log("brand-copy-flow.test.js passed");
```

Then update `miniprogram/tests/dream-goal-view.test.js` so the expected strings become:

```javascript
assert.equal(summary.summaryHintText, '\u5148\u5f52\u6863\uff0c\u518d\u628a\u8fd9\u4efd\u79ef\u7d2f\u7559\u7ed9\u4e0b\u4e00\u4e2a\u76ee\u6807');
assert.equal(summary.summaryHintText, '\u7ee7\u7eed\u628a\u6bcf\u4e00\u6b21\u5c0f\u79ef\u7d2f\u6512\u8fdb\u76ee\u6807');
assert.equal(summary.summaryHintText, '\u5f53\u524d\u6ca1\u6709\u8fdb\u884c\u4e2d\u7684\u68a6\u60f3\u76ee\u6807\uff0c\u53ef\u4ee5\u5f00\u59cb\u65b0\u7684\u79ef\u7d2f');
```

- [ ] **Step 2: Run the copy tests to verify they fail**

Run:

```bash
node miniprogram/tests/brand-copy-flow.test.js
node miniprogram/tests/dream-goal-view.test.js
```

Expected: FAIL because the pages and `dreamGoalView.js` still use pre-brand-rollout wording.

- [ ] **Step 3: Implement the copy rollout**

In `miniprogram/pages/profile/complete/index.js`:

- import `BRAND_NAME`
- expose `brandName` on page `data`

In `miniprogram/pages/profile/complete/index.wxml`:

- replace the subtitle with `在{{brandName}}里，昵称会和你的梦想积累一起展示。`
- keep the “默认头像自动分配”事实不变
- update the helper copy to mention that saving the nickname will sync back to personal center and dream-related pages

In `miniprogram/pages/index/index.wxml`:

- change the dream-card subtitle from “给未来的自己留一笔确定性” to `今天攒一点，未来开一朵。`
- change the empty-state copy to a warmer version, for example `从旅行、相机或应急金开始，把每一次小积累慢慢攒成一朵花`

In `miniprogram/pages/dream/list/list.wxml`:

- empty state desc: `从一个想实现的小目标开始，把每一次小积累慢慢攒成一朵花。`
- archived-only tip: `当前没有进行中的梦想目标，你可以创建新的目标继续积累。`

In `miniprogram/pages/dream/edit/edit.wxml`:

- keep the title structure
- replace the intro desc with `先把想实现的未来写下来，后面的每一次积累才有方向。`

In `miniprogram/utils/dreamGoalView.js`:

- ready summary hint: `先归档，再把这份积累留给下一个目标`
- in-progress summary hint: `继续把每一次小积累攒进目标`
- archived summary hint: `当前没有进行中的梦想目标，可以开始新的积累`

Do not change action semantics such as `summaryAction`, `saveButtonText`, `archiveButtonText` or the goal status model.

- [ ] **Step 4: Run the targeted regression tests**

Run:

```bash
node miniprogram/tests/brand-copy-flow.test.js
node miniprogram/tests/dream-goal-view.test.js
node miniprogram/tests/home-dream-summary.test.js
```

Expected: all PASS

- [ ] **Step 5: Commit**

```bash
git add miniprogram/tests/brand-copy-flow.test.js miniprogram/tests/dream-goal-view.test.js miniprogram/pages/profile/complete/index.js miniprogram/pages/profile/complete/index.wxml miniprogram/pages/index/index.wxml miniprogram/pages/dream/list/list.wxml miniprogram/pages/dream/edit/edit.wxml miniprogram/utils/dreamGoalView.js
git commit -m "feat: roll out duozanzan brand copy"
```

## Final Verification Checklist

After all four tasks are complete, run this targeted suite before handing work back:

```bash
node miniprogram/tests/brand-metadata.test.js
node miniprogram/tests/brand-assets.test.js
node miniprogram/tests/brand-profile-pages.test.js
node miniprogram/tests/brand-copy-flow.test.js
node miniprogram/tests/profile-page-structure.test.js
node miniprogram/tests/settings-page-structure.test.js
node miniprogram/tests/dream-goal-view.test.js
node miniprogram/tests/home-dream-summary.test.js
```

Expected:

- all tests PASS
- `miniprogram/assets/images/brand/duozanzan-avatar.png` exists and is non-empty
- profile footer and settings page no longer show `家庭记账` old-brand copy
- dream-related empty states and summary hints match the approved `朵攒攒` tone

## Handoff Notes

- If the user later asks to make the brand even more visual, build on the shared `brand.js` constants first and only then extend more pages.
- If the user decides to replace the old `family-book-avatar.*` files entirely, do it in a separate cleanup pass after this rollout is stable.
