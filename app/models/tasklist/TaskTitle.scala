/*
 * Copyright 2024 HM Revenue & Customs
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

package models.tasklist

import enumeratum._

sealed abstract class TaskTitle(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName
}

object TaskTitle extends Enum[TaskTitle] with PlayJsonEnum[TaskTitle] {

  val values: IndexedSeq[TaskTitle] = findValues

  // UK Self-Employment
  case object cisDeductions extends TaskTitle("Construction Industry Scheme deductions")

}