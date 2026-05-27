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

import connectors.IntegrationFrameworkConnector
import connectors.errors.ApiError
import connectors.parsers.CreateCISDeductionsParser.CreateCISDeductionsResponse
import connectors.parsers.GetCISDeductionsHttpParser.GetCISDeductionsResponse
import connectors.parsers.UpdateCISDeductionsHttpParser.UpdateCISDeductionsResponse
import models.{CreateCISDeductions, CreateCISDeductionsSuccess, UpdateCISDeductions}
import models.get.CISSource
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, when}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockIntegrationFrameworkConnector {

  protected val mockIntegrationFrameworkConnector: IntegrationFrameworkConnector =
    mock(classOf[IntegrationFrameworkConnector])

  def mockGetCisDeductions(taxYear: Int, nino: String, source: String, result: Either[ApiError, Option[CISSource]]): Unit =
    when(
      mockIntegrationFrameworkConnector.getCisDeductions(
        eqTo(taxYear),
        eqTo(nino),
        eqTo(source)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(result))

  def mockCreateCisDeductions(taxYear: Int,
                              nino: String,
                              createCISDeductions: CreateCISDeductions,
                              result: Either[ApiError, CreateCISDeductionsSuccess]): Unit =
    when(
      mockIntegrationFrameworkConnector.create(
        eqTo(taxYear),
        eqTo(nino),
        eqTo(createCISDeductions)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(result))

  def mockUpdate(taxYear: Int, nino: String, submissionId: String, updateCISDeductions: UpdateCISDeductions, result: Either[ApiError, Unit]): Unit =
    when(
      mockIntegrationFrameworkConnector.update(
        eqTo(taxYear),
        eqTo(nino),
        eqTo(submissionId),
        eqTo(updateCISDeductions)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(result))

  def mockDeleteCisDeductions(taxYear: Int, nino: String, submissionId: String, result: Either[ApiError, Unit]): Unit =
    when(
      mockIntegrationFrameworkConnector.deleteCisDeductions(
        eqTo(taxYear),
        eqTo(nino),
        eqTo(submissionId)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(result))
}
