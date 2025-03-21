/*
 * Copyright 2023 HM Revenue & Customs
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

package models.get

import play.api.libs.json.{Json, OFormat}

case class GetPeriodData(deductionFromDate: String,
                         deductionToDate: String,
                         deductionAmount: Option[BigDecimal],
                         costOfMaterials: Option[BigDecimal],
                         grossAmountPaid: Option[BigDecimal],
                         submissionDate: String,
                         submissionId: Option[String],
                         source: String) {
  val zero: Option[BigDecimal] = Some(BigDecimal(0))

  val isEmpty: Boolean = this match {
    case GetPeriodData(_, _, `zero`, `zero`, `zero`, _, _, _) => true
    case GetPeriodData(_, _, None, None, None, _, _, _) => true
    case _ => false
  }
}

object GetPeriodData {
  implicit val format: OFormat[GetPeriodData] = Json.format[GetPeriodData]
}
