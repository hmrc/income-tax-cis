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
import org.scalamock.handlers.{CallHandler3, CallHandler4}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockCISDeductionsConnector extends MockFactory { _: TestSuite =>

  protected val mockCISDeductionsConnector: CISDeductionsConnector = mock[CISDeductionsConnector]

  def mockDelete(nino: String,
                 submissionId: String,
                 response: Either[ApiError, Unit]): CallHandler3[String, String, HeaderCarrier, Future[DeleteCISDeductionsResponse]] = {
    (mockCISDeductionsConnector.delete(_: String, _: String)(_: HeaderCarrier))
      .expects(nino, submissionId, *)
      .returning(Future.successful(response))
  }

  def mockGet(nino: String,
              taxYear: Int,
              source: String,
              connectorResult: GetCISDeductionsResponse): CallHandler4[String,
    Int, String, HeaderCarrier, Future[GetCISDeductionsResponse]] = {
    (mockCISDeductionsConnector.get(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(nino, taxYear, source, *)
      .returning(Future.successful(connectorResult))
  }

  def mockUpdate(nino: String,
                 submissionId: String,
                 model: UpdateCISDeductions,
                 connectorResult: Either[ApiError, Unit]): CallHandler4[String, String,
    UpdateCISDeductions, HeaderCarrier, Future[UpdateCISDeductionsResponse]] = {
    (mockCISDeductionsConnector.update(_: String, _: String, _: UpdateCISDeductions)(_: HeaderCarrier))
      .expects(nino, submissionId, model, *)
      .returning(Future.successful(connectorResult))
  }

  def mockCreate(nino: String,
                 taxYear: Int,
                 model: CreateCISDeductions,
                 connectorResult: Either[ApiError, CreateCISDeductionsSuccess]): CallHandler4[String, Int,
    CreateCISDeductions, HeaderCarrier, Future[CreateCISDeductionsResponse]] = {
    (mockCISDeductionsConnector.create(_: String, _: Int, _: CreateCISDeductions)(_: HeaderCarrier))
      .expects(nino, taxYear, model, *)
      .returning(Future.successful(connectorResult))
  }
}
