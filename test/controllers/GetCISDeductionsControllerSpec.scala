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
import models.get.AllCISDeductions
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json
import play.api.test.Helpers.status
import support.ControllerUnitTest
import support.builders.AllCISDeductionsBuilder.anAllCISDeductions
import support.mocks.{MockAuthorisedAction, MockCISDeductionsService}
import support.providers.FakeRequestProvider

import scala.concurrent.ExecutionContext.Implicits.global

class GetCISDeductionsControllerSpec extends ControllerUnitTest
  with MockCISDeductionsService
  with MockAuthorisedAction
  with FakeRequestProvider {

  private val nino: String = "123456789"
  private val anyTaxYear: Int = 2022

  private val underTest = new GetCISDeductionsController(
    mockCISDeductionsService,
    mockAuthorisedAction,
    cc)

  "calling .getCISDeductions" should {
    "with existing customer and contractor data" should {
      "return an OK 200 response when called as an individual" in {
        mockAuthorisation()
        mockGetCISDeductions(nino, anyTaxYear, Right(anAllCISDeductions))

        val result = await(underTest.getCISDeductions(nino, anyTaxYear)(fakeGetRequest))

        result.header.status shouldBe OK
        Json.parse(consumeBody(result)) shouldBe Json.toJson(anAllCISDeductions)
      }
    }

    "without existing customer and contractor data" should {
      "return an NO CONTENT 204 response when called as an individual" in {
        mockAuthorisation()
        mockGetCISDeductions(nino, anyTaxYear, Right(AllCISDeductions(None, None)))

        val result = underTest.getCISDeductions(nino, anyTaxYear)(fakeGetRequest)

        status(result) shouldBe NO_CONTENT
      }
    }

    "when an error is returned" should {
      "return the error response when called as an individual" in {
        mockAuthorisation()
        mockGetCISDeductions(nino, anyTaxYear, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("any-code", "any-reason"))))

        val result = await(underTest.getCISDeductions(nino, anyTaxYear)(fakeGetRequest))

        result.header.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
