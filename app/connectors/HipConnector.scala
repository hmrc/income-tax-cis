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

package connectors

import config.AppConfig
import connectors.Connector.hcWithCorrelationId
import connectors.errors.ApiError
import connectors.response.PostCISDeductionsResponse
import models.requests.HipCISDeductionsRequest
import models.{CreateCISDeductionsSuccess, PeriodData}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{StringContextOps, HeaderCarrier, HeaderNames}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HipConnector @Inject()(
  http: HttpClientV2,
  appConfig: AppConfig
)(implicit ec: ExecutionContext) {
  lazy val logger: Logger = LoggerFactory.getLogger("hip-connector")

  // HIP API#1789
  def createCISDeductions(
                                        taxYear: String,
                                        nino: String,
                                        employerRef: String,
                                        contractorName: String,
                                        fromDate: String,
                                        toDate: String,
                                        periodData: Seq[PeriodData]
                                      )(implicit hc: HeaderCarrier): Future[Either[ApiError, CreateCISDeductionsSuccess]] = {
    val hipApiVersion: String = "1789"
    val url = s"${appConfig.hipBaseUrl}/income-tax/v1/$taxYear/cis/deductions/$nino"

    val requestBody = HipCISDeductionsRequest(
      employerRef = employerRef,
      contractorName = contractorName,
      fromDate = fromDate,
      toDate = toDate,
      periodData = periodData.toArray
    )

    logger.debug(s"[HipConnector] Calling createCISDeductions with url: $url, body: ${Json.toJson(requestBody)}")

    http
      .post(url"$url")(hcWithCorrelationId(hc))
      .setHeader("Environment" -> appConfig.hipEnvironment)
      .setHeader(HeaderNames.authorisation -> s"Bearer ${appConfig.hipAuthTokenFor(hipApiVersion)}")
      .withBody[HipCISDeductionsRequest](requestBody)
      .execute[PostCISDeductionsResponse]
      .map { response: PostCISDeductionsResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            s"[HipConnector] Error creating a CIS deduction from the HIP Integration Framework: URL: $url" +
              s" correlationId: $correlationId; status: ${response.httpResponse.status}; Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }
}
