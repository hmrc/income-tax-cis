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

package models.requests

import models.PeriodData
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class HipCISDeductionsRequestSpec extends AnyWordSpec with Matchers {
  private val employerRef = "exampleRef"
  private val contractorName = "exampleName"
  private val fromDate = "2019-08-24"
  private val toDate = "2019-08-24"
  private val periodData: Array[PeriodData] = Array(PeriodData("2019-08-24", "2019-08-24", Some(BigDecimal(12.34)), BigDecimal(45.67), Some(BigDecimal(89.01))))

  "HipCISDeductionsRequest" should {
    "serialize correctly to JSON" in {
      val request = HipCISDeductionsRequest(
        employerRef = employerRef,
        contractorName = contractorName, fromDate = fromDate,
        toDate = toDate,
        periodData = periodData
      )

      val expectedPeriodJson = Array(Json.obj(
          "deductionFromDate" -> "2019-08-24",
          "deductionToDate" -> "2019-08-24",
          "grossAmountPaid" -> 12.34,
          "deductionAmount" -> 45.67,
          "costOfMaterials" -> 89.01
      ))

      val expectedJson = Json.obj(
        "employerRef"      -> "exampleRef",
        "contractorName"   -> "exampleName",
        "fromDate"         -> "2019-08-24",
        "toDate"           -> "2019-08-24",
        "periodData"       -> expectedPeriodJson
      )

      Json.toJson(request) shouldBe expectedJson
    }
  }
}
