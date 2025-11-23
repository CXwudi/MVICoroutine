package mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineStoreTest {

  sealed interface CounterIntent {
    data object Increment : CounterIntent
  }

  sealed interface CounterAction {
    data object Init : CounterAction
  }

  sealed interface CounterMessage {
    data object Incremented : CounterMessage
    data class Set(val value: Int) : CounterMessage
  }

  data class CounterState(val value: Int = 0, val initialized: Boolean = false)

  sealed interface CounterLabel

  private val reducer = Reducer<CounterState, CounterMessage> { state, msg ->
    when (msg) {
      CounterMessage.Incremented -> state.copy(value = state.value + 1)
      is CounterMessage.Set -> state.copy(value = msg.value, initialized = true)
    }
  }

  private class TestExecutor :
    Executor<CounterIntent, CounterAction, CounterState, CounterMessage, CounterLabel> {
    override suspend fun executeIntent(
      intent: CounterIntent,
      scope: ExecutorScope<CounterIntent, CounterAction, CounterState, CounterMessage, CounterLabel>
    ) {
      when (intent) {
        CounterIntent.Increment -> scope.dispatch(CounterMessage.Incremented)
      }
    }

    override suspend fun executeAction(
      action: CounterAction,
      scope: ExecutorScope<CounterIntent, CounterAction, CounterState, CounterMessage, CounterLabel>
    ) {
      when (action) {
        CounterAction.Init -> scope.dispatch(CounterMessage.Set(10))
      }
    }
  }

  private class TestBootstrapper : Bootstrapper<CounterAction> {
    override suspend fun bootstrap(dispatchAction: suspend (CounterAction) -> Unit) {
      dispatchAction(CounterAction.Init)
    }
  }

  @Test
  fun testSynchronousStateUpdate() = runTest {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    val store = CoroutineStore(
      initialState = CounterState(),
      bootstrapper = null,
      executorFactory = { TestExecutor() },
      reducer = reducer,
      dispatcher = dispatcher
    )

    assertEquals(0, store.state.value.value)
    store.sendIntent(CounterIntent.Increment)
    assertEquals(1, store.state.value.value)
    store.dispose()
  }

  @Test
  fun testAutoInit() = runTest {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    val store = CoroutineStore(
      initialState = CounterState(),
      bootstrapper = TestBootstrapper(),
      executorFactory = { TestExecutor() },
      reducer = reducer,
      dispatcher = dispatcher,
      autoInit = true
    )

    // With UnconfinedTestDispatcher, launch happens immediately if possible,
    // but bootstrapper runs in a separate launch { }.
    // However, UnconfinedTestDispatcher should execute it eagerly.
    // Let's verify.

    assertEquals(10, store.state.value.value)
    assertTrue(store.state.value.initialized)
    store.dispose()
  }

  @Test
  fun testManualInit() = runTest {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    val store = CoroutineStore(
      initialState = CounterState(),
      bootstrapper = TestBootstrapper(),
      executorFactory = { TestExecutor() },
      reducer = reducer,
      dispatcher = dispatcher,
      autoInit = false
    )

    assertEquals(0, store.state.value.value)

    store.init()

    assertEquals(10, store.state.value.value)
    store.dispose()
  }
}
