# GlobalArchitecture / single-module

MVI + Jetpack Compose + Jetpack 的 Android 通用脚手架（单模块形态）：一个 `app` 模块加 `build-logic` 约定插件。单 Activity、纯 Compose 页面、无 Fragment，Navigation 3、Hilt、Room、DataStore。

整个目录可以直接拷贝成新项目使用；改包名与应用名见上级目录的 `tools/rename-project.py`。页面或团队规模上来之后，按上级目录 `docs/modularization.md` 逐包拆成 `multi-module/` 的结构，包名不变。

## 目录结构

```text
single-module/
├── app/
│   ├── build.gradle.kts         依赖、签名、R8、BuildConfig
│   ├── proguard-rules.pro
│   ├── schemas/                 Room 导出的 schema
│   └── src/
│       ├── main/kotlin/com/example/globalarch/
│       │   ├── GlobalArchApplication.kt   MainActivity.kt   MainViewModel.kt
│       │   ├── navigation/      AppNavHost、大屏详情占位
│       │   ├── core/            mvi common model domain database datastore network data analytics designsystem ui
│       │   └── feature/         home（派生状态）detail（自持有状态 + 校验）settings（只读 Effect）
│       ├── main/res/            values/strings.xml 与各包拆分的 strings_<name>.xml，以及 values-zh-rCN
│       ├── test/kotlin/         单元测试，含原 core:testing 的 Fake 仓库与 MainDispatcherRule
│       └── androidTest/kotlin/  Room、Compose 界面、整体流程
├── build-logic/convention/      约定插件 scaffold.*（application、application.compose、hilt、room）
├── gradle/                      版本目录 libs.versions.toml 与 wrapper
├── build.gradle.kts             Spotless 配置
├── gradle.properties
└── settings.gradle.kts
```

`core/*` 与 `feature/*` 是包，不是 Gradle 模块；它们与 `multi-module/` 里的同名模块一一对应，包名、类名完全相同。

## 环境要求

- JDK 17（`JAVA_HOME` 指向 17，AGP 9 与 Gradle 9.8 的最低要求）
- Android SDK：安装 Platform 37 与对应 build-tools（首次构建时 AGP 会按许可自动下载 Platform）
- Android Studio：使用支持 AGP 9.4 的稳定版

## 常用命令

```bash
./gradlew assembleDebug                  # 构建 debug 包
./gradlew installDebug                   # 安装到已连接设备或模拟器
./gradlew testDebugUnitTest              # 单元测试（含包边界检查）
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
| `-PenableComposeCompilerReports=true` | 输出 Compose 编译器报告到 `app/build/compose_compiler` | `false` |

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

## 包边界

单模块没有 Gradle 依赖来阻止越界引用，边界由 `app/src/test/kotlin/com/example/globalarch/architecture/PackageBoundaryTest.kt` 守住：

- 按包名扫描 `src/main/kotlin` 的 import，违反依赖方向（例如 `feature` 引用 `core.data`，`feature` 之间互相引用，`core.model`/`core.domain`/`core.common` 引用 `android.*`）时 `testDebugUnitTest` 失败；
- 新增 `core.*` 或 `feature.*` 包时，必须在 `ALLOWED_DEPENDENCIES` 里登记它能依赖谁；
- 依赖规则表见上级目录 `docs/modularization.md` 第 2 节。

## 约定摘要

- ViewModel 只有 `dispatch(intent)` 一个公开入口；State 单一且不可变；一次性事件走 Effect。
- 页面拆成有状态的 `Route` 与无状态的 `Screen`；导航由 `AppNavHost` 通过回调注入，`feature` 包之间零依赖。
- 读取一律来自 Room，网络只负责刷新；没有规则的操作不写 UseCase。
- 业务代码里不硬编码服务器地址与密钥；字符串全部进资源并附中文翻译，资源名带所属包名前缀（`home_*`、`detail_*`、`ui_*` 等）。
- 源码里引用资源要写 `import com.example.globalarch.R`（只有一个 `R`）；与 `multi-module/` 不同，不需要为每个包单独引用。

详细说明见上级目录 `docs/`：

- `docs/architecture.md`：分层、数据流、依赖注入、导航、平台适配
- `docs/mvi-guide.md`：MVI 写法、测试配方、反模式
- `docs/modularization.md`：单模块与多模块对照、迁移步骤、两套工程的差异清单
- `docs/version-baseline.md`：版本基线与 Google Play 合规清单

## 测试

| 层 | 位置 | 工具 |
|:---|:---|:---|
| 领域规则、工具 | `src/test/.../core/domain`、`core/common` | JUnit4 |
| ViewModel | `src/test/.../feature/*`、`MainViewModelTest` | Fake 仓库、`MainDispatcherRule`、Turbine、MockK |
| 仓库、数据源 | `src/test/.../core/data`、`core/datastore`、`core/network` | Fake、`TemporaryFolder`、MockWebServer |
| 包边界 | `src/test/.../architecture` | JUnit4 |
| 数据库 | `src/androidTest/.../core/database` | 内存 Room |
| Compose 界面 | `src/androidTest/.../feature/*`、`core/ui` | `createComposeRule`（v2）+ `testTag` |
| 整体流程 | `src/androidTest/.../MainActivityTest` | `createAndroidComposeRule<MainActivity>`（v2） |

## 去掉示例业务

示例业务（笔记）只用来演示各层怎么配合。要换成自己的业务：

1. 删除 `feature/home|detail|settings` 包及对应资源、测试，或改造它们，同步修改 `AppNavHost`。
2. 替换 `core/model`、`core/domain`、`core/database`、`core/network`、`core/data` 里与笔记有关的类型。
3. 保留 `core/mvi`、`core/common`、`core/designsystem`、`core/ui`、`core/analytics`、`src/test` 里的 `core/testing` 与 `build-logic`，它们与业务无关。
4. 更新 `PackageBoundaryTest` 里的 `ALLOWED_DEPENDENCIES`（删掉的包要同步移除）。
