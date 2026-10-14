# Android-Global-Architecture

Android 通用脚手架：MVI + Jetpack Compose + Jetpack，单 Activity、纯 Compose 页面、不使用 Fragment。版本基线按 Google 2026-10 的最新要求（compileSdk / targetSdk 37、AGP 9.4、Kotlin 2.4、Navigation 3、16 KB 页面对齐、edge-to-edge、预测性返回、自适应大屏）。

提供两套功能完全相同、可以各自独立拷贝的工程：

| | `single-module/` | `multi-module/` |
|:---|:---|:---|
| 模块 | `app` + `build-logic` | `app` + `core:*`（12 个）+ `feature:*`（3 个）+ `build-logic` |
| 适合 | 个人项目、原型、小团队，先跑起来 | 多人并行开发、需要模块级增量构建与编译期边界 |
| 边界约束 | `PackageBoundaryTest` 扫描 import | Gradle 依赖，越界无法编译 |
| 包结构 | `com.example.globalarch.{core,feature}.<名>` | 与单模块完全相同 |
| MVI 约定 | 相同 | 相同 |
| 单模块 → 多模块 | 按 `docs/modularization.md` 逐包拆出，包名不变 | 反向合并同样可行 |

不确定选哪套时先用 `single-module/`：包名与多模块一致，拆分时只是把目录搬进新模块并补一个 `build.gradle.kts`。

## 快速开始

```bash
cp -R single-module ~/work/MyApp
python3 tools/rename-project.py ~/work/MyApp --package com.acme.notes --name "Acme Notes"
cd ~/work/MyApp
./gradlew spotlessApply assembleDebug testDebugUnitTest
```

`rename-project.py` 同时改包名、应用名、主题名、Gradle 根工程名、Room schema 目录与 `GlobalArchApplication` 类名，只处理文本文件与目录名，不碰构建产物。包名变化会改变 import 的字母序，所以改名后先跑一次 `spotlessApply`。参数 `--id` 可单独指定标识符（默认由应用名去掉空格生成）。

## 目录

```text
Android-Global-Architecture/
├── single-module/     单模块工程（README 里有结构、命令、构建属性）
├── multi-module/      多模块工程
├── docs/              设计文档
└── tools/rename-project.py
```

## 文档

| 文档 | 内容 |
|:---|:---|
| `docs/architecture.md` | 分层与依赖方向、数据流、依赖注入、导航、平台适配、构建体系 |
| `docs/mvi-guide.md` | MVI 铁律、Route 与 Screen、状态与 Effect 写法、测试配方、反模式 |
| `docs/modularization.md` | 单模块与多模块对照、依赖规则、迁移步骤、两套工程差异清单 |
| `docs/version-baseline.md` | 版本基线、版本耦合关系、Google Play 合规清单 |

## 技术栈

| 类别 | 选型 |
|:---|:---|
| 界面 | Jetpack Compose（BOM 2026.09.00）、Material 3、自适应列表-详情 |
| 架构 | MVI + Clean Architecture，离线优先（Room 为唯一数据源） |
| 导航 | Navigation 3，类型安全 `NavKey` |
| 注入 | Hilt（只用 KSP），`@AssistedInject` 传导航参数 |
| 数据 | Room、DataStore Preferences、Retrofit + OkHttp + Moshi（地址为空时网络层是空实现） |
| 构建 | AGP 9.4.1 内置 Kotlin、版本目录、`build-logic` 约定插件、配置缓存、R8、Spotless + ktlint |
| 平台 | edge-to-edge、预测性返回、SplashScreen、按应用语言（英文与简体中文）、16 KB 页面对齐 |
| 测试 | JUnit4、MockK、Turbine、Fake 仓库、Compose UI 测试（v2 API）、Room 内存库、MockWebServer |

## 与团队规则的关系

脚手架的 `compileSdk` 与 `targetSdk` 是 37，`minSdk` 是 26，来自 Google Play 对新上架与更新应用的 2026-08-31 起 targetSdk 不低于 36 的要求，并按最新稳定平台取值。若所在仓库的工作流规则里固定了更低的 SDK 版本，以版本目录里的 `compileSdk`、`targetSdk`、`minSdk` 三项为准手动调整即可，其余依赖版本不受影响。

测试框架选 JUnit4 + MockK + Turbine：AGP 对 JUnit4 原生支持，仪器测试的 `AndroidJUnitRunner` 也基于 JUnit4，JUnit5 需要额外的社区 Gradle 插件。若团队规则要求 JUnit5 或 Maestro，按规则自行替换，不影响架构分层。

## 验证记录

验证日期 2026-10-14，环境 JDK 17、Gradle 9.8.1、Pixel_9a 模拟器（Android 17，API 37，16 KB 页面）。

| 项目 | single-module | multi-module |
|:---|:---|:---|
| `spotlessCheck assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug assembleRelease` | 通过，Lint 无问题 | 通过 |
| `-PwarningsAsErrors=true -PenableComposeCompilerReports=true -PapiBaseUrl=...` 下的同类门禁 | 通过，BuildConfig 与 Compose 报告均生效 | 通过 |
| 单元测试 | 61 个全部通过（含 8 个包边界测试） | 53 个全部通过 |
| 仪器测试 `connectedDebugAndroidTest`（Android 17 / API 37 / 16 KB 页面） | 29 个全部通过 | 29 个全部通过 |
| debug 包运行冒烟（14 项） | 14 / 14 | 14 / 14 |
| release（R8）包运行冒烟（14 项） | 14 / 14 | 14 / 14 |
| release 包大小、16 KB 对齐（`zipalign -c -P 16 -v 4`） | 约 1.9 MB，通过 | 约 1.9 MB，通过 |
| `rename-project.py` 改名后 `spotlessCheck assembleDebug testDebugUnitTest lintDebug` | 改为 `com.acme.notes` 通过 | 改为 `org.acme.notepad.app` 并指定 `--id AcmePad` 通过 |

运行冒烟用 `adb` 与 `uiautomator` 驱动，覆盖：空态、标题校验、新增、编辑、列表删除、详情删除、主题切换（浅色、深色、跟随系统）、重启后主题保持、旋转保留输入、1600x2560 大屏的列表-详情双栏、进程被杀后恢复返回栈、返回键退出，并检查 logcat 无崩溃。release 包用调试密钥签名后安装，以确认 R8 没有破坏 Moshi、Retrofit、Hilt、Room 与 Navigation 3 的序列化。

源码零注释：用仓库的 `scan-source-comments.sh --any` 扫描 202 个 `.kt` 与 `.kts` 文件，无命中。

未覆盖：真机、正式发布密钥的签名与上架流程、持续集成配置文件；`apiBaseUrl` 只验证了注入 `BuildConfig`，网络层行为由 MockWebServer 单元测试覆盖。
