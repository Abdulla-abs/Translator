# 多文件项目（高级功能）子里程碑

**日期**：2026-05-25  
**父文档**：[2026-05-25-feature-roadmap-implementation.md](./2026-05-25-feature-roadmap-implementation.md)（主路线图仅引用本文，不展开实现细节）  
**需求来源**：[feature.md](../../feature.md) § 多文件项目  
**前置依赖**：主路线图 **P1**（xlsx 导出/读取基建）、**P2**（`StickyCompareGrid` 比对表格）完成后，本系列里程碑收益最大。

> **实施状态（v0.7）**
>
> | 里程碑 | 状态 | 说明 |
> |--------|------|------|
> | **M1** | ✅ **已完成**（2026-05-25，已手动验收） | 主页多文件项目入口、沙箱克隆、values* 扫描、init 快照 |
> | **M2** | ✅ **已完成**（2026-05-25） | 详情信息卡 + 项目功能/版本追踪 UI 壳 |
> | **M3** | ✅ **已完成**（2026-05-26，已手动验收） | 全量/单语言 xlsx 导出、`ResMultiStringsMatrixBuilder` |
> | **M4** | ✅ **已完成**（2026-05-26，已手动验收） | xlsx 导入比对、多列表格、差异覆写、`dirty` 标记 |
> | **M5** | ✅ **已完成**（2026-05-26） | push / 版本列表 / 回滚 / 级联删除（5s 倒计时） |
>
> **文档索引**：§0 可行性复评 · §9 M1 · §10 M3/M4 · §11 M5 落地清单

---

## 0. 可行性复评（2026-05-25）

### 0.1 总体结论

| 维度 | 复评结论 |
|------|----------|
| **系列整体** | **中高（可实现）**，较初评「中偏低」上调 |
| **建议启动时机** | **M1–M5 已完成**；后续为 iOS 平台补齐与手动验收 |
| **最大风险** | iOS **目录/表格选择仍为 stub**；M5 级联删除 UX 与磁盘占用；外部 Excel 复杂格式 xlsx 未全面兼容 |
| **版本追踪** | 维持 **目录快照**（非外置 Git）；与 KMP 三端、级联删除 UX 最匹配（见主路线图讨论） |

**一句话**：M1–M5 已交付；建议 JVM/Android 手动验收版本流程，iOS 目录/表格选择仍待补齐。

### 0.2 已具备基线（P1/P2 完成后）

| 能力 | 状态 | 代码锚点 |
|------|------|----------|
| `strings.xml` 解析/序列化（含 `plurals`） | ✅ | `StringsXmlCodec.kt` |
| 资源展平（string / array / plurals） | ✅ | `ResourceFlattener.kt` |
| 双列比对矩阵 + 吸顶表格 UI | ✅ | `CompareMatrixBuilder`、`StickyCompareGrid` |
| `.xlsx` **写出**（ZIP + OOXML） | ✅ | `MinimalXlsxEncoder`、`StringsMatrixExporter`、`ZipArchiveWriter` |
| `.xlsx` **读入** | ✅（M4） | `MinimalXlsxDecoder`、`readZipArchive`（JVM/Android 用 `ZipInputStream`，iOS 用 `StoredZipReader`） |
| 单文件两列导出矩阵 | ✅（无 plurals 行） | `StringsMatrixBuilder.buildForFileEditor` |
| values 目录识别与语言码映射 | ✅ | `isValuesResourceFolderName`、`ValuesFolderCountryTransformer` |
| 多 values 目录遍历语义 | ✅（翻译用例） | `TranslateValuesFolderUseCase` |
| 项目持久化模式（索引 JSON + 沙箱目录） | ✅ | `TranslationProjectFileStore`、`CompareProjectFileStore`、**`ResMultiProjectFileStore`** |
| 平台文件原子写 / 递归删 | ✅ | `PlatformFileSystem.kt` |
| **递归目录复制 / res 克隆** | ✅（M1） | `copyDirectoryRecursively`、`cloneResDirectoryToWorkspace` |
| 多文件项目 values* 扫描 | ✅（M1） | `ResMultiProjectScanner` |
| 多文件项目版本快照 push/回滚/级联删 | ✅（M5） | `ResProjectVersionStore` |
| 目录选择 UI | ⚠️ 部分 | `rememberDirectoryPicker`：JVM ✅、Android SAF ✅（含持久读权限）、**iOS 返回 null** |
| xlsx/spreadsheet 保存 | ✅ JVM/Android | `XmlFileAccess.launchSaveSpreadsheet` |
| xlsx/spreadsheet 打开 | ✅ JVM/Android | `XmlFileAccess.launchPickSpreadsheet` |
| 多列导入比对表格 | ✅（M4） | `StickyMultiCompareGrid`、`ResMultiImportCompareScreen` |
| 主页/比对/多文件 UI 模式（卡片 + 上传区网格） | ✅ | `FileProjectsSection`、`CompareProjectsSection`、**`ResMultiProjectsSection`** |

