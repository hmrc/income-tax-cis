/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.HipConnector
import connectors.errors.ApiError
import models.{CreateCISDeductionsSuccess, PeriodData}
import org.scalamock.handlers.CallHandler8
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockHipConnector extends MockFactory { _: TestSuite =>

  protected val mockHipConnector: HipConnector = mock[HipConnector]

  def mockHipCISDeductionsSubmission(
                                      taxYear: String,
                                      nino: String,
                                      employerRef: String,
                                      contractorName: String,
                                      fromDate: String,
                                      toDate: String,
                                      periodData: PeriodData,
                                      result: Either[ApiError, CreateCISDeductionsSuccess]
                                    ): CallHandler8[String, String, String, String, String, String, Seq[PeriodData], HeaderCarrier, Future[
    Either[ApiError, CreateCISDeductionsSuccess]
  ]] = (
    mockHipConnector
      .createCISDeductions(
        _: String,
        _: String,
        _: String,
        _: String,
        _: String,
        _: String,
        _: Seq[PeriodData]
      )(
        _: HeaderCarrier
      ))
    .expects(taxYear, nino, employerRef, contractorName, fromDate, toDate, Seq(periodData), *)
    .returning(Future.successful(result))
}
