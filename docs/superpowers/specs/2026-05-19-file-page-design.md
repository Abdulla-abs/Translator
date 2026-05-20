# XML 翻译工作台（file_page）设计说明

**日期**：2026-05-19  
**设计来源**：[androidPages/file_page/](../../../androidPages/file_page/)  
**路由**：`RootRoute.Files` → `FilesUiMode.Detail` → `FileEditorScreen`

## 范围

- 整体进度、暂停/继续、导出 XML
- 按 key 列表展示源/目标对照
- 逐条调用 `TranslationSegmentPort` 翻译
- 替换原 `TranslateFileTab` 详情占位

## 非目标

- 单条内联编辑、删除条目
- 真实 Pause 后断点续传持久化
