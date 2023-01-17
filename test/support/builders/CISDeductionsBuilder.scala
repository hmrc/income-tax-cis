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

package support.builders

import models.get.CISDeductions
import support.builders.CISSubmissionBuilder.aCISSubmission
import support.builders.GetPeriodDataBuilder.aGetPeriodData
import support.utils.TaxYearUtils.{taxYear, taxYearEOY}

object CISDeductionsBuilder {

  val aCISDeductions: CISDeductions = CISDeductions(
    fromDate = s"$taxYearEOY-04-06",
    toDate = s"$taxYear-04-05",
    contractorName = aCISSubmission.contractorName,
    employerRef = aCISSubmission.employerRef.get,
    totalDeductionAmount = Some(200.00),
    totalCostOfMaterials = Some(300.00),
    totalGrossAmountPaid = Some(400.00),
    periodData = Seq(aGetPeriodData)
  )
}
