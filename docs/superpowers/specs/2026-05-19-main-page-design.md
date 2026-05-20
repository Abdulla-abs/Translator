# 主页（main_page）UI 设计说明

**文档类型**：产品 / 交互与架构（androidPages 对齐）  
**日期**：2026-05-19  
**设计来源**：[androidPages/main_page/](../../../androidPages/main_page/)（`code.html`、`screen.png`）  
**依赖**：根目录 [DESIGN.md](../../../DESIGN.md) 设计令牌（已在 `ui/theme` 落地）、[阶段二 UI spec](./2026-05-14-android-trans-phase1-design.md) 核心用例  
**关联页面**：`file_page`（完整文件翻译工作区，后续里程碑）

---

## 1. 目标

将当前默认首页（`TranslateScreen`：顶部「文本 | 文件」双 Tab）**替换**为 mockup 中的 **Dashboard 主页**，在单屏内提供：

1. **Quick Translate**：源/目标语言 + 原文/译文双栏，一键调用既有 `TranslatePlainTextUseCase`。
2. **File Projects**：最近 XML 项目卡片网格、上传入口、「VIEW ALL」跳转文件模块。
3. **洞察区**：静态展示「Efficiency +24%」与「Smart Sync」推广卡片（首版不接真实统计/云同步）。
4. **导航**：底栏四入口 **翻译 | 文件 | 设置 | 关于**（与 mockup 一致；桌面宽屏可选侧栏，首版可仅底栏）。

---

## 2. 与现有代码的关系

| 现状 | 目标 |
|------|------|
| `RootRoute.Translate` → `TranslateScreen`（Tab 文本/文件） | `RootRoute.Translate` → `MainDashboardScreen` |
| 文件翻译逻辑在 `TranslateFileTab` | 提取为可复用 composable；**完整编辑**在 `RootRoute.Files`（暂嵌 `TranslateFileTab` 或占位，待 `file_page` 计划） |
| 底栏 3 Tab：翻译/设置/关于 | 底栏 4 Tab：翻译/文件/设置/关于 |
| `AppTheme` + `AppComponents` | 扩展 `AppGlassCard`、语言 Chip、文件项目卡片 |

**架构约束（不变）**：Composable 不直接创建 `HttpClient`；通过 `TranslationServices`、`AppSettingsRepository`、`XmlFileAccess` 注入。

---

## 3. 信息架构

```
App
├── 翻译（默认首页 = main_page Dashboard）
│   ├── Quick Translate
│   ├── File Projects（最近项 + 上传）
│   └── 洞察区（静态）
├── 文件（file_page，本里程碑可先复用 TranslateFileTab）
├── 设置
└── 关于
```

**顶栏**：左侧 Terminal 图标 +「KMP Translator」；右侧设置图标 → `RootRoute.Settings`（不切换底栏选中态亦可接受）。

**移动端 FAB**：`+` → 与「Upload XML」相同，调用 `XmlFileAccess.launchPickXml`。

---

## 4. 区块规格（对照 code.html）

### 4.1 Quick Translate

- Section 标题：闪电图标 +「Quick Translate」(`headline-sm`)。
- **玻璃卡片**（`surface-container` 半透明 + `outline-variant` 描边 + 12dp 圆角）。
- 左栏 **SOURCE**：`label-caps` 标签；语言 Chip（显示 `defaultSourceLang`，可点击编辑为文本）；等宽多行输入。
- 右栏 **TARGET**：同上 + 只读结果区（空态：code 图标 + 占位文案）；右下角 **Translate** 圆角主按钮（`primary-container` + `auto_fix` 类图标可用文字/Unicode 代替）。
- 行为：点击翻译 → `services.translatePlainText`；加载中 `AppThinProgress`；错误 `error` 色文案。

### 4.2 File Projects

- 标题：文件夹图标 +「File Projects」；右侧 **VIEW ALL** → `RootRoute.Files`。
- **Bento 网格**：窄屏 1 列，中屏 2 列，宽屏 3 列（`WindowWidthSizeClass` 或 `BoxWithConstraints`）。
- **文件卡片**：文件名 `body-base`、修改时间 `code-sm`、进度 Badge（百分比）、4px `AppThinProgress`、已译/总 key 文案；左侧色条（进行中 `primary-container`，完成 `secondary-container`）；点击 → `Files` 并携带项目 id（首版可仅导航）。
- **上传卡片**：虚线边框 `outline-variant`，hover/按压高亮 `primary`；点击 → `launchPickXml`，解析后加入最近列表。
- **首版数据**：`RecentXmlProject` + `InMemoryRecentProjectRepository`（`commonMain`）；无持久化；列表为空时仅显示上传卡。

### 4.3 洞察区

- 左：**Efficiency +24%** 大标题 + 说明文案；背景可用 `surface-container-high` 渐变（不必加载 mockup 外链图）。
- 右：**Smart Sync** `tertiary-container` 卡片 + 图标 + 文案（纯展示，无点击逻辑）。

### 4.4 导航与响应式

| 视口 | 行为 |
|------|------|
| 紧凑（手机） | 底栏 4 项 + FAB |
| 中等/展开（平板/桌面） | 可选 `NavigationRail`（64dp，`surface-container-low`）；隐藏 FAB；底栏可隐藏（首版允许保留底栏以降低复杂度） |

---

## 5. 非目标（本 spec）

- 真实「效率统计」、Smart Sync 后端。
- `file_page` 完整布局（单独计划）。
- 语言下拉的真实 locale 列表（首版 Chip 内联编辑语言码即可）。
- Material Symbols 字体资源（首版用 Compose Material Icons Extended 或 Unicode 占位）。

---

## 6. 方案对比（已定稿）

| 方案 | 说明 | 结论 |
|------|------|------|
| A | 新 `MainDashboardScreen`，逻辑从 `TranslateScreens` 抽取 | **采用** |
| B | 在 `TranslateScreen` 上叠 Dashboard 壳 | 文件臃肿，难维护 |
| C | 仅改样式不改结构 | 无法满足 mockup 信息架构 |

---

## 7. 验收标准

- [ ] 启动应用默认进入 Dashboard，视觉与 `screen.png` 区块一致（颜色/间距沿用 `AppTheme`）。
- [ ] Quick Translate 可完成真实翻译并显示结果/错误。
- [ ] 上传 XML 后 File Projects 出现新卡片；VIEW ALL / 卡片可进入「文件」路由。
- [ ] 底栏 4 项与设置/关于/顶栏设置按钮导航正确。
- [ ] `:composeApp:compileKotlinJvm` 与 `:composeApp:jvmTest` 通过。
