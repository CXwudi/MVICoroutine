package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import poc.cx.getFibonacciNumbers
import sample.app.store.CounterIntent
import sample.app.store.createCounterStore

@Composable
fun App() {
  MaterialTheme {
    Surface(
      modifier =
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
      AppContent()
    }
  }

}

@Composable
fun AppContent() {

  val scope = rememberCoroutineScope()
  val store = remember { createCounterStore(scope) }
  val state by store.state.collectAsState()

  Box(
    modifier = Modifier.fillMaxSize().background(Color.White),
    contentAlignment = Alignment.Center
  ) {
    Column {
      BasicText("getFibonacciNumbers(7)=${getFibonacciNumbers(7).joinToString(", ")}")
      BasicText("store.state = ${state}")
      Row {
        Button(onClick = { store.sendIntent(CounterIntent.Increment) }) {
          BasicText("Increment")
        }
        Button(onClick = { store.sendIntent(CounterIntent.Decrement) }) {
          BasicText("Decrement")
        }
      }

    }
  }
}