### 0.3 分里程碑复评

| 里程碑 | 初评 | **复评** | 变化原因 |
|--------|------|----------|----------|
| **M1** 项目壳 + 初始化 | 中高 | ✅ **已交付** | JVM + Android SAF 克隆已验收；iOS 选目录仍 stub |
| **M2** 详情信息区 | 高 | **高** | 纯 UI + meta 读取，无新基建 |
| **M3** Excel 导出 | 中 | ✅ **已交付** | `ResMultiStringsMatrixBuilder` + `ResMultiProjectExporter`；含 plurals 展平行 |
| **M4** 导入比对与覆写 | 中 | ✅ **已交付** | `MinimalXlsxDecoder` + `StickyMultiCompareGrid` + `FlatResourceWriter` 局部覆写 |
| **M5** 版本追踪 | 中偏低 | **中高** | 不引入 Git；`deletePathRecursively` + 目录复制快照与现有 persistence 风格一致；级联删除为纯 Kotlin 索引逻辑 |

### 0.4 关键风险与对策

| 风险 | 严重度 | 对策 |
|------|--------|------|
| Android `OpenDocumentTree` 仅得 URI | ~~高~~ **已缓解（M1）** | 已实现 `DocumentFile` 树复制 + `takePersistableUriPermission`；复杂/超大目录仍需压测 |
| iOS 目录选择未实现 | **中** | zip 导入降级；或 iOS 暂不列入验收 |
| M4 需 N 列比对，当前 `CompareMatrix` 仅 2 列 | ~~中~~ **已缓解（M4）** | 新建 `ResMultiImportCompareMatrix` + `StickyMultiCompareGrid` |
| 单文件 `StringsMatrixBuilder` 未含 plurals | **低** | M3 直接基于 `ResourceFlattener` 建多语言矩阵，勿扩展 editor 专用 builder |
| 大项目多版本磁盘占用 | **中** | M5 全量快照；UI 提示占用，后续可做「仅 strings.xml」浅快照优化（非 v1） |
| M4 覆写需合并回 `string`/`string-array`/`plurals` 结构 | ~~中~~ **已缓解（M4）** | `FlatResourceWriter` + `ResMultiImportApplier`；`ResMultiImportApplierTest` / `ResMultiImportApplierJvmTest` |

### 0.5 修订工作量（人日）

| 里程碑 | 原估算 | **复评估算** | 说明 |
|--------|--------|--------------|------|
| M1 | 4–5 | **5–7** | Android SAF 克隆上浮 |
| M2 | 2–3 | **2–3** | 不变 |
| M3 | 4–5 | **3–5** | 编码器已有，主要做 N 列 builder + UI |
| M4 | 5–7 | **6–9** | xlsx 解码 + 多列比对 + Popover + 写回 |
| M5 | 5–8 | **4–6** | 快照方案明确，无 Git 集成 |
| **合计** | 20–28 | **20–30** | 与初评接近，风险自 M1/M4 转移 |

### 0.6 实施建议（复评）

1. ~~**M1 技术 spike**~~ ✅ 已完成并手动验收。
2. ~~**M3/M4 xlsx 导出/导入**~~ ✅ 已完成并手动验收（2026-05-26）。
3. **当前建议**：对 **M5** 做 JVM/Android 手动验收（导入 → dirty → 推送 → 回滚 / 级联删除）；iOS 平台能力仍 stub。
4. **不建议**依赖用户安装 Git；目录快照与现有 `copyDirectoryRecursively` 一致即可。

---

## 1. 范围总览

