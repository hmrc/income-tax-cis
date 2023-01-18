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

package support.mocks

import connectors.errors.ApiError
import models.get.AllCISDeductions
import models.submission.CISSubmission
import org.scalamock.handlers.{CallHandler3, CallHandler4}
import org.scalamock.scalatest.MockFactory
import services.CISDeductionsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockCISDeductionsService extends MockFactory {

  protected val mockCISDeductionsService: CISDeductionsService = mock[CISDeductionsService]

  def mockGetCISDeductions(nino: String,
                           taxYear: Int,
                           result: Either[ApiError, AllCISDeductions]): CallHandler3[String, Int, HeaderCarrier, Future[Either[ApiError, AllCISDeductions]]] = {
    (mockCISDeductionsService.getCISDeductions(_: String, _: Int)(_: HeaderCarrier))
      .expects(nino, taxYear, *)
      .returning(Future.successful(result))
  }

  def mockDeleteCISDeductionsSubmission(taxYear: Int,
                                        nino: String,
                                        submissionId: String,
                                        response: Either[ApiError, Unit]): CallHandler4[Int, String, String, HeaderCarrier, Future[Either[ApiError, Unit]]] = {
    (mockCISDeductionsService.deleteCISDeductionsSubmission(_: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(taxYear, nino, submissionId, *)
      .returning(Future.successful(response))
  }

  def mockSubmitCISDeductions(nino: String,
                              taxYear: Int,
                              data: CISSubmission,
                              response: Either[ApiError, Option[String]]): CallHandler4[String, Int, CISSubmission, HeaderCarrier, Future[Either[ApiError, Option[String]]]] = {
    (mockCISDeductionsService.submitCISDeductions(_: String, _: Int, _: CISSubmission)(_: HeaderCarrier))
      .expects(nino, taxYear, data, *)
      .returning(Future.successful(response))
  }
}
