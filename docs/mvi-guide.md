# MVI 约定与写法

本脚手架的所有页面都遵循同一套 MVI 约定。单模块与多模块两种工程形态的包名、类名、写法完全一致，区别只在代码放在哪个 Gradle 模块里。

## 1. 铁律

1. ViewModel 对外只有一个入口：`dispatch(intent)`。UI 不得调用 ViewModel 的其它业务方法。
2. 一个页面只有一个不可变 `State`（`data class`，字段全部 `val`），UI 只读它。
3. 一次性事件（导航、Snackbar、Toast）用 `Effect`，通过 `Channel(Channel.BUFFERED)` + `receiveAsFlow()` 暴露，不放进 `State`。
4. `reduce` 必须是纯函数：只依赖旧 State 和入参，不做 IO，不读时钟，不读随机数。
5. 派生状态使用 `combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`；禁止 `Eagerly`，禁止不带超时的 `WhileSubscribed()`。
6. 页面拆成有状态的 `Route` 和无状态的 `Screen`：`Route` 负责取 ViewModel、收集 State/Effect；`Screen` 只接收 `state` 与 `onIntent`，因此可以 `@Preview`、可以直接做 Compose UI 测试。
7. 只读页面或没有一次性事件的页面，用 `Nothing` 作为 `Intent` / `Effect` 的类型参数，不造空的密封类。

## 2. 基础设施（`core:mvi`）

```kotlin
interface UiState
interface UiIntent
interface UiEffect

abstract class MviViewModel<S : UiState, in I : UiIntent, E : UiEffect> : ViewModel() {
    abstract val state: StateFlow<S>
    val effects: Flow<E>
    fun dispatch(intent: I)
    protected abstract fun handle(intent: I)
    protected fun sendEffect(effect: E)
}

abstract class SelfOwnedMviViewModel<S : UiState, in I : UiIntent, E : UiEffect>(
    initialState: S,
) : MviViewModel<S, I, E>() {
    protected val currentState: S
    protected fun reduce(transform: (S) -> S)
}
```

- `MviViewModel` 只规定契约：`state`、`effects`、`dispatch`、`handle`。
- `SelfOwnedMviViewModel` 是“自持有状态”的便捷基类，内部用 `MutableStateFlow.update {}` 实现 `reduce`。
- Intent 类型参数声明为 `in I`（只被消费，逆变）；只读页面可以直接写成 `MviViewModel<S, Nothing, Nothing>`，见 `SettingsViewModel`（Intent 有、Effect 为 `Nothing`）。

## 3. 两种状态模式

| 模式 | 适用 | 基类 | 示例 |
|:---|:---|:---|:---|
| 自持有 | 表单、编辑页，状态由用户输入驱动 | `SelfOwnedMviViewModel` | `DetailViewModel` |
| 派生 | 列表、设置页，状态由仓库数据流派生 | `MviViewModel`（自己覆盖 `state`） | `HomeViewModel`、`SettingsViewModel`、`MainViewModel` |

### 3.1 自持有（`DetailViewModel`）

```kotlin
override fun handle(intent: DetailIntent) {
    when (intent) {
        is DetailIntent.TitleChanged -> reduce { it.copy(title = intent.value, titleError = null) }
        is DetailIntent.ContentChanged -> reduce { it.copy(content = intent.value, contentError = null) }
        DetailIntent.Save -> save()
        DetailIntent.Delete -> delete()
    }
}
```

- 同步更新用 `reduce`；异步工作放进 `viewModelScope.launch`，结果再 `reduce` 或 `sendEffect`。
- 重复点击保护写在 ViewModel 里（`save()` 先判断 `isSaving`），不依赖 UI 防抖。

### 3.2 派生（`HomeViewModel`）

```kotlin
override val state: StateFlow<HomeState> = combine(
    notesRepository.observeNotes(),
    isRefreshing,
) { notes, refreshing ->
    HomeState(notes = notes.map(Note::toItem), isLoading = false, isRefreshing = refreshing)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
    initialValue = HomeState(),
)
```

- 数据库是事实源，列表由 Room 的 `Flow` 驱动；`isRefreshing` 这类瞬时标记用私有 `MutableStateFlow` 参与 `combine`。
- 5 秒超时覆盖屏幕旋转，页面不可见后停止上游订阅。
- 单元测试里需要有人订阅才会计算，使用 `core:testing` 的 `keepCollecting(flow)`。

## 4. Route 与 Screen

```kotlin
@Composable
fun HomeRoute(
    onOpenNote: (Long) -> Unit,
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    selectedNoteId: Long? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect -> /* 导航 / Snackbar */ }
    HomeScreen(state = state, onIntent = viewModel::dispatch, /* ... */)
}
```

- 状态收集一律 `collectAsStateWithLifecycle()`。
- Effect 收集一律 `CollectEffects(...)`：内部按 `STARTED` 生命周期重启收集，并在 `Dispatchers.Main.immediate` 上消费，避免页面在后台时丢失或重复处理事件。
- Snackbar 的 `showSnackbar` 会挂起，Route 里用 `rememberCoroutineScope().launch { ... }` 触发，不要在 `CollectEffects` 回调里直接挂起，否则会阻塞后续 Effect 的消费。
- Route 不依赖具体导航库：跳转以回调参数传入，由 `app` 模块注入（见 `docs/architecture.md` 导航一节）。
- 需要参数的 ViewModel 使用 Hilt 辅助注入：`@HiltViewModel(assistedFactory = ...)` + `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(...) })`，见 `DetailViewModel` / `NoteDetailRoute`。

