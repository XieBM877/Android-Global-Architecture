# 版本基线与合规清单

基线核实日期：2026-10-14。版本号来自 Maven 仓库元数据与各项目官方文档；单一来源是两套工程里完全相同的 `gradle/libs.versions.toml`，升级时只改这一个文件（两套工程保持同步）。

## 1. 工具链

| 项 | 版本 | 说明 |
|:---|:---|:---|
| Gradle | 9.8.1 | AGP 9.4 要求不低于 9.6.0；wrapper 已锁定并带 SHA-256 校验 |
| Android Gradle Plugin | 9.4.1 | 内置 Kotlin，不再应用 `org.jetbrains.kotlin.android`；使用新版 DSL |
| Kotlin | 2.4.21 | Compose 编译器插件与 Kotlin 同版本 |
| KSP | 2.3.12 | 所有注解处理（Hilt、Room、Moshi）均走 KSP，不使用 kapt |
| JDK | 17 | `sourceCompatibility`/`targetCompatibility`/`jvmTarget` 均为 17 |
| compileSdk / targetSdk | 37 | Android 17 |
| minSdk | 26 | Android 8.0；覆盖绝大多数在用设备，且避免大量兼容代码 |

## 2. 依赖

| 类别 | 依赖 | 版本 |
|:---|:---|:---|
| Compose | BOM | 2026.09.00（ui / foundation / runtime 1.12.1，material3 1.4.0，material-icons-core 1.7.8） |
| 导航 | Navigation 3（runtime / ui） | 1.2.0 |
| 自适应 | adaptive-navigation3 | 1.3.0 |
| 生命周期 | lifecycle（viewmodel / runtime-compose / viewmodel-navigation3） | 2.11.0 |
| Activity | activity-compose | 1.13.0 |
| 核心 | core-ktx / core-splashscreen | 1.19.1 / 1.2.0 |
| 依赖注入 | Hilt / androidx.hilt | 2.60.1 / 1.4.0 |
| 数据库 | Room | 2.8.5 |
| 偏好存储 | DataStore Preferences | 1.2.1 |
| 协程 | kotlinx-coroutines | 1.11.0 |
| 序列化 | kotlinx-serialization-core（NavKey 序列化） | 1.11.0 |
| 网络 | Retrofit / OkHttp / Moshi | 3.0.0 / 5.5.0 / 1.15.2 |
| 日志 | Timber | 5.0.1 |
| 安装优化 | profileinstaller | 1.4.1 |
| 测试 | JUnit4 / MockK / Turbine | 4.13.2 / 1.14.11 / 1.2.1 |
| 仪器测试 | androidx.test ext-junit / runner / core / Espresso | 1.3.0 / 1.7.0 / 1.7.0 / 3.7.0 |
| 代码风格 | Spotless / ktlint | 8.10.4 / 1.8.0 |

## 3. 版本耦合关系（升级时必看）

- AGP 与 Gradle：AGP 次版本升级通常抬高 Gradle 最低版本，先看 AGP 发布说明。
- Kotlin 与 KSP：KSP 2.3.x 起版本号独立于 Kotlin，但仍需选择声明支持当前 Kotlin 的版本；升级 Kotlin 时同步检查 KSP、Compose 编译器（同 Kotlin 版本）、Hilt、Room。
- Hilt 与 AGP：Hilt Gradle 插件需要支持当前 AGP 的版本；升级 AGP 后第一时间跑 `assembleDebug`。
- Compose BOM：只改 BOM 版本，各 Compose 库不单独写版本；Navigation 3、adaptive、lifecycle 不在 BOM 内，需要单独维护。
- `build-logic` 使用 `kotlin-dsl`，其内嵌的 Kotlin 版本由 Gradle 决定，可能略低于业务 Kotlin 版本，这是正常的。
- Espresso 与新系统：Compose UI 测试传递依赖的 Espresso 3.5.x 在 Android 17（API 37）上会因反射 `InputManager.getInstance` 失败，所有 Compose 仪器测试整体报 `NoSuchMethodException`；脚手架显式依赖 `espresso-core:3.7.0`。升级目标 API 后若仪器测试整体失败，先看 Espresso 版本。
- Compose 测试规则使用 `androidx.compose.ui.test.junit4.v2` 包（v1 已弃用）。v2 使用 `StandardTestDispatcher`，异步副作用（例如 `rememberSyncedTextFieldState` 把输入同步给 ViewModel）要在断言前 `waitForIdle()` 或 `waitUntil`。
- 构建输出末尾的 `Deprecated Gradle features were used in this build, making it incompatible with Gradle 10` 来自 AGP 9.4.1 内部对 `Configuration.setVisible` 的调用，不是脚手架脚本；AGP 升级后会消失，用 `--warning-mode all --stacktrace` 可复核调用来源。

## 4. Google Play 与平台要求清单

以下条目在脚手架里已满足或有明确落点，发布前逐项复核。

| 要求 | 脚手架做法 |
|:---|:---|
| 新应用和更新的 `targetSdk` 不低于 36（自 2026-08-31 起，可申请延期至 2026-11-01） | 使用 37，由版本目录统一管理 |
| 以 AAB 上架 | `./gradlew bundleRelease` |
| 16 KB 内存页大小 | 不含自带 native 库；依赖带入的 `.so` 已按 16 KB 对齐（已用 `zipalign -c -P 16 -v 4` 与 ELF `p_align` 校验）；升级依赖后重新校验，并在 16 KB 页大小的模拟器上冒烟 |
| 边到边显示 | `enableEdgeToEdge()`，内容通过 `Scaffold` 内边距与 `imePadding()` 处理 |
| 预测性返回 | Manifest 显式开启 `enableOnBackInvokedCallback`，返回统一交给 `NavDisplay` |
| 大屏适配（Android 16 起面向 36+ 的应用不能再靠方向/可缩放限制规避大屏） | 不锁方向，列表-详情使用 `ListDetailSceneStrategy`，表单限制最大内容宽度 |
| 应用语言 | `locales_config.xml` + `values-zh-rCN`，系统“应用语言”可用 |
| 启动页 | 使用 SplashScreen API，不自造启动 Activity |
| 备份与数据提取规则 | `backup_rules.xml`、`data_extraction_rules.xml` |
| 隐私与数据安全 | 默认不采集用户数据；接入统计或网络前，更新 Play 数据安全表单 |
| 代码混淆与压缩 | release 开启 R8 与资源压缩，`proguard-rules.pro` 仅保留必要规则 |

## 5. 每季度检查

1. Google Play 的 target API 要求与截止日期。
2. AGP、Gradle、Kotlin、KSP、Compose BOM、Navigation 3、Hilt、Room 的稳定版发布。
3. `lintDebug` 与 `testDebugUnitTest` 全绿后再合并版本升级。
4. 在 Android 最新预览/稳定版模拟器上跑一遍冒烟流程：新建笔记、列表、详情、设置切换主题、旋转、折叠/大屏。
