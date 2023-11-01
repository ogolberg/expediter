/*
 * Copyright (c) 2023 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.expediter.ignore

import com.toasttab.expediter.issue.Issue
import java.io.Serializable

interface Ignore : Serializable {
    fun ignore(issue: Issue): Boolean

    companion object {
        val NOTHING: Ignore = object : Ignore {
            override fun ignore(issue: Issue) = false
        }
    }

    class Not(
        private val ignore: Ignore
    ) : Ignore {
        override fun ignore(issue: Issue) = !ignore.ignore(issue)
    }

    class And(
        private vararg val ignores: Ignore
    ) : Ignore {
        override fun ignore(issue: Issue) = ignores.all { it.ignore(issue) }
    }

    class Or(
        private vararg val ignores: Ignore
    ) : Ignore {
        override fun ignore(issue: Issue) = ignores.any { it.ignore(issue) }
    }

    object IsConstructor : Ignore {
        override fun ignore(issue: Issue) = issue is Issue.WithMemberAccess && issue.member.ref.run {
            isConstructor()
        }
    }

    class TargetStartsWith(
        private vararg val partial: String
    ) : Ignore {
        override fun ignore(issue: Issue) = partial.any { issue.target?.run { startsWith(it) } ?: false }
    }

    class CallerStartsWith(
        private vararg val partial: String
    ) : Ignore {
        override fun ignore(issue: Issue) = issue.caller?.run { partial.any { this.startsWith(it) } } ?: false
    }

    class Signature(
        private val signature: String
    ) : Ignore {
        override fun ignore(issue: Issue) =
            issue is Issue.WithMemberAccess && issue.member.ref.signature == signature

        companion object {
            val IS_BLANK = Signature("()V")
        }
    }
}
