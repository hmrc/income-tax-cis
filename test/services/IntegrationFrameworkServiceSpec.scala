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

package services

import support.UnitTest
import support.builders.CISSourceBuilder.aCISSource
import support.mocks.MockIntegrationFrameworkConnector
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID

class IntegrationFrameworkServiceSpec extends UnitTest
  with MockIntegrationFrameworkConnector
  with TaxYearProvider {

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val source = "some-source"
  private val nino = "some-nino"
  private val submissionId = UUID.randomUUID().toString

  private val underTest = new IntegrationFrameworkService(mockIntegrationFrameworkConnector)

  ".getCisDeductions" should {
    "delegate to IFConnector and return the result" in {
      mockGetCisDeductions(taxYear, nino, source, Right(Some(aCISSource)))

      await(underTest.getCisDeductions(taxYear, nino, source))
    }
  }

  ".deleteCisDeductions" should {
    "delegate to IFConnector and return the result" in {
      mockDeleteCisDeductions(taxYear, nino, submissionId, Right(()))

      await(underTest.deleteCisDeductions(taxYear, nino, submissionId)) shouldBe Right(())
    }
  }
}
