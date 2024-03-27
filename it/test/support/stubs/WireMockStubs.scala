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

package support.stubs

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.ContentTypes.JSON
import play.api.libs.json.JsValue
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HttpResponse

// TODO: Refactor
trait WireMockStubs {

  def stubGetHttpClientCall(url: String,
                            httpResponse: HttpResponse,
                            requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingBuilder = get(urlMatching(url))
    getStubMapping(httpResponse, requestHeaders, mappingBuilder)
  }

  def stubDeleteHttpClientCall(url: String,
                               httpResponse: HttpResponse,
                               requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingBuilder = delete(urlMatching(url))
    getStubMapping(httpResponse, requestHeaders, mappingBuilder)
  }

  def stubGetWithResponseBody(url: String, status: Int, response: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(get(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubGetWithoutResponseBody(url: String, status: Int): StubMapping =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)))

  def stubPostWithoutResponseBody(url: String, status: Int, requestBody: String): StubMapping =
    stubFor(post(urlEqualTo(url)).withRequestBody(equalToJson(requestBody))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json; charset=utf-8")))

  def stubPostWithResponseBody(url: String, status: Int, requestBody: String, response: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(post(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders.withRequestBody(equalToJson(requestBody))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubPutWithoutResponseBody(url: String, requestBody: String, status: Int, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(put(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders.withRequestBody(equalToJson(requestBody))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubPutWithResponseBody(url: String, requestBody: String, responseBody: String, status: Int, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(put(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders.withRequestBody(equalToJson(requestBody))
      .willReturn(
        aResponse()
          .withBody(responseBody)
          .withStatus(status)
          .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubPatchWithoutResponseBody(url: String, requestBody: String, status: Int): StubMapping =
    stubFor(patch(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json; charset=utf-8")))

  def stubPostWithoutResponseAndRequestBody(url: String, status: Int): StubMapping =
    stubFor(post(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json; charset=utf-8")))

  def verifyPostWithRequestBody(url: String, times: Int, body: JsValue): Unit =
    verify(times, postRequestedFor(urlEqualTo(url))
      .withRequestBody(equalToJson(body.toString(), true, true))
    )

  def auditStubs(): Unit = {
    val auditResponseCode = 204
    stubPostWithoutResponseAndRequestBody("/write/audit", auditResponseCode)
  }

  def stubDeleteWithoutResponseBody(url: String, status: Int, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(delete(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)))
  }

  def stubDeleteWithResponseBody(url: String, status: Int, response: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(delete(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
      ))
  }

  private def getStubMapping(httpResponse: HttpResponse,
                             requestHeaders: Seq[HttpHeader],
                             mappingBuilder: MappingBuilder): StubMapping = {
    val responseBuilder = aResponse()
      .withStatus(httpResponse.status)
      .withBody(httpResponse.body)
      .withHeader(CONTENT_TYPE, JSON)

    val mappingBuilderWithHeaders: MappingBuilder = requestHeaders
      .foldLeft(mappingBuilder)((result, nxt) => result.withHeader(nxt.key(), equalTo(nxt.firstValue())))

    stubFor(mappingBuilderWithHeaders.willReturn(responseBuilder))
  }
}
