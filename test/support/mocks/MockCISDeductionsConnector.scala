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

import connectors.CISDeductionsConnector
import connectors.errors.ApiError
import connectors.parsers.CreateCISDeductionsParser.CreateCISDeductionsResponse
import connectors.parsers.DeleteCISDeductionsHttpParser.DeleteCISDeductionsResponse
import connectors.parsers.GetCISDeductionsHttpParser.GetCISDeductionsResponse
import connectors.parsers.UpdateCISDeductionsHttpParser.UpdateCISDeductionsResponse
import models.{CreateCISDeductions, CreateCISDeductionsSuccess, UpdateCISDeductions}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, when}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockCISDeductionsConnector {

  protected val mockCISDeductionsConnector: CISDeductionsConnector =
    mock(classOf[CISDeductionsConnector])

  def mockDelete(nino: String, submissionId: String, response: Either[ApiError, Unit]): Unit =
    when(
      mockCISDeductionsConnector.delete(
        eqTo(nino),
        eqTo(submissionId)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(response))

  def mockGet(nino: String, taxYear: Int, source: String, connectorResult: GetCISDeductionsResponse): Unit =
    when(
      mockCISDeductionsConnector.get(
        eqTo(nino),
        eqTo(taxYear),
        eqTo(source)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(connectorResult))

  def mockUpdate(nino: String, submissionId: String, model: UpdateCISDeductions, connectorResult: Either[ApiError, Unit]): Unit =
    when(
      mockCISDeductionsConnector.update(
        eqTo(nino),
        eqTo(submissionId),
        eqTo(model)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(connectorResult))

  def mockCreate(nino: String, taxYear: Int, model: CreateCISDeductions, connectorResult: Either[ApiError, CreateCISDeductionsSuccess]): Unit =
    when(
      mockCISDeductionsConnector.create(
        eqTo(nino),
        eqTo(taxYear),
        eqTo(model)
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(connectorResult))
}