| 里程碑 | 名称 | 目标 | 预估 | 依赖 | 状态 |
|--------|------|------|------|------|------|
| **M1** | 项目壳 + 初始化 | 创建项目、选择 res 目录、沙箱克隆、识别 `values*` | 4–5 人日 | 无 | ✅ 已完成 |
| **M2** | 详情信息区 | 项目名称、创建时间、已导入语言列表；基础导航 | 2–3 人日 | M1 | ✅ 已完成 |
| **M3** | Excel 导出 | 全量合并导出、单语言选择导出 | 4–5 人日 | M1、主路线图 P1 | ✅ 已完成 |
| **M4** | Excel 导入比对与覆写 | 选 xlsx → 比对页 → 红格 → 确认应用更改 | 5–7 人日 | M1、M3、主路线图 P2 | ✅ 已完成 |
| **M5** | 版本追踪 | init 快照、push、列表、回滚、级联删除 | 5–8 人日 | M1 | ✅ 已完成 |

**合计**：约 20–28 人日（与主路线图原 P3 估算一致，但可独立排期与验收）。

**非目标（全系列）**

- 分支、merge、三方合并
- 修改用户设备上的原始 res 目录（**仅操作沙箱 `workspace/`**）
- 在线同步 / 云备份

---

## 2. 共享架构约定

### 2.1 目录布局（沙箱）

```
{appData}/res-multi-projects/
  index.json
  {projectId}/
    meta.json                 # 名称、createdAt、sourceResPath（仅记录）、languages[]
    workspace/                # 可编辑克隆体，结构同用户选的 res/
      values/strings.xml
      values-zh/strings.xml
      ...
    versions/
      v0001-init/
      v0002-{userName}/
      ...
    versions-index.json       # 有序版本列表 + headId + dirty 标志
```

### 2.2 核心模块

| 模块 | 职责 | 状态 |
|------|------|------|
| `ResMultiProjectFileStore` | 索引、meta、workspace 读写、初始化编排 | ✅ M1 |
| `ResMultiProjectScanner` | 扫描 `workspace` 下 `values*` + `strings.xml` | ✅ M1 |
| `ResProjectVersionStore` | init / push / restore / 级联删除 | ✅ |
| `cloneResDirectoryToWorkspace` | 选目录源 → 沙箱克隆（JVM 路径 / Android URI） | ✅ M1 |
| `ResMultiStringsMatrixBuilder` / `ResMultiProjectExporter` | 全语言 key 并集、xlsx 导出 | ✅ M3 |
| `MinimalXlsxDecoder` / `StringsMatrixImporter` | xlsx 读回为 `StringsMatrix` | ✅ M4 |
| `ResMultiImportCompareBuilder` / `ResMultiImportApplier` | 多列比对、差异覆写、`dirty` | ✅ M4 |
| `FlatResourceWriter` | 展平 key → `StringsXmlCodec` 写回 | ✅ M4 |
| `StickyMultiCompareGrid` / `ResMultiImportCompareScreen` | 多列吸顶比对 UI | ✅ M4 |
| `ui/screens/resmulti/*`、`ResMultiProjectCard` | 列表、初始化、详情、导入比对导航 | ✅ M1–M4 |

### 2.3 平台能力

| API | 用途 | 状态 |
|-----|------|------|
| `rememberDirectoryPicker` | 选择 `res` 父目录（JVM 路径 / Android `content://` 树） | ✅ M1 |
| `cloneResDirectoryToWorkspace` | 递归克隆到 `workspace/` | ✅ M1 |
| `copyDirectoryRecursively` / `listFileNamesInDirectory` | 本地沙箱读写 | ✅ M1 |
| `launchSaveSpreadsheet` | M3 导出 | ✅ 已有 |
| `launchPickSpreadsheet` | M4 导入 | ✅ M4 |

---

## 3. 里程碑详述

### M1 — 项目壳 + 初始化

**状态：✅ 已完成**（2026-05-25，已手动验收）

**可行性：中高**（已验证；Android SAF 已通过 `DocumentFile` 克隆）

**交付物（已实现）**

- 主页 **「多文件项目」** 区块（`ResMultiProjectsSection`，布局对齐文件项目/比对；创建卡片在列表末尾）
- `ResMultiProjectRepository` + `ResMultiProjectFileStore` + `index.json` / `meta.json`
- `ResMultiProjectScreen`：待初始化选目录 → 克隆中 → 就绪页展示语言 chips
- 初始化完成 → 写入 `meta.json`、扫描 `languages[]`、**`ResProjectVersionStore.createInitSnapshot`**（`versions/v0001-init/`）

