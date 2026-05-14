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

import cats.data.EitherT
import connectors.errors.ApiError
import models.prePopulation.PrePopulationResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, when}
import services.PrePopulationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait MockPrePopulationService {

  protected val mockPrePopService: PrePopulationService =
    mock(classOf[PrePopulationService])

  def mockGetPrePop(taxYear: Int, nino: String, result: Either[ApiError, PrePopulationResponse]): Unit =
    when(
      mockPrePopService.get(
        eqTo(taxYear),
        eqTo(nino)
      )(
        any[ExecutionContext](),
        any[HeaderCarrier]()
      )
    ).thenReturn(EitherT.fromEither[Future](result))
}
