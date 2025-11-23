package sample.app.store

import kotlinx.coroutines.delay
import mvi.Bootstrapper
import mvi.Executor
import mvi.ExecutorScope
import mvi.Reducer


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

data class CounterState(val value: Int = -1, val initialized: Boolean = false)

sealed interface CounterLabel

class CounterReducer: Reducer<CounterState, CounterMessage> {
  override fun reduce(state: CounterState, msg: CounterMessage): CounterState {
    return when (msg) {
      CounterMessage.Incremented -> state.copy(value = state.value + 1)
      is CounterMessage.Set -> state.copy(value = msg.value, initialized = true)
    }
  }

}

class CounterExecutor :
  Executor<CounterIntent, CounterAction, CounterState, CounterMessage, CounterLabel> {
  override suspend fun ExecutorScope<CounterIntent, CounterAction, CounterState, CounterMessage, CounterLabel>.executeIntent(
    intent: CounterIntent
  ) {
    println("executeIntent: $intent")
//    delay(200) // test out do we really guarantee only one at a time?
    when (intent) {
      CounterIntent.Increment -> dispatch(CounterMessage.Incremented)
    }
  }

  override suspend fun ExecutorScope<CounterIntent, CounterAction, CounterState, CounterMessage, CounterLabel>.executeAction(
    action: CounterAction
  ) {
    println("executeAction: $action")
    delay(2000) // test out do we really guarantee only one at a time?
    when (action) {
      CounterAction.Init -> dispatch(CounterMessage.Set(10))
    }
  }
}

class CounterBootstrapper : Bootstrapper<CounterAction> {
  override suspend fun bootstrap(dispatchAction: suspend (CounterAction) -> Unit) {
    dispatchAction(CounterAction.Init)
  }
}
