/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors.httpParsers

import models.{DesErrorBodyModel, DesErrorModel, DesErrorsBodyModel}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtils

class DESParserSpec extends TestUtils{

  object FakeParser extends DESParser {
    override val parserName: String = "TestParser"
  }

  def httpResponse(json: JsValue =
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
      val result = FakeParser.logMessage(httpResponse())
      result mustBe
        """[TestParser][read] Received 500 from DES. Body:{
          |  "failures" : [ {
          |    "code" : "SERVICE_UNAVAILABLE",
          |    "reason" : "The service is currently unavailable"
          |  }, {
          |    "code" : "INTERNAL_SERVER_ERROR",
          |    "reason" : "The service is currently facing issues."
          |  } ]
          |} CorrelationId: 1234645654645""".stripMargin
    }
    "return the the correct error" in {
      val result = FakeParser.badSuccessJsonFromDES
      result mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("PARSING_ERROR", "Error parsing response from DES")))
    }
    "handle multiple errors" in {
      val result = FakeParser.handleDESError(httpResponse())
      result mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorsBodyModel(Seq(
        DesErrorBodyModel("SERVICE_UNAVAILABLE", "The service is currently unavailable"),
        DesErrorBodyModel("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")
      ))))
    }
    "handle single errors" in {
      val result = FakeParser.handleDESError(httpResponse(Json.parse(
        """{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}""".stripMargin)))
      result mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")))
    }

    "handle response that is neither a single error or multiple errors" in {
      val result = FakeParser.handleDESError(httpResponse(Json.obj()))
      result mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("PARSING_ERROR", "Error parsing response from DES")))
    }

    "handle response when the response body is not json" in {
      val result = FakeParser.handleDESError(HttpResponse(INTERNAL_SERVER_ERROR, "", Map("CorrelationId" -> Seq("1234645654645"))))
      result mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("PARSING_ERROR", "Error parsing response from DES")))
    }

  }
}