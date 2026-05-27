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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, when}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockHipConnector {

  protected val mockHipConnector: HipConnector =
    mock(classOf[HipConnector])

  def mockHipCISDeductionsSubmission(taxYear: String,
                                     nino: String,
                                     employerRef: String,
                                     contractorName: String,
                                     fromDate: String,
                                     toDate: String,
                                     periodData: PeriodData,
                                     result: Either[ApiError, CreateCISDeductionsSuccess]): Unit =
    when(
      mockHipConnector.createCISDeductions(
        eqTo(taxYear),
        eqTo(nino),
        eqTo(employerRef),
        eqTo(contractorName),
        eqTo(fromDate),
        eqTo(toDate),
        eqTo(Seq(periodData))
      )(any[HeaderCarrier]())
    ).thenReturn(Future.successful(result))
}