> **与初稿差异**：入口在 **Translate 主页**，尚未在 `Files` Tab 增加子 Tab（若产品要求与 spec 原文完全一致，可在 M2 补路由）。

**验收标准**

- [x] 选择用户 res 目录后，`workspace/` 为完整克隆，原始目录只读未被改写
- [x] 正确识别 `values`、`values-en`、`values-zh-rCN` 等，语言列表写入 `meta.languages`
- [x] 无 `strings.xml` 的 values 目录可跳过（扫描结果 `warnings`）
- [x] Desktop（JVM）与 Android 可选目录并克隆；iOS 选目录仍为 stub（返回 null，未列入本次验收）

**自动化测试**

- `ResMultiProjectScannerJvmTest`：values* 扫描与警告
- `ResMultiProjectFileStoreTest`：克隆、meta、init 版本目录

**已知限制 / 后续**

- iOS：需 zip 导入或实现目录选择（§6 待确认项 2）
- `Files` Tab 独立入口：未做，可按产品决定是否在 M2 补充

---

### M2 — 详情信息区

**状态：✅ 已完成**（2026-05-25）

**可行性：高**

**交付物（已实现）**

- 初始化完成后的**详情主页**（`ResMultiProjectDetailSections`）：
  - **项目信息**卡片：名称、创建时间、来源路径、语言数量、语言 chips
  - **项目功能**区：导出全量 / 单语言 xlsx、导入比对（M3/M4）
  - **版本追踪**区：由 `ResMultiProjectVersionUi` 提供（M5 已启用，见 §11）
- 进入详情时 `repository.reloadFromDisk()` 确保 meta 与磁盘一致

**验收标准**

- [x] 从列表进入详情可见正确 meta
- [x] 返回列表后 meta 仍持久化（`ResMultiProjectRepositoryTest`）

**依赖**：M1

---

### M3 — Excel 导出

**状态：✅ 已完成**（2026-05-26，已手动验收）

**可行性：中高**（已验证）

**交付物（已实现）**

- **导出全量 xlsx**：所有语言列 + key 行（表头为语言码，首列为 key）
- **导出单个 xlsx**：弹窗选择语言 → 导出对应 `strings.xml` 单列（+ key 列）
- 详情「项目功能」区按钮 + `launchSaveSpreadsheet` 保存

**自动化测试**

- `ResMultiStringsMatrixBuilderTest`（commonTest）
- `ResMultiProjectExporterJvmTest`（jvmTest）

**验收标准**

- [x] 全量表与 [feature.md](../../feature.md) 示例结构一致（key | zh | en | jp）
- [x] 单语言导出文件可在 Excel 中打开且 key/value 正确
- [x] 空 values 或解析失败有明确错误提示

**依赖**：M1、P1（`StringsMatrixExporter` / `StringsMatrix` 多列模型）

**实现提示**：`ResMultiStringsMatrixBuilder` 应对每个 `workspace/values-*` 调用 `StringsXmlCodec.parse` + `ResourceFlattener.flatten`，按 key 并集拼 `StringsMatrix`；勿复用仅支持两列且不含 plurals 的 `buildForFileEditor`。

---

### M4 — Excel 导入比对与覆写

**状态：✅ 已完成**（2026-05-26，已手动验收）

**可行性：中**（已验证；xlsx 解码与多列比对已落地）

**产品决策（实现）**：表头语言在 workspace 无匹配时**整列跳过**，并在比对页摘要中列出 `skippedHeaders`。

**交付物（已实现）**

- 详情 **「导入表格比对」** → `launchPickSpreadsheet` 选 `.xlsx` → `DashboardUiMode.ResMultiImportCompare`
- **`ResMultiImportCompareScreen`** + **`StickyMultiCompareGrid`**：按语言列展示导入值，与工作区逐格比对，差异红底
- 点击红格：对话框展示「工作区」vs「表格」文案
- 右上角菜单 **「应用表格更改」**：确认后 `ResMultiImportApplier` 仅覆写有差异的 key；`meta.dirty` 与 `versions-index.json` 的 `dirty` 置 `true`
- 导航：`MainDashboardScreen.openResMultiImportCompare`；应用成功后返回详情并 `reloadFromDisk()`

