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

package connectors.Response

import connectors.errors.{ApiError, SingleErrorBody}
import connectors.response.PostCISDeductionsResponse
import play.api.http.Status._
import play.api.libs.json.{Json, JsValue}
import uk.gov.hmrc.http.HttpResponse
import utils.UnitTest
import connectors.response.PostCISDeductionsResponse.postCISDeductions
import models.CreateCISDeductionsSuccess

class PostCISDeductionsResponseSpec extends UnitTest {
  private val anyHeaders: Map[String, Seq[String]] = Map.empty
  private val anyMethod: String = "POST"
  private val anyUrl = "/income-tax/v1/cis/deductions"

  private val underTest = postCISDeductions

  val cisResponse: CreateCISDeductionsSuccess = CreateCISDeductionsSuccess(
    submissionId = "submission-id"
  )

  "postCISDeductionsResponse" should {
    "convert JsValue to CISDeductionsSuccess" when {
      "status is OK and valid jsValue" in {
        val httpResponse: HttpResponse = HttpResponse.apply(OK, Json.toJson(cisResponse), anyHeaders)
        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe PostCISDeductionsResponse(
          httpResponse,
          Right(cisResponse)
        )
      }

      "status is OK and invalid jsValue" in {
        val jsValue: JsValue = Json.parse(
          """
            |{
            | "not": { "valid": [] }
            |}
            |""".stripMargin
        )
        val httpResponse: HttpResponse = HttpResponse.apply(OK, jsValue, anyHeaders)
        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe PostCISDeductionsResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
        )
      }

      "status is NOT_FOUND and any jsValue" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(NOT_FOUND, jsValue, anyHeaders)
        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe PostCISDeductionsResponse(
          httpResponse,
          Left(ApiError(NOT_FOUND, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is INTERNAL_SERVER_ERROR and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe PostCISDeductionsResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is SERVICE_UNAVAILABLE and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(SERVICE_UNAVAILABLE, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe PostCISDeductionsResponse(
          httpResponse,
          Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is BAD_REQUEST and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(BAD_REQUEST, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe PostCISDeductionsResponse(
          httpResponse,
          Left(ApiError(BAD_REQUEST, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is OTHER and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(FAILED_DEPENDENCY, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe PostCISDeductionsResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }
    }
  }
}
