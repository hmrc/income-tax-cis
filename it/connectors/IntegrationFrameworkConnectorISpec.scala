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

package connectors

import builders.CISSourceBuilder.customerCISSource
import connectors.errors.{ApiError, SingleErrorBody}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import utils.CISTaxYearHelper

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class IntegrationFrameworkConnectorISpec extends ConnectorIntegrationTest
  with TaxYearProvider {

  private val nino = "some-nino"
  private val source = "some-source"
  private val submissionId = UUID.randomUUID().toString
  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val underTest = new IntegrationFrameworkConnector(httpClient, appConfigStub)

  private def toTaxYearParam(taxYear: Int): String = {
    s"${(taxYear - 1).toString takeRight 2}-${taxYear.toString takeRight 2}"
  }

  ".getCisDeductions" should {
    "return correct IF response when correct parameters are passed" in {
      val httpResponse = HttpResponse(OK, Json.toJson(Some(customerCISSource(taxYear))).toString())
      val cisTaxYear = CISTaxYearHelper.cisTaxYearConverter(taxYear)

      val url = s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino\\?startDate=${cisTaxYear.fromDate}&endDate=${cisTaxYear.toDate}&source=$source"
      stubGetHttpClientCall(url, httpResponse)

      await(underTest.getCisDeductions(taxYear, nino, source)(hc)) shouldBe Right(Some(customerCISSource(taxYear)))
    }

    "return IF error when Left is returned" in {
      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(SingleErrorBody("some-code", "some-reason")).toString())
      val cisTaxYear = CISTaxYearHelper.cisTaxYearConverter(taxYear)

      val url = s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino\\?startDate=${cisTaxYear.fromDate}&endDate=${cisTaxYear.toDate}&source=$source"
      stubGetHttpClientCall(url, httpResponse)

      await(underTest.getCisDeductions(taxYear, nino, source)(hc)) shouldBe
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
    }
  }

  ".deleteCisDeductions" should {
    "return correct IF response when correct parameters are passed" in {
      val httpResponse = HttpResponse(NO_CONTENT, "")

      stubDeleteHttpClientCall(s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino/submissionId/$submissionId", httpResponse)

      await(underTest.deleteCisDeductions(taxYear, nino, submissionId)(hc)) shouldBe Right(())
    }

    "return IF error when Left is returned" in {
      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(SingleErrorBody("some-code", "some-reason")).toString())

      stubDeleteHttpClientCall(s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino/submissionId/$submissionId", httpResponse)

      await(underTest.deleteCisDeductions(taxYear, nino, submissionId)(hc)) shouldBe
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
    }
  }
}
