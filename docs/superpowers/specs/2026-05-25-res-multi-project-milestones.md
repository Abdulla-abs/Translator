# 多文件项目（高级功能）子里程碑

**日期**：2026-05-25  
**父文档**：[2026-05-25-feature-roadmap-implementation.md](./2026-05-25-feature-roadmap-implementation.md)（主路线图仅引用本文，不展开实现细节）  
**需求来源**：[feature.md](../../feature.md) § 多文件项目  
**前置依赖**：主路线图 **P1**（xlsx 导出/读取基建）、**P2**（`StickyCompareGrid` 比对表格）完成后，本系列里程碑收益最大。

---

## 1. 范围总览

| 里程碑 | 名称 | 目标 | 预估 | 依赖 |
|--------|------|------|------|------|
| **M1** | 项目壳 + 初始化 | 创建项目、选择 res 目录、沙箱克隆、识别 `values*` | 4–5 人日 | 无（目录选择 expect/actual 可先行） |
| **M2** | 详情信息区 | 项目名称、创建时间、已导入语言列表；基础导航 | 2–3 人日 | M1 |
| **M3** | Excel 导出 | 全量合并导出、单语言选择导出 | 4–5 人日 | M1、主路线图 P1 |
| **M4** | Excel 导入比对与覆写 | 选 xlsx → 比对页 → 红格 → 确认应用更改 | 5–7 人日 | M1、M3、主路线图 P2 |
| **M5** | 版本追踪 | init 快照、push、列表、回滚、级联删除 | 5–8 人日 | M1 |

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

### 2.2 核心模块（新建）

| 模块 | 职责 |
|------|------|
| `ResMultiProjectFileStore` | 索引、meta、workspace 读写 |
| `ResMultiProjectInitializer` | 选目录 → 递归克隆 → 扫描 `isValuesResourceFolderName` |
| `ResMultiStringsMatrixBuilder` | 全语言 key 并集 + 单元格值（移植 `StringXmlOutput` 语义） |
| `ResProjectVersionStore` | 快照复制、回滚、级联删除 |
| `ui/screens/resmulti/*` | 列表、初始化、详情 |

### 2.3 平台能力

| API | 用途 |
|-----|------|
| `expect suspend fun pickResRootDirectory(): String?` | 选择 Android `res` 父目录或等价路径 |
| `expect suspend fun saveSpreadsheet(...)` | M3 导出（与单文件项目共用） |
| `expect suspend fun openSpreadsheet(): ByteArray?` | M4 导入 |

---

## 3. 里程碑详述

### M1 — 项目壳 + 初始化

**可行性：中高**

**交付物**

- `Files` 模块新增「多文件项目」入口（布局仿文件项目；「创建多文件项目」→ 命名弹窗）
- `ResMultiProjectRepository` + 持久化索引
- 详情**初始化态** UI：仅提示选择文件夹；选后后台克隆 + 扫描
- 初始化完成 → 写入 `meta.json`、触发 **M5 的 init 版本**（见 M5，可与 M1 联调或 M5 回填）

**验收标准**

- [ ] 选择用户 res 目录后，`workspace/` 为完整克隆，原始目录只读未被改写
- [ ] 正确识别 `values`、`values-en`、`values-zh-rCN` 等，语言列表写入 `meta.languages`
- [ ] 无 `strings.xml` 的 values 目录可跳过或记入警告日志
- [ ] Desktop（JVM）与 Android 至少一端可选目录；iOS 若目录选择受限，文档写明降级策略

**风险**

- 三端目录选择 API 不一致 → `expect/actual` 分平台实现，common 只消费规范化路径

---

### M2 — 详情信息区

**可行性：高**

**交付物**

- 初始化完成后的**详情主页**：项目信息卡片（名称、创建时间、语言 chips）
- 预留「项目功能」「版本追踪」按钮占位（可先 disabled，M3/M5 启用）

**验收标准**

- [ ] 从列表进入详情可见正确 meta
- [ ] 返回列表后 meta 仍持久化

**依赖**：M1

---

### M3 — Excel 导出

**可行性：中**

**交付物**

- **导出全量 xlsx**：所有语言列 + key 行（表头为语言码，首列为 key）
- **导出单个 xlsx**：弹窗选择语言 → 导出对应 `strings.xml` 单列（+ key 列）
- 按钮挂在详情「项目功能」区

**验收标准**

- [ ] 全量表与 [feature.md](../../feature.md) 示例结构一致（key | zh | en | jp）
- [ ] 单语言导出文件可在 Excel 中打开且 key/value 正确
- [ ] 空 values 或解析失败有明确错误提示

**依赖**：M1、主路线图 P1（`StringsMatrixExporter` / xlsx 编码器）

