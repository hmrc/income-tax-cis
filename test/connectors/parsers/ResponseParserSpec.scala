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

import connectors.errors.{ApiError, MultiErrorsBody, SingleErrorBody}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import support.UnitTest
import uk.gov.hmrc.http.HttpResponse

class ResponseParserSpec extends UnitTest {

  object FakeParser extends ResponseParser {
    override val parserName: String = "TestParser"
  }

  private def httpResponse(json: JsValue =
                           Json.parse(
                             """{"failures":[
                               |{"code":"SERVICE_UNAVAILABLE","reason":"The service is currently unavailable"},
                               |{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}]}""".stripMargin)): HttpResponse = HttpResponse(
    INTERNAL_SERVER_ERROR,
    json,
    Map("CorrelationId" -> Seq("1234645654645"))
  )

  "FakeParser" should {
    "log the correct message" in {
      val underTest = FakeParser.logMessage(httpResponse())

      underTest shouldBe
        """[TestParser][read] Received 500 from DES/IF. Body:{
          |  "failures" : [ {
          |    "code" : "SERVICE_UNAVAILABLE",
          |    "reason" : "The service is currently unavailable"
          |  }, {
          |    "code" : "INTERNAL_SERVER_ERROR",
          |    "reason" : "The service is currently facing issues."
          |  } ]
          |} CorrelationId: 1234645654645""".stripMargin
    }

    "return the the correct error from DES" in {
      val underTest = FakeParser.badSuccessJsonFromDES

      underTest shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from DES")))
    }

    "return the correct error" in {
      val underTest = FakeParser.badSuccessJsonResponse

      underTest shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from DES")))
    }

    "handle multiple errors" in {
      val underTest = FakeParser.handleError(httpResponse())

      underTest shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, MultiErrorsBody(Seq(
        SingleErrorBody("SERVICE_UNAVAILABLE", "The service is currently unavailable"),
        SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")
      ))))
    }

    "handle single errors" in {
      val underTest = FakeParser.handleError(httpResponse(Json.parse(
        """{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}""".stripMargin)))

      underTest shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")))
    }

    "handle response that is neither a single error or multiple errors" in {
      val underTest = FakeParser.handleError(httpResponse(Json.obj()))

      underTest shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from DES")))
    }

    "handle response when the response body is not json" in {
      val underTest = FakeParser.handleError(HttpResponse(INTERNAL_SERVER_ERROR, "", Map("CorrelationId" -> Seq("1234645654645"))))

      underTest shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from DES")))
    }

    "handle single HiP errors" in {
      val underTest = FakeParser.handleErrorHIP(httpResponse(Json.parse(
        """{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}""".stripMargin)), 500)

      underTest shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")))
    }
  }
}
