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

package controllers

import connectors.errors.{ApiError, SingleErrorBody}
import models.CreateCISDeductionsSuccess
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.status
import support.ControllerUnitTest
import support.builders.CISSubmissionBuilder.aCISSubmission
import support.builders.PeriodDataBuilder.aPeriodData
import support.mocks.{MockAuthorisedAction, MockCISDeductionsService}
import support.providers.FakeRequestProvider

import scala.concurrent.ExecutionContext.Implicits.global

class CreateUpdateCisDeductionsControllerSpec extends ControllerUnitTest
  with MockCISDeductionsService
  with MockAuthorisedAction
  with FakeRequestProvider {

  private val nino: String = "123456789"
  private val anyTaxYear: Int = 2022

  private val underTest = new CreateUpdateCisDeductionsController(
    mockCISDeductionsService,
    mockAuthorisedAction,
    cc)

  "calling .postCISDeductions" should {
    "with create body" should {
      "return an OK 200 response" in {
        mockAuthorisation()
        mockSubmitCISDeductions(nino, anyTaxYear, aCISSubmission.copy(submissionId = None), Right(Some("id")))

        val result = await(underTest.postCISDeductions(nino, anyTaxYear)(fakeGetRequest.withJsonBody(Json.toJson(aCISSubmission.copy(submissionId = None)))))

        result.header.status shouldBe OK
        Json.parse(consumeBody(result)) shouldBe Json.toJson(CreateCISDeductionsSuccess("id"))
      }
    }

    "with update body" should {
      val anUpdateCISSubmission = aCISSubmission.copy(employerRef = None, contractorName = None)
      "return an OK 200 response" in {
        mockAuthorisation()
        mockSubmitCISDeductions(nino, anyTaxYear, anUpdateCISSubmission, Right(None))


        val result = underTest.postCISDeductions(nino, anyTaxYear)(fakeRequest.withJsonBody(Json.toJson(anUpdateCISSubmission)))

        status(result) shouldBe OK
      }

      "return an OK 200 response when more than one period" in {
        mockAuthorisation()
        mockSubmitCISDeductions(nino, anyTaxYear, anUpdateCISSubmission.copy(periodData = Seq(aPeriodData, aPeriodData)), Right(None))

        val result = underTest.postCISDeductions(nino, anyTaxYear)(fakeRequest.withJsonBody(Json.toJson(anUpdateCISSubmission.copy(periodData = Seq(aPeriodData, aPeriodData)))))

        status(result) shouldBe OK
      }
    }

    "return a bad request" when {
      "a submission id is passed alongside a name and reference" in {
        mockAuthorisation()

        val result = underTest.postCISDeductions(nino, anyTaxYear)(fakeRequest.withJsonBody(Json.toJson(aCISSubmission)))

        status(result) shouldBe BAD_REQUEST
      }

      "period data is empty" in {
        val body: JsValue = Json.toJson(aCISSubmission.copy(periodData = Seq.empty))

        mockAuthorisation()

        val result = underTest.postCISDeductions(nino, anyTaxYear)(fakeRequest.withJsonBody(body))

        status(result) shouldBe BAD_REQUEST
      }
    }

    "when an error is returned" should {
      "return the error response when called as an individual" in {
        mockAuthorisation()
        mockSubmitCISDeductions(nino, anyTaxYear, aCISSubmission.copy(submissionId = None), Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

        val result = underTest.postCISDeductions(nino, anyTaxYear)(fakeRequest.withJsonBody(Json.toJson(aCISSubmission.copy(submissionId = None))))

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
