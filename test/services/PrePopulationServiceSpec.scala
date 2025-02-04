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

package services

import connectors.errors.{ApiError, SingleErrorBody}
import models.get.{AllCISDeductions, CISDeductions, CISSource, GetPeriodData}
import models.prePopulation.PrePopulationResponse
import play.api.http.Status.IM_A_TEAPOT
import support.UnitTest
import support.mocks.MockCISDeductionsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class PrePopulationServiceSpec extends UnitTest
  with MockCISDeductionsService {

  trait Test {
    val taxYear: Int = 2024
    val nino: String = "AA111111A"
    val service: PrePopulationService = new PrePopulationService(
      service = mockCISDeductionsService
    )

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    val dummyData: PrePopulationResponse = PrePopulationResponse(
      hasCis = false
    )
  }

  "get" when {
    val dummyErrorBody: SingleErrorBody = SingleErrorBody("Some", "Error")
    val defaultErrorBody: SingleErrorBody = SingleErrorBody("defaulted", "error")
    val defaultError: ApiError = ApiError(IM_A_TEAPOT, defaultErrorBody)
    "call to retrieve CIS data fails with a non-404 status code" should {
      "return an error" in new Test {
        mockGetCISDeductions(taxYear = taxYear, nino = nino, result = Left(ApiError(500, dummyErrorBody)))
        val result: Either[ApiError, PrePopulationResponse] = await(service.get(taxYear, nino).value)

        result shouldBe a[Left[_, _]]
        result.swap.getOrElse(defaultError).body shouldBe dummyErrorBody
      }
    }

    "call to retrieve CIS data succeeds, but the response contains no relevant data" should {
      "return a 'no pre-pop' response" in new Test {
        val emptyIfResponse: AllCISDeductions = AllCISDeductions(None, None)

        mockGetCISDeductions(taxYear = taxYear, nino = nino, result = Right(emptyIfResponse))
        val result: Either[ApiError, PrePopulationResponse] = await(service.get(taxYear, nino).value)

        result shouldBe a[Right[_, _]]
        result.getOrElse(dummyData) shouldBe PrePopulationResponse.noPrePop
      }
    }

    "call to retrieve CIS data succeeds, and the response contains relevant data" should {
      "return pre-pop flags as 'true' when customer data exists" in new Test {
        val customerOnlyIfResponse: AllCISDeductions = AllCISDeductions(
          customerCISDeductions = Some(CISSource(
            totalDeductionAmount = Some(100),
            totalCostOfMaterials = None,
            totalGrossAmountPaid = None,
            cisDeductions = Nil
          )),
          contractorCISDeductions = None
        )

        mockGetCISDeductions(taxYear = taxYear, nino = nino, result = Right(customerOnlyIfResponse))
        val result: Either[ApiError, PrePopulationResponse] = await(service.get(taxYear, nino).value)

        result shouldBe a[Right[_, _]]
        result.getOrElse(PrePopulationResponse.noPrePop) shouldBe PrePopulationResponse(hasCis = true)
      }

      "return pre-pop flags as 'true' when non-zeroed HMRC-Held data exists" in new Test {
        val hmrcHeldOnlyIfResponse: AllCISDeductions = AllCISDeductions(
          customerCISDeductions = None,
          contractorCISDeductions = Some(CISSource(
            totalDeductionAmount = Some(100),
            totalCostOfMaterials = None,
            totalGrossAmountPaid = None,
            cisDeductions = Nil
          ))
        )

        mockGetCISDeductions(taxYear = taxYear, nino = nino, result = Right(hmrcHeldOnlyIfResponse))
        val result: Either[ApiError, PrePopulationResponse] = await(service.get(taxYear, nino).value)

        result shouldBe a[Right[_, _]]
        result.getOrElse(PrePopulationResponse.noPrePop) shouldBe PrePopulationResponse(hasCis = true)
      }

      "return pre-pop flags as 'false' when only zeroed HMRC-held data exists" in new Test {
        val hmrcHeldOnlyIfResponse: AllCISDeductions = AllCISDeductions(
          customerCISDeductions = None,
          contractorCISDeductions = Some(CISSource(
            totalDeductionAmount = Some(0),
            totalCostOfMaterials = Some(0),
            totalGrossAmountPaid = Some(0),
            cisDeductions = Seq(
              CISDeductions(
                fromDate = "fromDate",
                toDate = "toDate",
                contractorName = Some("contractor name"),
                employerRef = "ref",
                totalDeductionAmount = Some(0),
                totalCostOfMaterials = Some(0),
                totalGrossAmountPaid = Some(0),
                periodData = Seq(
                  GetPeriodData(
                    deductionFromDate = "fromDate",
                    deductionToDate = "toDate",
                    deductionAmount = Some(0),
                    costOfMaterials = Some(0),
                    grossAmountPaid = Some(0),
                    submissionDate = "submissionDate",
                    submissionId = Some("submissionId"),
                    source = "contractor"
                  )
                )
              )
            )
          ))
        )

        mockGetCISDeductions(taxYear = taxYear, nino = nino, result = Right(hmrcHeldOnlyIfResponse))
        val result: Either[ApiError, PrePopulationResponse] = await(service.get(taxYear, nino).value)

        result shouldBe a[Right[_, _]]
        result.getOrElse(dummyData) shouldBe PrePopulationResponse.noPrePop
      }
    }
  }

}
