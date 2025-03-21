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

case class CISDeductions(fromDate: String,
                         toDate: String,
                         contractorName: Option[String],
                         employerRef: String,
                         totalDeductionAmount: Option[BigDecimal],
                         totalCostOfMaterials: Option[BigDecimal],
                         totalGrossAmountPaid: Option[BigDecimal],
                         periodData: Seq[GetPeriodData]) {
  private val isPeriodDataEmpty: Boolean = periodData.forall(_.isEmpty)

  private val zero: Option[BigDecimal] = Some(BigDecimal(0))

  val isEmpty: Boolean = this match {
    case CISDeductions(_, _, _, _, `zero`, `zero`, `zero`, _) if isPeriodDataEmpty => true
    case CISDeductions(_, _, _, _, None, None, None, _) if isPeriodDataEmpty => true
    case _ => false
  }
}

object CISDeductions {
  implicit val format: OFormat[CISDeductions] = Json.format[CISDeductions]
}
