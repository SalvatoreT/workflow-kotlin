/*
 * Copyright 2019 Square Inc.
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
package com.squareup.workflow.internal

import com.squareup.workflow.ExperimentalWorkflowApi
import com.squareup.workflow.Workflow
import com.squareup.workflow.WorkflowIdentifier
import com.squareup.workflow.identifier
import com.squareup.workflow.readByteStringWithLength
import com.squareup.workflow.readUtf8WithLength
import com.squareup.workflow.writeByteStringWithLength
import com.squareup.workflow.writeUtf8WithLength
import okio.Buffer
import okio.ByteString

/**
 * Value type that can be used to distinguish between different workflows of different types or
 * the same type (in that case using a [name]).
 */
@OptIn(ExperimentalWorkflowApi::class)
internal data class WorkflowNodeId(
  internal val identifier: WorkflowIdentifier,
  internal val name: String = ""
) {
  constructor(
    workflow: Workflow<*, *, *>,
    name: String = ""
  ) : this(workflow.identifier, name)

  fun matches(
    otherWorkflow: Workflow<*, *, *>,
    otherName: String
  ): Boolean = identifier == otherWorkflow.identifier && name == otherName

  internal fun toByteStringOrNull(): ByteString? {
    // If identifier is not snapshottable, neither are we.
    val identifierBytes = identifier.toByteStringOrNull() ?: return null
    return Buffer().let { sink ->
      sink.writeByteStringWithLength(identifierBytes)
      sink.writeUtf8WithLength(name)
      sink.readByteString()
    }
  }

  internal companion object {
    fun parse(bytes: ByteString): WorkflowNodeId = Buffer().let { source ->
      source.write(bytes)

      val identifierBytes = source.readByteStringWithLength()
      val identifier = WorkflowIdentifier.parse(identifierBytes)
          ?: throw ClassCastException("Invalid WorkflowIdentifier in ByteString")
      val name = source.readUtf8WithLength()
      return WorkflowNodeId(identifier, name)
    }
  }
}

internal fun <W : Workflow<I, O, R>, I, O, R>
    W.id(key: String = ""): WorkflowNodeId = WorkflowNodeId(this, key)