## 5. 文本输入

文本框的内容使用 Compose 的 `TextFieldState`，由界面层持有，再单向同步给 ViewModel：

```kotlin
val titleField = rememberSyncedTextFieldState(
    value = state.title,
    onValueChange = { onIntent(DetailIntent.TitleChanged(it)) },
)
OutlinedTextField(state = titleField, /* ... */)
```

原因：把 `StateFlow` 经 `collectAsStateWithLifecycle()` 再回灌给基于 `value` 的 `TextField`，状态更新是异步的，输入法组合态（中文拼音、日文假名等）下容易出现光标跳动、文字重复。`TextFieldState` 在界面层同步更新，输入法不受影响。

`rememberSyncedTextFieldState` 的行为：

- 初始文本取自 `value`，不会触发回调；
- 用户输入产生的文本与 `value` 不同时，才回调 `onValueChange`；
- 旋转后 `TextFieldState` 由 Compose 自动恢复，文本与 ViewModel 一致时不会重复发 Intent（因此不会误清除校验错误）；
- 进程被回收后，界面层恢复了文本而 ViewModel 是新实例，两者不一致，会自动补发一次 Intent 完成对齐。

ViewModel 仍然是业务事实源：校验、保存都读 `State` 里的字符串，文本框只负责输入。

## 6. 校验与错误

- 领域层用类型化异常表达业务规则失败，例如 `NoteValidationException(reason)`；ViewModel 把它映射成 State 里的字段错误（`titleError`、`contentError`）。
- 字段错误和提示文案使用 `UiText`（`Plain` 或 `Resource(@StringRes, args)`），ViewModel 不持有 `Context`，Compose 里 `error.asString()` 在界面层解析成字符串，因此支持多语言与系统语言切换。
- 非业务错误（网络、数据库）用 `ShowMessage(UiText)` 这类 Effect 通知界面，不吞异常，也不在 State 里堆 `Throwable`。
- 协程里统一用 `suspendRunCatching`（`core:common`）：它会重新抛出 `CancellationException`，不会把取消误当成失败。

## 7. 新增一个页面的步骤

1. 在 `feature/<name>/` 下新建 `<Name>Contract.kt`：`State`、`Intent`、`Effect`。
2. 新建 `<Name>ViewModel.kt`，选择自持有或派生基类，实现 `handle`。
3. 新建 `<Name>Route.kt`（取 ViewModel、收集 State/Effect）与 `<Name>Screen.kt`（无状态界面，带 `@Preview`、`testTag`）。
4. 新建 `navigation/<Name>Navigation.kt`：`@Serializable` 的 `NavKey` 与 `EntryProviderScope<NavKey>.<name>Entry(...)`。
5. 在 `app` 的 `AppNavHost` 注册入口并注入跨页面跳转回调。
6. 字符串放进该 feature 自己的 `res/values/strings.xml`，并同步 `values-zh-rCN`；资源名以模块名为前缀。
7. 补测试：ViewModel 单元测试（Fake 仓库 + `MainDispatcherRule`），Screen 的 Compose UI 测试。

## 8. 测试配方

| 对象 | 工具 | 要点 |
|:---|:---|:---|
| 自持有 ViewModel | JUnit4 + `MainDispatcherRule` + Fake 仓库 | 先 `dispatch`，再断言 `state.value` |
| 派生 ViewModel | 同上 + `keepCollecting(state)` | `stateIn(WhileSubscribed)` 需要订阅者 |
| Effect | Turbine：`viewModel.effects.test { ... }` | 先订阅再 `dispatch` |
| 外部 SDK 封装 | MockK | 只对 `AnalyticsTracker` 这类接口用 Mock，仓库用 Fake |
| Compose 界面 | `androidx.compose.ui.test.junit4.v2.createComposeRule` + `testTag` | 测无状态 `Screen`，传入构造好的 State，断言回调的 Intent；v1 的同名函数已弃用 |
| 整体流程 | `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<MainActivity>` | 少量冒烟用例即可；v2 用 `StandardTestDispatcher`，异步结果用 `waitUntil` 等待 |

## 9. 反模式

- 把导航、Toast 做成 State 里的布尔标记，再靠“消费后置空”清除。
- 在 `Screen` 里直接调用 ViewModel 或仓库。
- 在 `reduce` 里做网络请求、读取系统时间。
- 每个页面写一个只转发仓库调用的 UseCase；没有规则、编排、聚合逻辑时直接用仓库。
- 在 ViewModel 里持有 `Context`、`Activity`、`NavController`。
- 用 `SharingStarted.Eagerly` 或不带超时的 `WhileSubscribed()`。
- 在 `CollectEffects` 回调里直接 `snackbarHostState.showSnackbar(...)`（会阻塞后续 Effect）。
