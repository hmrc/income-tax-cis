/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import uk.gov.hmrc.http.HeaderNames._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionId}
import utils.TestUtils

class DesConnectorSpec extends TestUtils{

  class FakeConnector(override val appConfig: AppConfig) extends DesConnector {
    def headerCarrierTest(url: String)(hc: HeaderCarrier): HeaderCarrier = desHeaderCarrier(url)(hc)
  }
  val connector = new FakeConnector(appConfig = mockAppConfig)

  "FakeConnector" when {

    "host is Internal" should {
      val internalHost = "http://localhost"

      "add the correct authorization" in {
        val hc = HeaderCarrier()
        val result = connector.headerCarrierTest(internalHost)(hc)
        result.authorization mustBe Some(Authorization(s"Bearer ${mockAppConfig.authorisationToken}"))
      }

      "add the correct environment" in {
        val hc = HeaderCarrier()
        val result = connector.headerCarrierTest(internalHost)(hc)
        result.extraHeaders mustBe List(
          "Environment" -> mockAppConfig.environment
        )
      }
    }

    "host is External" should {
      val externalHost = "http://127.0.0.1"

      "include all HeaderCarrier headers in the extraHeaders when the host is external" in {
        val hc = HeaderCarrier(sessionId = Some(SessionId("sessionIdHeaderValue")))
        val result = connector.headerCarrierTest(externalHost)(hc)

        result.extraHeaders.size mustBe  4
        result.extraHeaders.contains(xSessionId -> "sessionIdHeaderValue") mustBe true
        result.extraHeaders.contains(authorisation -> s"Bearer ${mockAppConfig.authorisationToken}") mustBe true
        result.extraHeaders.contains("Environment" -> mockAppConfig.environment) mustBe true
        result.extraHeaders.exists(x => x._1.equalsIgnoreCase(xRequestChain)) mustBe true
      }
    }
  }

}
