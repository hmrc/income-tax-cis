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

class CISDeductionsSpec extends UnitTest {

  "CISDeductions" when {
    "isEmpty" should {
      val nonEmptyPeriodData: GetPeriodData = GetPeriodData("", "", None, None, Some(1), "", None, "")
      val emptyPeriodData: GetPeriodData = nonEmptyPeriodData.copy(grossAmountPaid = None)
      "return 'false' when any data is defined" in {
        CISDeductions("", "", None, "", Some(1), None, None, Nil).isEmpty shouldBe false
      }

      "return 'false' when only period data is non empty" in {
        CISDeductions("", "", None, "", None, None, None, Seq(nonEmptyPeriodData)).isEmpty shouldBe false
      }

      "return 'false' when period data is non empty and zeroed values exist" in {
        CISDeductions("", "", None, "", Some(0), Some(0), Some(0), Seq(nonEmptyPeriodData)).isEmpty shouldBe false
      }

      "return 'true' when no data is defined" in {
        CISDeductions("", "", None, "", None, None, None, Nil).isEmpty shouldBe true
      }

      "return 'true' when data matches 'zeroed' response and period data is empty" in {
        CISSource(Some(0), Some(0), Some(0), Nil).isEmpty shouldBe true
      }

      "return 'true' when period data is empty for a 'zeroed' response" in {
        CISDeductions("", "", None, "", Some(0), Some(0), Some(0), Seq(emptyPeriodData)).isEmpty shouldBe true
      }

      "return 'true' when only period data is present and they are empty" in {
        CISDeductions("", "", None, "", None, None, None, Seq(emptyPeriodData)).isEmpty shouldBe true
      }
    }
  }
}
