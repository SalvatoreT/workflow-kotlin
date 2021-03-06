/*
 * Copyright 2017 Square Inc.
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
package com.squareup.sample.gameworkflow

import com.squareup.sample.tictactoe.databinding.NewGameLayoutBinding
import com.squareup.workflow.ui.WorkflowUiExperimentalApi
import com.squareup.workflow.ui.LayoutRunner
import com.squareup.workflow.ui.ViewFactory
import com.squareup.workflow.ui.backPressedHandler

@OptIn(WorkflowUiExperimentalApi::class)
internal val NewGameViewFactory: ViewFactory<NewGameScreen> =
  LayoutRunner.bind(NewGameLayoutBinding::inflate) { rendering, _ ->
    if (playerX.text.isBlank()) playerX.setText(rendering.defaultNameX)
    if (playerO.text.isBlank()) playerO.setText(rendering.defaultNameO)

    startGame.setOnClickListener {
      rendering.onStartGame(playerX.text.toString(), playerO.text.toString())
    }

    root.backPressedHandler = { rendering.onCancel() }
  }
