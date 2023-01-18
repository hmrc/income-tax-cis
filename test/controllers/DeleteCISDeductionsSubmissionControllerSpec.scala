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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.test.Helpers.status
import support.ControllerUnitTest
import support.mocks.{MockAuthorisedAction, MockCISDeductionsService}
import support.providers.FakeRequestProvider

import scala.concurrent.ExecutionContext.Implicits.global

class DeleteCISDeductionsSubmissionControllerSpec extends ControllerUnitTest
  with MockCISDeductionsService
  with MockAuthorisedAction
  with FakeRequestProvider {

  private val underTest = new DeleteCISDeductionsSubmissionController(
    mockCISDeductionsService,
    mockAuthorisedAction,
    cc
  )

  private val nino: String = "123456789"
  private val taxYear: Int = 2022

  "calling .deleteCISDeductionsSubmission" should {
    "return an NO CONTENT response when successfully deletes the data" in {
      mockAuthorisation()
      mockDeleteCISDeductionsSubmission(taxYear, nino, "submissionId", Right(()))

      val result =
        underTest.deleteCISDeductionsSubmission(nino, taxYear, "submissionId")(fakeDeleteRequest)

      status(result) shouldBe NO_CONTENT
    }

    "when an error is returned" should {
      "return the error response" in {
        mockAuthorisation()
        mockDeleteCISDeductionsSubmission(taxYear, nino, "submissionId", Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

        val result = underTest.deleteCISDeductionsSubmission(nino, taxYear, "submissionId")(fakeDeleteRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