**自动化测试**

- `MinimalXlsxDecoderTest`、`ResMultiImportCompareBuilderTest`、`ResMultiImportApplierTest`（commonTest）
- `ResMultiImportApplierJvmTest`（jvmTest）

**已知限制**

- iOS：`launchPickSpreadsheet` 仍为 stub
- xlsx 解码优先兼容本应用 **M3 导出格式**（`MinimalXlsxEncoder` 产出）；外部 Excel 复杂特性未保证
- M4 覆写后 `dirty == true` 时详情页 **推送版本** 可用（M5）

**验收标准**

- [x] 表头语言在 workspace 无对应文件时，列标记为不可用或整列跳过（产品择一，实现文档化）
- [x] 确认覆写仅改动 diff 的 key，未改动 key 字节级不变（golden 测）
- [x] 取消确认不修改 workspace

**依赖**：M1、M3（xlsx 解析）、主路线图 P2（表格 UI）

---

### M5 — 版本追踪

**状态：✅ 已完成**（2026-05-26；建议 JVM/Android 手动验收）

**可行性：中高**（目录快照 + `deletePathRecursively`；**不采用** Git）

**交付物（已实现）**

- 初始化 → **v0001-init**（M1 `createInitSnapshot`）
- **`ResProjectVersionStore`**：`pushVersion`、`restoreToVersion`、`deleteVersionAndSuccessors`、`requiresCascadeDeleteCountdown`（5s）
- 详情 **`ResMultiProjectVersionSection`**：`dirty` 时推送；版本列表对话框；恢复/删除确认（非 HEAD 删除带级联警告与倒计时）
- **`PersistentResMultiProjectRepository`**：版本操作后 `reloadFromDisk()`

**自动化测试**

- `ResProjectVersionStoreTest`（commonTest：ID 分配、倒计时判定、索引前缀逻辑）
- `ResProjectVersionStoreJvmTest`（jvmTest：push/restore 往返、级联删除磁盘目录）

**验收标准**

- [x] 线性历史，无分支 UI
- [x] 删除中间版本后索引与 `versions/` 下后继目录均移除（jvmTest）
- [ ] 回滚后导出/导入基于回滚后内容（**待手动验收**）
- [ ] 级联删除倒计时对话框（**待手动验收**）

**依赖**：M1

**已知限制**

- 未实现「版本占用空间」磁盘提示（规格风险项，可后续补）
- iOS 未测

---

## 4. 推荐实施顺序

```
M1 ✅ → M2 ✅ → M3 ✅ → M4 ✅
M1 ✅ ─────────────────→ M5 ✅
```

- **当前建议**：全系列功能已落地；优先 JVM/Android 端到端验收，iOS 选目录/选表仍为 stub。

---

## 5. 测试策略（本系列）

| 里程碑 | `commonTest` | `jvmTest` | 手动 |
|--------|--------------|-----------|------|
| M1 | `joinPath` 等 | ✅ `ResMultiProjectScannerJvmTest`、`ResMultiProjectFileStoreTest` | ✅ JVM/Android 已验收；iOS 未测 |
| M3 | ✅ `ResMultiStringsMatrixBuilderTest` | ✅ `ResMultiProjectExporterJvmTest` | ✅ 已手动验收 |
| M4 | ✅ `ResMultiImportCompareBuilderTest`、`ResMultiImportApplierTest`、`MinimalXlsxDecoderTest` | ✅ `ResMultiImportApplierJvmTest` | ✅ 已手动验收 |
| M5 | ✅ `ResProjectVersionStoreTest` | ✅ `ResProjectVersionStoreJvmTest` | 倒计时对话框、回滚后导出 |

---

## 6. 产品确认记录

**已与主路线图同步**

| 项 | 决策 |
|----|------|
| 导出/导入格式 | **`.xlsx`**（不实现 `.xls`） |
| 导出/比对资源 | **string + string-array + plurals**（展平行键同主路线图 §3.4.3） |

**仍待确认（仅多文件范围）**

