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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, when}
import services.CISDeductionsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockCISDeductionsService {

  protected val mockCISDeductionsService: CISDeductionsService =
    mock(classOf[CISDeductionsService])

  def mockGetCISDeductions(nino: String,
                           taxYear: Int,
                           result: Either[ApiError, AllCISDeductions]): Unit =
    when(
      mockCISDeductionsService.getCISDeductions(
        eqTo(nino),
        eqTo(taxYear)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(result))

  def mockGetCISDeductionsException(nino: String,
                                    taxYear: Int,
                                    result: Throwable): Unit =
    when(
      mockCISDeductionsService.getCISDeductions(
        eqTo(nino),
        eqTo(taxYear)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.failed(result))

  def mockDeleteCISDeductionsSubmission(taxYear: Int,
                                        nino: String,
                                        submissionId: String,
                                        response: Either[ApiError, Unit]): Unit =
    when(
      mockCISDeductionsService.deleteCISDeductionsSubmission(
        eqTo(taxYear),
        eqTo(nino),
        eqTo(submissionId)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(response))

  def mockSubmitCISDeductions(nino: String,
                              taxYear: Int,
                              data: CISSubmission,
                              response: Either[ApiError, Option[String]]): Unit =
    when(
      mockCISDeductionsService.submitCISDeductions(
        eqTo(nino),
        eqTo(taxYear),
        eqTo(data)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(response))
}
