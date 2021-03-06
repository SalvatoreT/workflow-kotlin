/*
 * Copyright 2020 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sample.hellobackbutton

import android.os.Parcelable
import com.squareup.sample.container.BackButtonScreen
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.Finished
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.State
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.State.Quitting
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.State.Running
import com.squareup.workflow.RenderContext
import com.squareup.workflow.Snapshot
import com.squareup.workflow.StatefulWorkflow
import com.squareup.workflow.WorkflowAction.Companion.noAction
import com.squareup.workflow.action
import com.squareup.workflow.ui.WorkflowUiExperimentalApi
import com.squareup.workflow.ui.modal.AlertContainerScreen
import com.squareup.workflow.ui.modal.AlertScreen
import com.squareup.workflow.ui.modal.AlertScreen.Button.NEGATIVE
import com.squareup.workflow.ui.modal.AlertScreen.Button.POSITIVE
import com.squareup.workflow.ui.modal.AlertScreen.Event.ButtonClicked
import com.squareup.workflow.ui.modal.AlertScreen.Event.Canceled
import com.squareup.workflow.ui.toParcelable
import com.squareup.workflow.ui.toSnapshot
import kotlinx.android.parcel.Parcelize

/**
 * Wraps [HelloBackButtonWorkflow] to (sometimes) pop a confirmation dialog when the back
 * button is pressed.
 */
@OptIn(WorkflowUiExperimentalApi::class)
object AreYouSureWorkflow : StatefulWorkflow<Unit, State, Finished, AlertContainerScreen<*>>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?
  ): State = snapshot?.toParcelable() ?: Running

  @Parcelize
  enum class State : Parcelable {
    Running,
    Quitting
  }

  object Finished

  override fun render(
    props: Unit,
    state: State,
    context: RenderContext<Unit, State, Finished>
  ): AlertContainerScreen<*> {
    val ableBakerCharlie = context.renderChild(HelloBackButtonWorkflow, Unit) { noAction() }

    return when (state) {
      Running -> {
        AlertContainerScreen(
            BackButtonScreen(ableBakerCharlie) {
              // While we always provide a back button handler, by default the view code
              // associated with BackButtonScreen ignores ours if the view created for the
              // wrapped rendering sets a handler of its own. (Set BackButtonScreen.override
              // to change this precedence.)
              context.actionSink.send(maybeQuit)
            }
        )
      }
      Quitting -> {
        val dialog = AlertScreen(
            buttons = mapOf(
                POSITIVE to "I'm Positive",
                NEGATIVE to "Negatory"
            ),
            message = "Are you sure you want to do this thing?",
            onEvent = { alertEvent ->
              context.actionSink.send(
                  when (alertEvent) {
                    is ButtonClicked -> when (alertEvent.button) {
                      POSITIVE -> confirmQuit
                      else -> cancelQuit
                    }
                    Canceled -> cancelQuit
                  }
              )
            }
        )

        AlertContainerScreen(ableBakerCharlie, dialog)
      }
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()

  private val maybeQuit = action { state = Quitting }
  private val confirmQuit = action { setOutput(Finished) }
  private val cancelQuit = action { state = Running }
}