1. ~~版本删除倒计时是否固定 5 秒？~~ **已确认：5 秒**（`ResProjectVersionStore.cascadeDeleteCountdownSeconds`）
2. iOS 若无法选任意目录，是否仅支持「从 Files 导入 zip res」等降级方案？
3. ~~**M1 验收范围**~~ **已确认（2026-05-25）**：JVM + Android SAF 全量目录克隆已验收；iOS 暂不验收。

**M1 已确认（实施）**

| 项 | 决策 |
|----|------|
| 入口位置 | 主页「多文件项目」区块（`MainDashboardScreen`） |
| Android 克隆 | SAF `OpenDocumentTree` + `DocumentFile` 递归复制 |
| init 版本 | 初始化成功后自动 `v0001-init`（`ResProjectVersionStore`） |

**M4 已确认（实施）**

| 项 | 决策 |
|----|------|
| 无匹配语言列 | **整列跳过**，摘要展示 `skippedHeaders` |
| 比对表格 | 新建 `StickyMultiCompareGrid`（非扩展双列 `StickyCompareGrid`） |
| 覆写范围 | 仅 `hasDifference` 单元格；`FlatResourceWriter` 按 `FlatRowKind` 写回 |
| zip 读取 | JVM/Android：`ZipInputStream`；iOS：`StoredZipReader`（与 M3 写出格式对齐） |

---

## 7. 参考

| 文件 | 用途 |
|------|------|
| [feature.md](../../feature.md) | 需求原文 |
| [StringXmlOutput.java](../../android_trans/src/main/java/org/example/dom4j/StringXmlOutput.java) | 全量 key 合并 |
| [Jxl2Dom4j.java](../../android_trans/src/main/java/org/example/dom4j/Jxl2Dom4j.java) | 导入覆写参考 |
| [ValuesFolderCountryTransformer.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/usecase/ValuesFolderCountryTransformer.kt) | values 目录 → 语言码 |
| [TranslateValuesFolderUseCase.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/usecase/TranslateValuesFolderUseCase.kt) | 多目录写回语义（只读参考，M4 用 Codec 直写） |
| [ResMultiProjectFileStore.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/persistence/ResMultiProjectFileStore.kt) | M1 持久化与初始化 |
| [ResMultiProjectScanner.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/resmulti/ResMultiProjectScanner.kt) | values* 扫描 |
| [ResDirectoryCloner.*](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/persistence/ResDirectoryCloner.kt) | 跨平台 res 目录克隆 |
| [ResMultiProjectsSection.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/resmulti/ResMultiProjectsSection.kt) | 主页列表与创建入口 |
| [ResMultiStringsMatrixBuilder.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/resmulti/ResMultiStringsMatrixBuilder.kt) | M3 多语言矩阵 |
| [ResMultiProjectExporter.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/resmulti/ResMultiProjectExporter.kt) | M3 导出编排 |
| [MinimalXlsxDecoder.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/export/MinimalXlsxDecoder.kt) | M4 xlsx 解码 |
| [ResMultiImportCompareMatrix.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/resmulti/ResMultiImportCompareMatrix.kt) | M4 比对矩阵构建 |
| [ResMultiImportApplier.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/resmulti/ResMultiImportApplier.kt) | M4 差异覆写 |
| [StickyMultiCompareGrid.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/components/grid/StickyMultiCompareGrid.kt) | M4 多列 UI |
| [ResMultiImportCompareScreen.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/resmulti/ResMultiImportCompareScreen.kt) | M4 比对详情页 |
| [ResProjectVersionStore.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/persistence/ResProjectVersionStore.kt) | M5 快照 push/restore/级联删 |
| [ResMultiProjectVersionUi.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/ui/screens/resmulti/ResMultiProjectVersionUi.kt) | M5 版本区 UI 与确认对话框 |

---

## 9. M1 落地清单（2026-05-25）

| 路径 | 说明 |
|------|------|
| `persistence/ResMultiProjectFileStore.kt` | 项目 CRUD、`initializeFromSource` |
| `persistence/ResMultiProjectIndex.kt` | 索引 / meta / 版本索引序列化 |
| `persistence/ResProjectVersionStore.kt` | `v0001-init` 快照 |
| `persistence/ResDirectoryCloner.kt` + `*.jvm/android/ios.kt` | 目录克隆 |
| `persistence/PlatformFileSystem.kt` | `appResMultiProjectsRoot`、`copyDirectoryRecursively` 等 |
| `core/resources/resmulti/ResMultiProjectScanner.kt` | 语言目录扫描 |
| `ui/screens/resmulti/*` | Repository、Screen、Section |
| `ui/components/ResMultiProjectCard.kt` | 项目卡片 |
| `ui/screens/MainDashboardScreen.kt` | 导航与主页区块 |
| `composeResources/.../resmulti_*` | 中英文文案 |

