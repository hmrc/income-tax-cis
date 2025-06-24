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

package connectors

import connectors.errors.{SingleErrorBody, ApiError}
import models.TaxYearPathBindable.{asTys, TaxYear}
import models.{CreateCISDeductionsSuccess, PeriodData}
import models.requests.HipCISDeductionsRequest
import org.scalamock.scalatest.MockFactory
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpResponse, HeaderCarrier, SessionId}

import scala.concurrent.ExecutionContext.Implicits.global

class HipConnectorSpec extends ConnectorIntegrationSpec with MockFactory {

  private val nino = "test-nino"
  private val taxYear = 2020
  private val employerRef = "exampleRef"
  private val contractorName = "exampleName"
  private val fromDate = "2019-08-24"
  private val toDate = "2019-08-24"
  private val periodData: PeriodData = PeriodData("2019-08-24", "2019-08-24", Some(BigDecimal(12.34)), BigDecimal(45.67), Some(BigDecimal(89.01)))
  private val submissionId = "exampleSubmissionId"

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val underTest = new HipConnector(httpClientV2, appConfigStub)

  ".createCISDeductions" should {
    "return the CreateCISDeductionsSuccess" when {
      "successfully created CIS Deductions" in {
        val requestBody = Json.toJson(Requests.validCreateDeductionsRequest).toString()
        val httpResponse = HttpResponse(OK, Json.toJson(Responses.hipCreateDeductionsResponse).toString)

        stubPostHttpClientCall(
          s"/income-tax/v1/${asTys(TaxYear(taxYear))}/cis/deductions/$nino",
          requestBody,
          httpResponse
        )

        val expectedResult = Right(CreateCISDeductionsSuccess(submissionId))

        await(
          underTest.createCISDeductions(
            asTys(TaxYear(taxYear)),
            nino,
            employerRef,
            contractorName,
            fromDate,
            toDate,
            Seq(periodData)
          )(hc)
        ) shouldBe expectedResult
      }
    }
    "return a API error from upstream" when {
      "a NOT_FOUND' error is returned from the API" in {
        val requestBody = Json.toJson(Requests.validCreateDeductionsRequest).toString()
        val apiError = SingleErrorBody("code", "reason")
        val httpResponse = HttpResponse(NOT_FOUND, Json.toJson(apiError).toString())

        stubPostHttpClientCall(
          s"/income-tax/v1/${asTys(TaxYear(taxYear))}/cis/deductions/$nino",
          requestBody,
          httpResponse
        )

        val expectedResult = Left(ApiError(NOT_FOUND, apiError))

        await(
          underTest.createCISDeductions(
            asTys(TaxYear(taxYear)),
            nino,
            employerRef,
            contractorName,
            fromDate,
            toDate,
            Seq(periodData)
          )(hc)
        ) shouldBe expectedResult
      }
      "a Service Error is returned from the API" in {
        val requestBody = Json.toJson(Requests.validCreateDeductionsRequest).toString()
        val apiError = SingleErrorBody("code", "reason")
        val apiServiceErrors = Seq(BAD_REQUEST, CONFLICT, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE)

        apiServiceErrors.foreach { apiErrorCode =>
          val httpResponse = HttpResponse(apiErrorCode, Json.toJson(apiError).toString())

          stubPostHttpClientCall(
            s"/income-tax/v1/${asTys(TaxYear(taxYear))}/cis/deductions/$nino",
            requestBody,
            httpResponse
          )

          val expectedResult = Left(ApiError(apiErrorCode, apiError))

          await(
            underTest.createCISDeductions(
              asTys(TaxYear(taxYear)),
              nino,
              employerRef,
              contractorName,
              fromDate,
              toDate,
              Seq(periodData)
            )(hc)
          ) shouldBe expectedResult
        }
      }
      "another unexpected error is returned from the API" in {
        val requestBody = Json.toJson(Requests.validCreateDeductionsRequest).toString()
        val apiError = SingleErrorBody("code", "reason")
        val httpResponse = HttpResponse(INSUFFICIENT_STORAGE, Json.toJson(apiError).toString())

        stubPostHttpClientCall(
          s"/income-tax/v1/${asTys(TaxYear(taxYear))}/cis/deductions/$nino",
          requestBody,
          httpResponse
        )
        val expectedResult = Left(ApiError(INTERNAL_SERVER_ERROR, apiError))
        await(
          underTest.createCISDeductions(
            asTys(TaxYear(taxYear)),
            nino,
            employerRef,
            contractorName,
            fromDate,
            toDate,
            Seq(periodData)
          )(hc)
        ) shouldBe expectedResult

      }
    }
  }

  object Requests {
    val validCreateDeductionsRequest: HipCISDeductionsRequest = HipCISDeductionsRequest(
      employerRef = employerRef,
      contractorName = contractorName,
      fromDate = fromDate,
      toDate = toDate,
      periodData = Array(periodData)
    )
  }

  object Responses {
    val hipCreateDeductionsResponse: CreateCISDeductionsSuccess = CreateCISDeductionsSuccess(
      submissionId = submissionId
    )
  }
}
