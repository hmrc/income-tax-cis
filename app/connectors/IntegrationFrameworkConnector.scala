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
import connectors.parsers.DeleteCISDeductionsHttpParser.{DeleteCISDeductionsHttpReads, DeleteCISDeductionsResponse}
import connectors.parsers.GetCISDeductionsHttpParser.{GetCISDeductionsResponse, GetCISDeductionsResponseHttpReads}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.CISTaxYearHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IntegrationFrameworkConnector @Inject()(httpClient: HttpClient,
                                              appConf: AppConfig)
                                             (implicit ec: ExecutionContext) extends IFConnector {

  private val getApiVersion = "1792"
  private val deleteApiVersion = "1790"

  override protected[connectors] val appConfig: AppConfig = appConf

  def getCisDeductions(taxYear: Int,
                       nino: String,
                       source: String)(implicit hc: HeaderCarrier): Future[GetCISDeductionsResponse] = {
    val cisTaxYear = CISTaxYearHelper.cisTaxYearConverter(taxYear)
    val url =
      baseUrl + s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino?startDate=${cisTaxYear.fromDate}&endDate=${cisTaxYear.toDate}&source=$source"

    def ifCall(implicit hc: HeaderCarrier): Future[GetCISDeductionsResponse] = {
      httpClient.GET[GetCISDeductionsResponse](url)(GetCISDeductionsResponseHttpReads, hc, ec)
    }

    ifCall(ifHeaderCarrier(url, getApiVersion))
  }

  def deleteCisDeductions(taxYear: Int,
                          nino: String,
                          submissionId: String)
                         (implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    val url = baseUrl + s"/income-tax/cis/deductions/${toTaxYearParam(taxYear)}/$nino/submissionId/$submissionId"

    def ifCall(implicit hc: HeaderCarrier): Future[DeleteCISDeductionsResponse] = {
      httpClient.DELETE[DeleteCISDeductionsResponse](url)(DeleteCISDeductionsHttpReads, hc, ec)
    }

    ifCall(ifHeaderCarrier(url, deleteApiVersion))
  }

  private def toTaxYearParam(taxYear: Int): String = {
    s"${(taxYear - 1).toString takeRight 2}-${taxYear.toString takeRight 2}"
  }
}
