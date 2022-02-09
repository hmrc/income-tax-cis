/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import play.api.libs.json.{Json, OFormat}
import utils.CISTaxYearHelper.cisTaxYearConverter

case class CreateCISDeductionsModel(
                                        employerRef: String,
                                        contractorName: String,
                                        periodData: Seq[PeriodData]
                                      ) {
  def toApiModel(taxYear: Int): CreateCISDeductionsApiModel = {
    val cisTaxYear = cisTaxYearConverter(taxYear)
    CreateCISDeductionsApiModel(
      employerRef,
      contractorName,
      cisTaxYear.fromDate,
      cisTaxYear.toDate,
      periodData
    )
  }
}

object CreateCISDeductionsModel {
  implicit val format: OFormat[CreateCISDeductionsModel] = Json.format[CreateCISDeductionsModel]
}

case class CreateCISDeductionsApiModel(
                              employerRef: String,
                              contractorName: String,
                              fromDate: String,
                              toDate: String,
                              periodData: Seq[PeriodData]
                              )

object CreateCISDeductionsApiModel {
  implicit val format: OFormat[CreateCISDeductionsApiModel] = Json.format[CreateCISDeductionsApiModel]
}

case class CreateCISDeductionsSuccessModel(
                                submissionId: String
                              )

object CreateCISDeductionsSuccessModel {
  implicit val format: OFormat[CreateCISDeductionsSuccessModel] = Json.format[CreateCISDeductionsSuccessModel]
}
