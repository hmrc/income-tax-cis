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
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import support.UnitTest
import uk.gov.hmrc.http.HttpResponse

class DeleteCISDeductionsHttpParserSpec extends UnitTest {

  private val anyHeaders: Map[String, Seq[String]] = Map.empty
  private val anyMethod: String = "DELETE"
  private val anyUrl = "/any-url"
  private val singleErrorBody: SingleErrorBody = SingleErrorBody("some-code", "some-reason")
  private val singleErrorBodyJson: JsValue = Json.toJson(singleErrorBody)

  private val underTest = DeleteCISDeductionsHttpParser.DeleteCISDeductionsHttpReads

  ".DeleteCISDeductionsHttpReads" should {
    "convert JsValue to DeleteCISDeductionsResponse" when {
      "status is NO_CONTENT and any jsValue" in {
        val httpResponse = HttpResponse.apply(NO_CONTENT, "", anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Right(())
      }

      "status is BAD_REQUEST" in {
        val httpResponse = HttpResponse.apply(BAD_REQUEST, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(BAD_REQUEST, singleErrorBody))
      }

      "status is NOT_FOUND" in {
        val httpResponse = HttpResponse.apply(NOT_FOUND, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(NOT_FOUND, singleErrorBody))
      }

      "status is UNPROCESSABLE_ENTITY" in {
        val httpResponse = HttpResponse.apply(UNPROCESSABLE_ENTITY, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(UNPROCESSABLE_ENTITY, singleErrorBody))
      }

      "status is SERVICE_UNAVAILABLE" in {
        val httpResponse = HttpResponse.apply(SERVICE_UNAVAILABLE, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(SERVICE_UNAVAILABLE, singleErrorBody))
      }

      "status is INTERNAL_SERVER_ERROR" in {
        val httpResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))
      }

      "status is any other error" in {
        val httpResponse = HttpResponse.apply(HTTP_VERSION_NOT_SUPPORTED, singleErrorBodyJson, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))
      }
    }
  }
}
