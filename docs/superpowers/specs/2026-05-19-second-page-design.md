# 文件浏览页（second_page）UI 设计说明

**文档类型**：产品 / 交互与架构（androidPages 对齐）  
**日期**：2026-05-19  
**设计来源**：[androidPages/second_page/](../../../androidPages/second_page/)（`code.html`、`screen.png`）  
**依赖**：[主页 spec](./2026-05-19-main-page-design.md)、根目录 [DESIGN.md](../../../DESIGN.md)、`ui/theme` + `ui/components`  
**关联页面**：[file_page](../../../androidPages/file_page/)（点击 XML **VIEW** 后的编辑/翻译详情，单独里程碑）

---

## 1. 定位

`second_page` 对应应用 **底栏「文件」**（`RootRoute.Files`），是主页（`main_page` / Dashboard）之后的 **第二个主界面**：

| 顺序 | androidPages | 路由 | 当前实现 | 目标 |
|------|--------------|------|----------|------|
| 1 | `main_page` | `RootRoute.Translate` | `MainDashboardScreen` | 已完成 |
| 2 | `second_page` | `RootRoute.Files` | `FilesScreen` → `TranslateFileTab`（旧编辑器） | **文件浏览/列表** |
| 3 | `file_page` | （文件内子导航） | 未实现 | XML 翻译工作台（后续） |

用户从主页 **VIEW ALL**、文件卡、或底栏「文件」进入本页；在本页点击 XML 的 **VIEW** 再进入 `file_page`。

---

## 2. 目标

1. **替换** 当前 `FilesScreen` 内嵌的 `TranslateFileTab`，改为与 mockup 一致的 **文件管理列表**。
2. **面包屑**：`home → src → commonMain → resources`（可横向滚动）；点击分段回退路径。
3. **列表**：玻璃卡片容器；表头 `NAME | ACTION`；文件夹行 + XML 文件行（等宽文件名、版本/大小副标题）。
4. **操作**：文件夹进入子目录；XML **VIEW** 进入详情（首版可内嵌子页，接 `TranslateFileTab` 预填内容）；宽屏显示下载图标（首版可无实际操作）。
5. **顶栏能力**：搜索框（宽屏）、Filter（首版客户端按名称过滤）、上传 FAB。
6. **数据**：首版 **内存虚拟目录树 + 合并 `RecentXmlProjectRepository` 上传项**；不接真实磁盘遍历（`FileTreePort` 留接口）。

---

## 3. 界面结构（对照 code.html）

```
FilesScreen (FileBrowserScreen)
├── 顶栏区（Files 路由时由 AppRoot 或屏内提供）
│   ├── 标题 KMP Translator（可与全局 TopAppBar 合并）
│   ├── Search files...（sm+ 显示）
│   └── 设置
├── 面包屑 + Filter
│   ├── Home 圆钮 → 根目录
│   ├── src / commonMain / … Chip
│   └── 当前目录高亮（primary 边框）
├── AppGlassCard 列表
│   ├── 表头 Name | Action
│   ├── FolderRow（layout 等）→ chevron 下钻
│   └── XmlFileRow → VIEW（+ download 可选）
└── FAB upload_file → XmlFileAccess.launchPickXml
```

**底栏**：保持全局 4 Tab；本页选中「文件」。

---

## 4. 数据模型

```kotlin
sealed interface FileBrowserItem {
    data class Folder(val name: String, val path: String) : FileBrowserItem
    data class XmlFile(
        val id: String,
        val name: String,
        val path: String,
        val versionLabel: String,   // 首版可固定 "v1.0"
        val sizeLabel: String,      // 由 XML 字符长度估算
        val xmlContent: String?,    // 上传项有内容；mock 项可为 null
    ) : FileBrowserItem
}

data class FileBrowserState(
    val pathSegments: List<String>, // e.g. ["src","commonMain","resources"]
    val items: List<FileBrowserItem>,
    val searchQuery: String,
)
```

**虚拟树（首版 mock，与 mockup 一致）**：

- `/` → `src/`
- `src/` → `commonMain/`
- `commonMain/` → `resources/` 内含：
  - 文件夹 `layout/`
  - 文件 `strings_main.xml`、`errors_en.xml`、`auth_screens.xml`
- `layout/` → 空或占位

**与最近项目合并**：`RecentXmlProjectRepository.projects` 中项映射为 `XmlFile`，显示在 **当前目录** 或根目录 `resources/` 下（首版统一放在 `resources/`）。

---

## 5. 子导航（VIEW → file_page）

首版在 `FilesScreen` 内用 **本地状态**（不新增 `RootRoute`）：

```kotlin
sealed interface FilesUiMode {
    data object Browse : FilesUiMode
    data class Detail(val fileId: String) : FilesUiMode
}
```

- `Browse`：second_page 列表 UI  
- `Detail`：暂用 **`TranslateFileTab`** 预加载 `xmlContent`（`file_page` 计划替换为专用编辑器布局）

返回：Detail 顶栏返回键 → `Browse`。

---

## 6. 方案对比（已定稿）

| 方案 | 说明 | 结论 |
|------|------|------|
| A | 新 `FileBrowserScreen` + `InMemoryFileBrowserStore`，替换 `FilesScreen` 内容 | **采用** |
| B | 在 `TranslateFileTab` 上加 Tab「列表\|编辑」 | 与 mockup 不符 |
| C | 直接实现 `FileTreePort` 读真实目录 | 工作量大，放 2.1 |

---

## 7. 非目标

- 真实文件系统目录选择器（`FileTreePort` actual）— 后续。
- `file_page` 完整三栏翻译 UI — 独立 spec/plan。
- 下载、版本历史、云端同步。
- 桌面 `NavigationRail`（可复用 main_page 计划 Task 9，本页不阻塞）。

---

## 8. 验收标准

- [ ] 底栏「文件」展示 second_page 列表（非旧版纯 XML 表单）。
- [ ] 面包屑可点击回退；文件夹可下钻。
- [ ] 搜索框过滤当前列表名称（客户端）。
- [ ] VIEW 打开详情并可返回列表；上传 FAB/ pickXml 后列表出现新 XML。
- [ ] 主页上传/最近项目与文件列表数据一致（共享 `RecentXmlProjectRepository`）。
- [ ] `:composeApp:compileKotlinJvm` 与 `:composeApp:jvmTest` 通过。