**资源范围（已确认，与主路线图 §3.4.3 一致）**：导出须包含 `string`、`string-array`（item 展平）、`plurals`（quantity 展平）；依赖主路线图 `StringsXmlCodec` / `flattenToRows` 扩展（P2-0 或 P1-2b）。

---

### M4 — Excel 导入比对与覆写

**可行性：中**

**交付物**

- 「导入 xls 比对」：选文件 → 跳转比对详情（复用 **P2** `StickyCompareGrid`）
- 按表头语言码匹配 `workspace` 下对应 `strings.xml`，按 key 逐格比对，不一致红底
- 点击红格：Popover 展示「工作区原文」vs「导入表文字」
- 顶部 menu：确认后将导入表中**有差异**的单元格覆写对应 key（`StringsXmlCodec` 写回）
- 覆写后设置 `dirty = true`（供 M5 push 启用）

**验收标准**

- [ ] 表头语言在 workspace 无对应文件时，列标记为不可用或整列跳过（产品择一，实现文档化）
- [ ] 确认覆写仅改动 diff 的 key，未改动 key 字节级不变（golden 测）
- [ ] 取消确认不修改 workspace

**依赖**：M1、M3（xlsx 解析）、主路线图 P2（表格 UI）

---

### M5 — 版本追踪

**可行性：中偏低**

**交付物**

- 项目初始化完成 → 自动 **v0001-init** 快照（等同 git init）
- **推送版本**：`dirty == true` 时按钮可用；弹窗命名 → 复制 `workspace/` → `versions/vNNNN-name/`
- **版本列表**：展示全部版本；末项操作 →「回到此版本」「删除此版本」
- **回到此版本**：确认后用快照覆盖 `workspace/`，`dirty = false`
- **删除此版本**（非最新）：警告 + 确认按钮 **5s 倒计时**；删除该版本及时间序上所有后继版本

**验收标准**

- [ ] 线性历史，无分支 UI
- [ ] 删除中间版本后，列表与磁盘均无可访问的后继快照
- [ ] 回滚后导出/导入基于回滚后内容

**依赖**：M1（workspace 布局）

**风险**

- 磁盘占用：大项目在 UI 提示「版本占用空间」
- 回滚与未 push 的编辑：回滚前建议二次确认是否丢失未推送更改

---

## 4. 推荐实施顺序

```
M1 → M2 → M3 ──┐
                ├──→ M4（需 M3 读表 + P2 表格）
M1 ─────────────┴──→ M5（可与 M2 并行，但 init 快照依赖 M1 完成）
```

- **可并行**：M2 与 M5 在 M1 之后部分并行（M5 的列表 UI 依赖 M2 详情壳）。
- **阻塞关系**：M4 不得早于 P2 + M3。

---

## 5. 测试策略（本系列）

| 里程碑 | `commonTest` | `jvmTest` | 手动 |
|--------|--------------|-----------|------|
| M1 | 目录名 → 语言码映射 | 克隆目录样本 | 三端选目录 |
| M3 | `ResMultiStringsMatrixBuilder` | 写出 xlsx 再读回 | Excel 打开 |
| M4 | diff 检测、覆写 key 子集 | 导入样本 xlsx | 红格 Popover |
| M5 | 级联删除序列表 | 快照往返 | 倒计时对话框 |

---

## 6. 产品确认记录

**已与主路线图同步**

| 项 | 决策 |
|----|------|
| 导出/导入格式 | **`.xlsx`**（不实现 `.xls`） |
| 导出/比对资源 | **string + string-array + plurals**（展平行键同主路线图 §3.4.3） |

**仍待确认（仅多文件范围）**

1. 版本删除倒计时是否固定 5 秒？
2. iOS 若无法选任意目录，是否仅支持「从 Files 导入 zip res」等降级方案？

---

## 7. 参考

| 文件 | 用途 |
|------|------|
| [feature.md](../../feature.md) | 需求原文 |
| [StringXmlOutput.java](../../android_trans/src/main/java/org/example/dom4j/StringXmlOutput.java) | 全量 key 合并 |
| [Jxl2Dom4j.java](../../android_trans/src/main/java/org/example/dom4j/Jxl2Dom4j.java) | 导入覆写参考 |
| [ValuesFolderCountryTransformer.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/usecase/ValuesFolderCountryTransformer.kt) | values 目录 → 语言码 |
| [TranslateValuesFolderUseCase.kt](../../composeApp/src/commonMain/kotlin/fun/abbas/android_res_translator/core/resources/usecase/TranslateValuesFolderUseCase.kt) | 多目录写回语义（只读参考，M4 用 Codec 直写） |

---

## 8. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 0.1 | 2026-05-25 | 从主路线图拆出多文件项目子里程碑 M1–M5 |
| 0.2 | 2026-05-25 | 同步主路线图：`.xlsx` 与 string-array/plurals 范围 |
