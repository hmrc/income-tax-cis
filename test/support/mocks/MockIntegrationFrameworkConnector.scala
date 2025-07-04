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
import org.scalamock.handlers.{CallHandler4, CallHandler5}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockIntegrationFrameworkConnector extends MockFactory { _: TestSuite =>

  protected val mockIntegrationFrameworkConnector: IntegrationFrameworkConnector = mock[IntegrationFrameworkConnector]

  def mockGetCisDeductions(taxYear: Int,
                           nino: String,
                           source: String,
                           result: Either[ApiError, Option[CISSource]]): CallHandler4[Int, String, String, HeaderCarrier, Future[GetCISDeductionsResponse]] = {
    (mockIntegrationFrameworkConnector.getCisDeductions(_: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(taxYear, nino, source, *)
      .returning(Future.successful(result))
  }

  def mockCreateCisDeductions(taxYear: Int,
                              nino: String,
                              createCISDeductions: CreateCISDeductions,
                              result: Either[ApiError, CreateCISDeductionsSuccess]): CallHandler4[Int, String, CreateCISDeductions, HeaderCarrier, Future[CreateCISDeductionsResponse]] = {
    (mockIntegrationFrameworkConnector.create(_: Int, _: String, _: CreateCISDeductions)(_: HeaderCarrier))
      .expects(taxYear, nino, createCISDeductions, *)
      .returning(Future.successful(result))
  }

  def mockUpdate(taxYear: Int,
                 nino: String,
                 submissionId: String,
                 updateCISDeductions: UpdateCISDeductions,
                 result: Either[ApiError, Unit]): CallHandler5[Int, String, String, UpdateCISDeductions, HeaderCarrier, Future[UpdateCISDeductionsResponse]] = {
    (mockIntegrationFrameworkConnector.update(_: Int, _: String, _: String, _: UpdateCISDeductions)(_: HeaderCarrier))
      .expects(taxYear, nino, submissionId, updateCISDeductions, *)
      .returning(Future.successful(result))
  }

  def mockDeleteCisDeductions(taxYear: Int,
                              nino: String,
                              submissionId: String,
                              result: Either[ApiError, Unit]): CallHandler4[Int, String, String, HeaderCarrier, Future[Either[ApiError, Unit]]] = {
    (mockIntegrationFrameworkConnector.deleteCisDeductions(_: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(taxYear, nino, submissionId, *)
      .returning(Future.successful(result))
  }
}
