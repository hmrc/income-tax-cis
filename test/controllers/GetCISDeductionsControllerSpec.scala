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

import common.CISSource.CONTRACTOR
import connectors.errors.{ApiError, SingleErrorBody}
import models.get.AllCISDeductions
import org.scalamock.handlers.CallHandler4
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.CISDeductionsService
import support.builders.CISDeductionsBuilder.aCISDeductions
import support.builders.CISSourceBuilder.aCISSource
import support.builders.GetPeriodDataBuilder.aGetPeriodData
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class GetCISDeductionsControllerSpec extends TestUtils {

  private val service: CISDeductionsService = mock[CISDeductionsService]
  private val controller = new GetCISDeductionsController(service, authorisedAction, mockControllerComponents)

  private val nino: String = "123456789"
  private val mtdItID: String = "1234567890"
  private val taxYear: Int = 2022
  private val contractorCISSource = aCISSource.copy(cisDeductions = Seq(aCISDeductions.copy(periodData = Seq(aGetPeriodData.copy(submissionId = None, source = CONTRACTOR)))))

  private val fakeGetRequest = FakeRequest("GET", "/").withHeaders("MTDITID" -> mtdItID)

  def mockGetCISDeductions(data: AllCISDeductions):
  CallHandler4[String, Int, HeaderCarrier, ExecutionContext, Future[Either[ApiError, AllCISDeductions]]] = {
    (service.getCISDeductions(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(Right(data)))
  }

  def mockGetCISDeductionsError(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext, Future[Either[ApiError, AllCISDeductions]]] = {
    (service.getCISDeductions(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(Left(ApiError(
        INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError
      ))))
  }

  "calling .getCISDeductions" should {
    "with existing customer and contractor data" should {
      "return an OK 200 response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetCISDeductions(AllCISDeductions(
            customerCISDeductions = Some(aCISSource),
            contractorCISDeductions = Some(contractorCISSource)
          ))
          controller.getCISDeductions(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe OK
        Json.parse(bodyOf(result)) mustBe Json.toJson(
          AllCISDeductions(
            customerCISDeductions = Some(aCISSource),
            contractorCISDeductions = Some(contractorCISSource)
          )
        )
      }
    }

    "without existing customer and contractor data" should {
      "return an NO CONTENT 204 response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetCISDeductions(AllCISDeductions(
            customerCISDeductions = None,
            contractorCISDeductions = None
          ))
          controller.getCISDeductions(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe NO_CONTENT
      }
    }

    "when an error is returned" should {
      "return the error response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetCISDeductionsError()
          controller.getCISDeductions(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
        bodyOf(result) mustBe """{"code":"PARSING_ERROR","reason":"Error parsing response from DES"}"""
      }
    }
  }
}
