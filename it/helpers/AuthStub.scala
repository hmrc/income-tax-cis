/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package helpers

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel}

trait AuthStub {

  private val authoriseUri: String = "/auth/authorise"
  private val AGENT_ENROLMENT_KEY = "HMRC-AS-AGENT"

  val otherEnrolment: JsObject = Json.obj(
    "key" -> "HMRC-OTHER-ENROLMENT",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "OTHERID",
        "value" -> "555555555"
      )
    )
  )
  private val agentEnrolment = Json.obj(
    "key" -> AGENT_ENROLMENT_KEY,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "AgentReferenceNumber",
        "value" -> "1234567890"
      )
    )
  )

  private val mtditEnrolment = Json.obj(
    "key" -> "HMRC-MTD-IT",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "MTDITID",
        "value" -> "555555555"
      )
    )
  )

  private val ninoEnrolment = Json.obj(
    "key" -> "HMRC-NI",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "NINO",
        "value" -> "AA123123A"
      )
    )
  )

  private def successfulAuthResponse(affinityGroup: Option[AffinityGroup], confidenceLevel: Option[ConfidenceLevel], enrolments: JsObject*): JsObject = {
    affinityGroup.fold(Json.obj())(unwrappedAffinityGroup => Json.obj("affinityGroup" -> unwrappedAffinityGroup)) ++
      confidenceLevel.fold(Json.obj())(unwrappedConfidenceLevel => Json.obj("confidenceLevel" -> unwrappedConfidenceLevel)) ++
      Json.obj("allEnrolments" -> enrolments)
  }

  def authorised(response: JsObject = successfulAuthResponse(Some(Individual), Some(ConfidenceLevel.L200), mtditEnrolment, ninoEnrolment)): StubMapping = {
    stubFor(post(urlMatching(authoriseUri))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(response.toString())
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def agentAuthorised(): StubMapping = {
    stubFor(post(urlMatching(authoriseUri))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(successfulAuthResponse(Some(Agent), None, agentEnrolment).toString())
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def unauthorisedOtherEnrolment(): StubMapping = {
    stubFor(post(urlMatching(authoriseUri))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(successfulAuthResponse(Some(Individual), Some(ConfidenceLevel.L200), otherEnrolment).toString())
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def insufficientEnrolments(): StubMapping = {
    stubFor(post(urlMatching(authoriseUri))
      .willReturn(
        aResponse()
          .withStatus(UNAUTHORIZED)
          .withBody(successfulAuthResponse(Some(Agent), None, agentEnrolment).toString())
          .withHeader("WWW-Authenticate", """MDTP detail="InsufficientEnrolments""")
      ))
  }

  def partialsAuthResponse(enrolments: JsObject*): JsObject = {
    Json.obj("authorisedEnrolments" -> enrolments)
  }
}
