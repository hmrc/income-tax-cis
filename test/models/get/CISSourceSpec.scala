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

class CISSourceSpec extends UnitTest {

  "CisSource" when {
    "isEmpty" should {
      val nonEmptyDeduction: CISDeductions = CISDeductions("", "", None, "", Some(1), None, None, Nil)
      val emptyDeduction: CISDeductions = nonEmptyDeduction.copy(totalDeductionAmount = None)
      "return 'false' when any data is defined" in {
        CISSource(Some(1), Some(2), Some(3), Seq(nonEmptyDeduction)).isEmpty shouldBe false
      }

      "return 'false' when only deductions are non empty" in {
        CISSource(None, None, None, Seq(nonEmptyDeduction)).isEmpty shouldBe false
      }

      "return 'false' when deductions are non empty and zeroed values exist" in {
        CISSource(Some(0), Some(0), Some(0), Seq(nonEmptyDeduction)).isEmpty shouldBe false
      }

      "return 'true' when no data is defined" in {
        CISSource(None, None, None, Nil).isEmpty shouldBe true
      }

      "return 'true' when data matches 'zeroed' response and period data is empty" in {
        CISSource(Some(0), Some(0), Some(0), Nil).isEmpty shouldBe true
      }

      "return 'true' when deductions are empty for a 'zeroed' response" in {
        CISSource(Some(0), Some(0), Some(0), Seq(emptyDeduction)).isEmpty shouldBe true
      }

      "return 'true' when only deductions are present and they are empty" in {
        CISSource(None, None, None, Seq(emptyDeduction)).isEmpty shouldBe true
      }
    }
  }
}
