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

import connectors.errors.{ApiError, SingleErrorBody}
import models.CreateCISDeductionsSuccess
import play.api.http.Status.{BAD_GATEWAY, CREATED, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import support.builders.CISSourceBuilder.aCISSource
import support.builders.CISSubmissionBuilder.aCISSubmission
import support.builders.CreateCISDeductionsBuilder.aCreateCISDeductions
import support.builders.UpdateCISDeductionsBuilder.anUpdateCISDeductions
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
  private val errorBody: SingleErrorBody = SingleErrorBody("some-code", "some-reason")

  private def toTaxYearParam(taxYear: Int): String = {
    s"${(taxYear - 1).toString takeRight 2}-${taxYear.toString takeRight 2}"
  }

  private val underTest = new IntegrationFrameworkConnector(httpClient, appConfigStub)

  ".getCisDeductions" should {
    "return correct IF response when correct parameters are passed" in {
      val httpResponse = HttpResponse(OK, Json.toJson(Some(aCISSource)).toString())
      val cisTaxYear = CISTaxYearHelper.cisTaxYearConverter(taxYear)

      val url = s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino\\?startDate=${cisTaxYear.fromDate}&endDate=${cisTaxYear.toDate}&source=$source"
      stubGetHttpClientCall(url, httpResponse)

      await(underTest.getCisDeductions(taxYear, nino, source)(hc)) shouldBe Right(Some(aCISSource))
    }

    "return IF error when Left is returned" in {
      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(errorBody).toString())
      val cisTaxYear = CISTaxYearHelper.cisTaxYearConverter(taxYear)

      val url = s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino\\?startDate=${cisTaxYear.fromDate}&endDate=${cisTaxYear.toDate}&source=$source"
      stubGetHttpClientCall(url, httpResponse)

      await(underTest.getCisDeductions(taxYear, nino, source)(hc)) shouldBe
        Left(ApiError(INTERNAL_SERVER_ERROR, errorBody))
    }
  }

  ".create" should {
    val url = s"/income-tax/23-24/cis/deductions/$nino"
    "return correct IF response when correct parameters are passed" in {
      val success = CreateCISDeductionsSuccess(aCISSubmission.submissionId.get)
      stubPostWithResponseBody(url, CREATED, Json.toJson(aCreateCISDeductions.toApiModel(taxYear)).toString(), Json.toJson(success).toString())

      await(underTest.create(taxYear, nino, aCreateCISDeductions)(hc)) shouldBe Right(success)
    }

    "return IF error when left is returned" in {
      stubPostWithResponseBody(url, INTERNAL_SERVER_ERROR, Json.toJson(aCreateCISDeductions.toApiModel(taxYear)).toString(), Json.toJson(errorBody).toString())

      await(underTest.create(taxYear, nino, aCreateCISDeductions)(hc)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, errorBody))
    }
  }

  ".update" should {
    val url = s"/income-tax/23-24/cis/deductions/$nino/$submissionId"
    "return correct IF response when correct parameters are passed" in {
      stubPutWithoutResponseBody(url, Json.toJson(anUpdateCISDeductions).toString(), NO_CONTENT)

      await(underTest.update(taxYear, nino, submissionId, anUpdateCISDeductions)(hc)) shouldBe Right(())
    }

    "return IF error when left is returned" in {
      val api = ApiError(INTERNAL_SERVER_ERROR, errorBody)

      stubPutWithResponseBody(url, Json.toJson(anUpdateCISDeductions).toString(), api.toJson.toString(), BAD_GATEWAY)

      await(underTest.update(taxYear, nino, submissionId, anUpdateCISDeductions)(hc)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, errorBody))
    }
  }

  ".deleteCisDeductions" should {
    "return correct IF response when correct parameters are passed" in {
      val httpResponse = HttpResponse(NO_CONTENT, "")

      stubDeleteHttpClientCall(s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino/submissionId/$submissionId", httpResponse)

      await(underTest.deleteCisDeductions(taxYear, nino, submissionId)(hc)) shouldBe Right(())
    }

    "return IF error when Left is returned" in {
      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(errorBody).toString())

      stubDeleteHttpClientCall(s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino/submissionId/$submissionId", httpResponse)

      await(underTest.deleteCisDeductions(taxYear, nino, submissionId)(hc)) shouldBe
        Left(ApiError(INTERNAL_SERVER_ERROR, errorBody))
    }
  }
}
