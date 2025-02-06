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

package models.prePopulation

import models.get.{AllCISDeductions, CISSource}
import play.api.libs.json.Json
import support.UnitTest

class PrePopulationResponseSpec extends UnitTest {

  "PrePopulationResponse" when {
    "written to JSON" should {
      "return the expected JsValue" in {
        val prePopulationResponse = PrePopulationResponse(
          hasCis = true
        )

        Json.toJson(prePopulationResponse) shouldBe Json.parse(
        """
           |{
           |  "hasCis": true
           |}
        """.stripMargin
        )
      }
    }

    "fromData" should {
      "return hasCisData as 'false' when HMRC and customer are 'None'" in {
        PrePopulationResponse.fromData(AllCISDeductions(None, None)) shouldBe PrePopulationResponse(false)
      }

      "return hasCisData as 'false' when HMRC and customer are defined, but empty" in {
        val emptyCis = Some(CISSource(None, None, None, Nil))
        PrePopulationResponse.fromData(AllCISDeductions(emptyCis, emptyCis)) shouldBe PrePopulationResponse(false)
      }

      "return hasCisData as 'true' when customer data exists for CIS" in {
        val someDataCis = Some(CISSource(Some(1), None, None, Nil))
        PrePopulationResponse.fromData(AllCISDeductions(someDataCis, None)) shouldBe PrePopulationResponse(true)
      }

      "return hasCisData as 'true' when contractor data exists for CIS" in {
        val someDataCis = Some(CISSource(Some(1), None, None, Nil))
        PrePopulationResponse.fromData(AllCISDeductions(None, someDataCis)) shouldBe PrePopulationResponse(true)
      }

      "return hasCisData as 'true' when both contractor and customer data exists for CIS" in {
        val someDataCis = Some(CISSource(Some(1), None, None, Nil))
        PrePopulationResponse.fromData(AllCISDeductions(someDataCis, someDataCis)) shouldBe PrePopulationResponse(true)
      }
    }
  }
}
