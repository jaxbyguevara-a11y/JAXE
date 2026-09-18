package com.jaxia.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps `Dispatchers.Main` for a test dispatcher.
 *
 * `viewModelScope` is hardwired to `Dispatchers.Main`, which has no
 * implementation in a plain JVM unit test — without this rule every ViewModel
 * test fails with "Module with the Main dispatcher had failed to initialize".
 *
 * [UnconfinedTestDispatcher] runs launched coroutines eagerly, so the `collect`
 * in the ViewModel's `init` has already consumed the first emission by the time
 * the constructor returns. That keeps the tests free of manual scheduler
 * advancing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: kotlinx.coroutines.test.TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
