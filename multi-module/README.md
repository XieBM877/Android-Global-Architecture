# GlobalArchitecture / multi-module

MVI + Jetpack Compose + Jetpack 的 Android 通用脚手架（多模块形态）：单 Activity、纯 Compose 页面、无 Fragment，Navigation 3、Hilt、Room、DataStore，`build-logic` 约定插件统一构建配置。

整个目录可以直接拷贝成新项目使用；改包名与应用名见上级目录的 `tools/rename-project.py`。需要更轻的结构时看同级的 `single-module/`，两者包结构与 MVI 约定一致。

## 目录结构

```text
multi-module/
├── app/                      应用壳：Application、MainActivity、AppNavHost、启动页与主题、Hilt 聚合、混淆
├── build-logic/convention/   约定插件 scaffold.*（SDK、Kotlin、Compose、Hilt、Room、测试依赖）
├── core/
│   ├── mvi/                  MviViewModel 基类与 State/Intent/Effect 契约
│   ├── common/               调度器注入、ApplicationScope、Clock、suspendRunCatching（纯 JVM）
│   ├── model/                领域模型（纯 JVM）
│   ├── domain/               仓库接口与 UseCase（纯 JVM）
│   ├── database/             Room
│   ├── datastore/            DataStore Preferences
│   ├── network/              Retrofit + OkHttp + Moshi，错误统一映射
│   ├── data/                 仓库实现（离线优先）
│   ├── analytics/            统计接口与 Timber 实现（SDK 封装范例）
│   ├── designsystem/         主题、尺寸、通用组件
│   ├── ui/                   UiText、CollectEffects、文本框同步、空/错误/加载视图
│   └── testing/              MainDispatcherRule、Fake 仓库、测试数据（纯 JVM）
├── feature/
│   ├── home/                 笔记列表（派生状态）
│   ├── detail/               笔记编辑（自持有状态 + 校验 + 辅助注入）
│   └── settings/             主题与动态取色（只读 Effect）
├── gradle/                   版本目录 libs.versions.toml 与 wrapper
├── build.gradle.kts          Spotless 配置
├── gradle.properties
└── settings.gradle.kts
```

## 环境要求

- JDK 17（`JAVA_HOME` 指向 17，AGP 9 与 Gradle 9.8 的最低要求）
- Android SDK：安装 Platform 37 与对应 build-tools（首次构建时 AGP 会按许可自动下载 Platform）
- Android Studio：使用支持 AGP 9.4 的稳定版

## 常用命令

```bash
./gradlew assembleDebug                  # 构建 debug 包
./gradlew installDebug                   # 安装到已连接设备或模拟器
./gradlew testDebugUnitTest              # 全部单元测试（含纯 JVM 模块）
./gradlew assembleDebugAndroidTest       # 编译仪器测试
./gradlew connectedDebugAndroidTest      # 运行仪器测试（需要设备）
./gradlew lintDebug                      # Lint
./gradlew spotlessCheck                  # 代码风格检查（spotlessApply 自动修复）
./gradlew assembleRelease                # R8 压缩后的 release 包
./gradlew bundleRelease                  # 上架用 AAB
```

## 构建属性

| 属性 | 作用 | 默认 |
|:---|:---|:---|
| `-PapiBaseUrl=https://example.com/` | 笔记远程接口地址，以 `/` 结尾；为空时网络层是空实现，应用完全离线可用 | 空 |
| `-PwarningsAsErrors=true` | Kotlin 与 Lint 警告按错误处理 | `false` |
| `-PenableComposeCompilerReports=true` | 输出 Compose 编译器报告到各模块 `build/compose_compiler` | `false` |

可以写进 `~/.gradle/gradle.properties` 或命令行，不要把真实地址与密钥提交进仓库。

## 代码风格

Spotless + ktlint（`android_studio` 风格，行宽 120，允许尾逗号）。规则写在根 `build.gradle.kts` 的 `ktlintRules`；实测 Spotless 的 ktlint 步骤不读取 `.editorconfig` 里的 ktlint 规则，所以 `.editorconfig` 只保存编辑器通用设置（缩进、行宽、尾逗号），改风格时两处保持一致。Spotless 的目标只包含各模块 `src` 目录与构建脚本，不遍历 `build/` 目录：遍历正在被 AGP 改写的目录会在并行构建时报 `Could not read path`。

## 发布签名

在项目根目录新建 `keystore.properties`（已被 `.gitignore` 忽略）：

```properties
storeFile=release.jks
storePassword=******
keyAlias=******
keyPassword=******
```

`storeFile` 相对项目根目录。文件不存在时 release 包不签名，便于 CI 先出未签名包再由流水线签名。

## 约定摘要

- ViewModel 只有 `dispatch(intent)` 一个公开入口；State 单一且不可变；一次性事件走 Effect。
- 页面拆成有状态的 `Route` 与无状态的 `Screen`；导航由 `app` 通过回调注入，`feature` 之间零依赖。
- 读取一律来自 Room，网络只负责刷新；没有规则的操作不写 UseCase。
- 只让接口跨模块；新增带 Hilt `@Module` 的库模块后，必须在 `app/build.gradle.kts` 里添加依赖。
- 业务代码里不硬编码服务器地址与密钥；字符串全部进资源并附中文翻译。

详细说明见上级目录 `docs/`：

- `docs/architecture.md`：分层、数据流、依赖注入、导航、平台适配
- `docs/mvi-guide.md`：MVI 写法、测试配方、反模式
- `docs/modularization.md`：单模块与多模块对照、迁移步骤
- `docs/version-baseline.md`：版本基线与 Google Play 合规清单

## 测试

| 层 | 位置 | 工具 |
|:---|:---|:---|
| 领域规则、工具 | `core/domain`、`core/common` 的 `src/test` | JUnit4 |
| ViewModel | 各 feature 与 `app` 的 `src/test` | Fake 仓库、`MainDispatcherRule`、Turbine、MockK |
| 仓库、数据源 | `core/data`、`core/datastore`、`core/network` 的 `src/test` | Fake、`TemporaryFolder`、MockWebServer |
| 数据库 | `core/database` 的 `src/androidTest` | 内存 Room |
| Compose 界面 | 各 feature 与 `core/ui` 的 `src/androidTest` | `createComposeRule` + `testTag` |
| 整体流程 | `app` 的 `src/androidTest` | `createAndroidComposeRule<MainActivity>` |

## 去掉示例业务

示例业务（笔记）只用来演示各层怎么配合。要换成自己的业务：

1. 删除 `feature/home|detail|settings` 或改造它们，同步修改 `settings.gradle.kts`、`app/build.gradle.kts`、`AppNavHost`。
2. 替换 `core/model`、`core/domain`、`core/database`、`core/network`、`core/data` 里与笔记有关的类型。
3. 保留 `core/mvi`、`core/common`、`core/designsystem`、`core/ui`、`core/analytics`、`core/testing` 与 `build-logic`，它们与业务无关。