---

## 10. M3 / M4 落地清单（2026-05-26）

| 路径 | 说明 |
|------|------|
| `core/resources/resmulti/ResMultiStringsMatrixBuilder.kt` | M3 多语言矩阵（含 plurals 展平） |
| `core/resources/resmulti/ResMultiProjectExporter.kt` | M3 从 workspace 导出 xlsx |
| `core/resources/export/MinimalXlsxDecoder.kt` | M4 xlsx → `StringsMatrix` |
| `core/resources/export/ZipArchiveReader.*` | 跨平台 ZIP 读（JVM/Android/iOS） |
| `core/resources/export/StoredZipReader.kt` | iOS 用纯 Kotlin ZIP 读 |
| `core/resources/export/StoredZipWriter.kt` | 修正中央目录头偏移（M3/M4 共用写出） |
| `core/resources/resmulti/ResMultiImportCompareMatrix.kt` | M4 比对矩阵与表头语言匹配 |
| `core/resources/resmulti/ResMultiProjectImportService.kt` | M4 解码 + 构建比对 |
| `core/resources/resmulti/FlatResourceWriter.kt` | M4 展平 key 写回 |
| `core/resources/resmulti/ResMultiImportApplier.kt` | M4 覆写与 `dirty` |
| `ui/components/grid/StickyMultiCompareGrid.kt` | M4 多列吸顶表格 |
| `ui/screens/resmulti/ResMultiImportCompareScreen.kt` | M4 比对详情与确认应用 |
| `ui/screens/resmulti/ResMultiProjectScreen.kt` | M3 导出 + M4 选表入口 |
| `ui/screens/MainDashboardScreen.kt` | `ResMultiImportCompare` 导航 |
| `ui/files/XmlFileAccess.*` | `launchPickSpreadsheet` / `launchSaveSpreadsheet` |
| `composeResources/.../resmulti_import_*` 等 | M4 中英文文案 |

---

## 11. M5 落地清单（2026-05-26）

| 路径 | 说明 |
|------|------|
| `persistence/ResProjectVersionStore.kt` | push / restore / `deleteVersionAndSuccessors`、5s 级联删除判定 |
| `ui/screens/resmulti/ResMultiProjectVersionUi.kt` | 版本区、推送命名、版本列表、恢复/删除确认 |
| `ui/screens/resmulti/ResMultiProjectScreen.kt` | 版本操作编排、`versionRefreshKey` |
| `ui/screens/resmulti/ResMultiProjectRepository.kt` | `pushVersion` / `restoreToVersion` / `deleteVersionAndSuccessors` |
| `composeResources/.../resmulti_version_*` | M5 中英文文案 |
| `commonTest/.../ResProjectVersionStoreTest.kt` | 纯逻辑单测 |
| `jvmTest/.../ResProjectVersionStoreJvmTest.kt` | 磁盘往返与级联删除 |

---

## 8. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 0.1 | 2026-05-25 | 从主路线图拆出多文件项目子里程碑 M1–M5 |
| 0.2 | 2026-05-25 | 同步主路线图：`.xlsx` 与 string-array/plurals 范围 |
| 0.3 | 2026-05-25 | P1/P2 完成后可行性复评：§0 基线表、里程碑评级调整、平台风险与工作量修订 |
| 0.4 | 2026-05-25 | **M1 完成**：验收勾选、§9 落地清单、基线/平台表更新；M5 init 快照记为部分完成 |
| 0.5 | 2026-05-25 | **M2 完成**：详情三区（信息/功能/版本）、disabled 占位、`ResMultiProjectRepositoryTest` |
| 0.6 | 2026-05-26 | **M3/M4 完成**：导出/导入比对/覆写、§10 落地清单、基线表与测试策略更新；**M4 已手动验收** |
| 0.7 | 2026-05-26 | **M5 完成**：版本 push/列表/回滚/级联删除 UI、`ResProjectVersionStore` 全量 API、§11 落地清单 |
