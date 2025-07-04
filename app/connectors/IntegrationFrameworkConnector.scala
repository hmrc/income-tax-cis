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

package connectors

import config.AppConfig
import connectors.errors.ApiError
import connectors.parsers.CreateCISDeductionsParser.{CreateCISDeductionsResponse, CreateCISDeductionsResponseHttpReads}
import connectors.parsers.DeleteCISDeductionsHttpParser.{DeleteCISDeductionsHttpReads, DeleteCISDeductionsResponse}
import connectors.parsers.GetCISDeductionsHttpParser.{GetCISDeductionsResponse, GetCISDeductionsResponseHttpReads}
import connectors.parsers.UpdateCISDeductionsHttpParser.{UpdateCISDeductionsResponse, UpdateCISDeductionsResponseHttpReads}
import models.{CreateCISDeductions, UpdateCISDeductions}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.CISTaxYearHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IntegrationFrameworkConnector @Inject()(httpClient: HttpClientV2,
                                              appConf: AppConfig)
                                             (implicit ec: ExecutionContext) extends IFConnector {

  private val GET_API_VERSION = "1792"
  private val CREATE_API_VERSION = "1789"
  private val UPDATE_API_VERSION = "1791"
  private val DELETE_API_VERSION = "1790"

  override protected[connectors] val appConfig: AppConfig = appConf

  def getCisDeductions(taxYear: Int,
                       nino: String,
                       source: String)
                      (implicit hc: HeaderCarrier): Future[GetCISDeductionsResponse] = {
    val cisTaxYear = CISTaxYearHelper.cisTaxYearConverter(taxYear)
    val url = s"$baseUrl/income-tax/cis/deductions/${taxYearParam(taxYear, GET_API_VERSION)}" +
      s"/$nino?startDate=${cisTaxYear.fromDate}&endDate=${cisTaxYear.toDate}&source=$source"

    def ifCall(implicit hc: HeaderCarrier): Future[GetCISDeductionsResponse] = {
      httpClient.get(url"$url").execute[GetCISDeductionsResponse]
    }

    ifCall(ifHeaderCarrier(url, GET_API_VERSION))
  }

  def create(taxYear: Int, nino: String, model: CreateCISDeductions)
            (implicit hc: HeaderCarrier): Future[CreateCISDeductionsResponse] = {
    val url = s"$baseUrl/income-tax/${taxYearParam(taxYear, CREATE_API_VERSION)}/cis/deductions/$nino"

    def ifCall(implicit hc: HeaderCarrier): Future[CreateCISDeductionsResponse] = {
      httpClient.post(url"$url")
        .withBody(Json.toJson(model.toApiModel(taxYear)))
        .execute[CreateCISDeductionsResponse]
    }

    ifCall(ifHeaderCarrier(url, CREATE_API_VERSION))
  }

  def update(taxYear: Int, nino: String, submissionId: String, model: UpdateCISDeductions)
            (implicit hc: HeaderCarrier): Future[UpdateCISDeductionsResponse] = {
    val url = s"$baseUrl/income-tax/${taxYearParam(taxYear, UPDATE_API_VERSION)}/cis/deductions/$nino/$submissionId"

    def ifCall(implicit hc: HeaderCarrier): Future[UpdateCISDeductionsResponse] = {
      httpClient.put(url"$url")
        .withBody(Json.toJson(model))
        .execute[UpdateCISDeductionsResponse](UpdateCISDeductionsResponseHttpReads, ec)
    }

    ifCall(ifHeaderCarrier(url, UPDATE_API_VERSION))
  }

  def deleteCisDeductions(taxYear: Int,
                          nino: String,
                          submissionId: String)
                         (implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    val url = baseUrl + s"/income-tax/cis/deductions/${taxYearParam(taxYear, DELETE_API_VERSION)}/$nino/submissionId/$submissionId"

    def ifCall(implicit hc: HeaderCarrier): Future[DeleteCISDeductionsResponse] = {
      httpClient.delete(url"$url").execute[DeleteCISDeductionsResponse](DeleteCISDeductionsHttpReads, ec)
    }

    ifCall(ifHeaderCarrier(url, DELETE_API_VERSION))
  }

  private def taxYearParam(taxYear: Int, apiVersion: String): String = {
    lazy val taxYearParam = s"${(taxYear - 1).toString takeRight 2}-${taxYear.toString takeRight 2}"

    apiVersion match {
      case GET_API_VERSION | DELETE_API_VERSION => taxYearParam
      case UPDATE_API_VERSION | CREATE_API_VERSION => "23-24"
      case _ => throw new NotImplementedError
    }
  }
}
