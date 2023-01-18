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

import common.CISSource.CONTRACTOR
import models.get.AllCISDeductions
import support.builders.CISDeductionsBuilder.aCISDeductions
import support.builders.CISSourceBuilder.aCISSource
import support.builders.GetPeriodDataBuilder.aGetPeriodData

object AllCISDeductionsBuilder {

  val anAllCISDeductions: AllCISDeductions = AllCISDeductions(
    customerCISDeductions = Some(aCISSource),
    contractorCISDeductions = Some(aCISSource.copy(cisDeductions = Seq(aCISDeductions.copy(periodData = Seq(aGetPeriodData.copy(source = CONTRACTOR))))))
  )
}
