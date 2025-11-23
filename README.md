# MVICoroutine

MVIKotlin Clone but coroutine-first

## PoC Result

Kind of success, tho we didn't go with coroutine-first approach eventually, because with coroutine we can't garantee that state is updated by the time when `sendIntent()` returns.

Eventually, we still end up with just normal function, no `suspend`. But we save a `coroutineScope` in the store and expose it to `Executor` and `Bootstrapper`, so that user can still run suspend functions inside. However, doing so break the garantee that state is updated when `sendIntent()` returns.
