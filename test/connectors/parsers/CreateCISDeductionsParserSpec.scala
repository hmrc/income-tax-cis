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

package connectors.parsers

import connectors.errors.{ApiError, SingleErrorBody}
import models.CreateCISDeductionsSuccess
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import support.UnitTest
import support.builders.CISSubmissionBuilder.aCISSubmission
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.HttpResponse

class CreateCISDeductionsParserSpec extends UnitTest
  with TaxYearProvider {

  private val anyHeaders: Map[String, Seq[String]] = Map.empty
  private val anyMethod: String = "POST"
  private val anyUrl = "/any-url"
  private val singleErrorBody: SingleErrorBody = SingleErrorBody("some-code", "some-reason")
  private val singleErrorBodyJson: JsValue = Json.toJson(singleErrorBody)

  private val underTest = CreateCISDeductionsParser.CreateCISDeductionsResponseHttpReads

  "CreateCISDeductionsResponseHttpReads" should {
    "convert JsValue to CreateCISDeductionsResponse" when {
      "status is OK and any jsValue" in {
        val success = CreateCISDeductionsSuccess(aCISSubmission.submissionId.get)
        val httpResponse = HttpResponse.apply(OK, Json.toJson(success).toString, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Right(success)
      }

      "status is CREATED and any jsValue" in {
        val success = CreateCISDeductionsSuccess(aCISSubmission.submissionId.get)
        val httpResponse = HttpResponse.apply(CREATED, Json.toJson(success).toString, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Right(success)
      }

      "status is INTERNAL_SERVER_ERROR" in {
        val httpResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))
      }

      "status is SERVICE_UNAVAILABLE" in {
        val httpResponse = HttpResponse.apply(SERVICE_UNAVAILABLE, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(SERVICE_UNAVAILABLE, singleErrorBody))
      }

      "status is BAD_REQUEST" in {
        val httpResponse = HttpResponse.apply(BAD_REQUEST, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(BAD_REQUEST, singleErrorBody))
      }

      "status is CONFLICT" in {
        val httpResponse = HttpResponse.apply(CONFLICT, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(CONFLICT, singleErrorBody))
      }

      "status is UNPROCESSABLE_ENTITY" in {
        val httpResponse = HttpResponse.apply(UNPROCESSABLE_ENTITY, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(UNPROCESSABLE_ENTITY, singleErrorBody))
      }

      "status is any other error" in {
        val httpResponse = HttpResponse.apply(HTTP_VERSION_NOT_SUPPORTED, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))
      }
    }
  }
}
