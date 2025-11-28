package mvi2

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Store<Intent, State, Label> {
  val state: StateFlow<State>
  val labels: Flow<Label>
  /**
   * Process an Intent.
   *
   * When this method returns, all messages/actions synchronously
   * dispatched from this intent (and their chains) have already
   * produced their state updates.
   *
   * However, additional updates from async work launched by coroutine scope
   * may arrive later.
   *
   * Noted, it is better to call this method from the UI thread.
   */
  fun sendIntent(intent: Intent)
  /**
   * Starts the store manually.
   *
   * This will call `init()` first on Executor, then on the Bootstrapper (if present),
   * and call `bootstrap()` on the Bootstrapper.
   *
   * Automatically called in the constructor if `autoInit` is `true`.
   */
  fun init()
  /**
   * Call `dispose()` on the Bootstrapper (if present) and Executor.
   */
  fun dispose()
}

/**
 * A pure function to update the state given a message.
 */
fun interface Reducer<State, Message> {
  fun reduce(state: State, message: Message): State
}
/**
 * Scope available to the Bootstrapper.
 *
 * Currently only allows dispatching Actions.
 */
interface BootstrapperScope<Action> {
  fun dispatch(action: Action)
}

fun interface Bootstrapper<Action> {
  fun BootstrapperScope<Action>.bootstrap()

  fun init() {}
  fun dispose() {}
}

/**
 * Scope available to the Executor during processing.
 * Lets the Executor:
 *  - read current state
 *  - dispatch Messages (state changes)
 *  - publish Labels (one-off events)
 *  - dispatch Actions (chained internal pipelines)
 */
interface ExecutorScope<Action, Message, State, Label> {
  /**
   * Read-only state.
   */
  fun state(): State

  fun dispatch(message: Message)
  fun tryPublish(label: Label): Boolean
  fun dispatchAction(action: Action)
}

/**
 * Executor handles both Intents and Actions.
 */
interface Executor<Intent, Action, Message, State, Label> {
  fun ExecutorScope<Action, Message, State, Label>.executeIntent(intent: Intent)
  fun ExecutorScope<Action, Message, State, Label>.executeAction(action: Action)

  fun init() {}
  fun dispose() {}
}