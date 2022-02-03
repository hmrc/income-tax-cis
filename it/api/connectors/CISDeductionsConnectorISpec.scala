/*
 * Copyright 2022 HM Revenue & Customs
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

package api.connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.MockAppConfig
import connectors.CISDeductionsConnector
import models._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, SessionId}
import utils.ConnectorIntegrationTest

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class CISDeductionsConnectorISpec extends ConnectorIntegrationTest {

  def connector(desHost: String = "localhost"): CISDeductionsConnector = new CISDeductionsConnector(httpClient, new MockAppConfig().config(desHost))

  val nino: String = "AA123123A"
  val submissionId: String = "a111111a-abcd-111a-123a-11a1a111a1"

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
  val updateCISDeductionsUrl: String = s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"

  val connectorWithInternalHost: CISDeductionsConnector = connector()
  val connectorWithExternalHost:  CISDeductionsConnector = connector("127.0.0.1")

  val updateCISDeductionsModel: UpdateCISDeductions =
    UpdateCISDeductions(
      PeriodData(
        deductionFromDate = "2019-04-06",
        deductionToDate = "2019-05-05",
        grossAmountPaid = Some(129.99),
        deductionAmount = 251.11,
        costOfMaterials = Some(30.00)
      )
    )

  val headersSentToDes = Seq(
    new HttpHeader(HeaderNames.authorisation, "Bearer authorisation-token"),
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  "UpdateCISDeductions" should {

    "include internal headers" when {

      "the host for DES is 'Internal'" in {

        stubPutWithoutResponseBody(
          updateCISDeductionsUrl, Json.toJson(updateCISDeductionsModel).toString(), NO_CONTENT, headersSentToDes
        )

        Await.result(connectorWithInternalHost.updateCISDeductions(nino, submissionId, updateCISDeductionsModel), Duration.Inf) shouldBe Right(())
      }

      "the host for DES is 'External'" in {
        stubPutWithoutResponseBody(
          updateCISDeductionsUrl, Json.toJson(updateCISDeductionsModel).toString(), NO_CONTENT)

        Await.result(connectorWithExternalHost.updateCISDeductions(nino, submissionId, updateCISDeductionsModel), Duration.Inf) shouldBe Right(())
      }
    }

    "handle error" when {
      val desErrorBodyModel = DesErrorBodyModel("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"DES returns $status" in {
          val desError = DesErrorModel(status, desErrorBodyModel)

          stubPutWithResponseBody(updateCISDeductionsUrl, Json.toJson(updateCISDeductionsModel).toString(), desError.toJson.toString(), status)
          Await.result(connectorWithInternalHost.updateCISDeductions(nino, submissionId, updateCISDeductionsModel), Duration.Inf) shouldBe Left(desError)
        }
      }
      s"DES returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, desErrorBodyModel)

        stubPutWithResponseBody(updateCISDeductionsUrl, Json.toJson(updateCISDeductionsModel).toString(), desError.toJson.toString(), BAD_GATEWAY)

        Await.result(connectorWithInternalHost.updateCISDeductions(nino, submissionId, updateCISDeductionsModel), Duration.Inf) shouldBe Left(desError)
      }
    }
  }
}


