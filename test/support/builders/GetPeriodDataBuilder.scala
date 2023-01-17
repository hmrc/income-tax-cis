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

import common.CISSource.CUSTOMER
import models.get.GetPeriodData
import support.utils.TaxYearUtils.taxYearEOY

object GetPeriodDataBuilder {

  val aGetPeriodData: GetPeriodData = GetPeriodData(
    deductionFromDate = s"$taxYearEOY-04-06",
    deductionToDate = s"$taxYearEOY-05-05",
    deductionAmount = Some(100.00),
    costOfMaterials = Some(200.00),
    grossAmountPaid = Some(300.00),
    submissionDate = "2022-05-11T16:38:57.489Z",
    submissionId = Some("4557ecb5-fd32-48cc-81f5-e6acd1099f3c"),
    source = CUSTOMER
  )
}
