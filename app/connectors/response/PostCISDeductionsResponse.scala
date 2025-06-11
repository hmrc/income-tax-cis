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

package connectors.response

import connectors.errors.ApiError
import connectors.parsers.ResponseParser
import models.CreateCISDeductionsSuccess
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

case class PostCISDeductionsResponse(httpResponse: HttpResponse, result: Either[ApiError, CreateCISDeductionsSuccess])

object PostCISDeductionsResponse {

  implicit val postCISDeductions: HttpReads[PostCISDeductionsResponse] = new HttpReads[PostCISDeductionsResponse] with ResponseParser {

    override val parserName: String = this.getClass.getSimpleName

    override def read(method: String, url: String, response: HttpResponse): PostCISDeductionsResponse = response.status match {
      case OK => PostCISDeductionsResponse(response, extractResult(response))
      case NOT_FOUND => PostCISDeductionsResponse(response, handleErrorHIP(response, NOT_FOUND))
      case BAD_REQUEST| CONFLICT | UNPROCESSABLE_ENTITY | INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE =>
        PostCISDeductionsResponse(response, handleErrorHIP(response, response.status))
      case _ => PostCISDeductionsResponse(response, handleErrorHIP(response, INTERNAL_SERVER_ERROR))
    }

    private def extractResult(response: HttpResponse): Either[ApiError, CreateCISDeductionsSuccess] = {
      val json = response.json
      json.validate[CreateCISDeductionsSuccess]
        .fold[Either[ApiError, CreateCISDeductionsSuccess]](_ => badSuccessJsonResponse, parsedModel => Right(parsedModel))
    }
  }

}