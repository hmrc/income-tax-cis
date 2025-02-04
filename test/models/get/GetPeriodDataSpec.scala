/*
 * Copyright 2025 HM Revenue & Customs
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

import support.UnitTest

class GetPeriodDataSpec extends UnitTest {

  "GetPeriodData" when {
    "isEmpty" should {
      val nonEmptyPeriodData: GetPeriodData = GetPeriodData("", "", None, None, Some(1), "", None, "")
      val emptyPeriodData: GetPeriodData = nonEmptyPeriodData.copy(grossAmountPaid = None)
      "return 'false' when any data is defined" in {
        nonEmptyPeriodData.isEmpty shouldBe false
      }

      "return 'true' when there is no data" in {
        emptyPeriodData.isEmpty shouldBe true
      }

      "return 'true' when data matches zeroed response" in {
        GetPeriodData("", "", Some(0), Some(0), Some(0), "", None, "").isEmpty shouldBe true
      }
    }
  }
}
