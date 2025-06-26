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
import connectors.parsers.CreateCISDeductionsParser.{CreateCISDeductionsResponse, CreateCISDeductionsResponseHttpReads}
import connectors.parsers.DeleteCISDeductionsHttpParser.{DeleteCISDeductionsHttpReads, DeleteCISDeductionsResponse}
import connectors.parsers.GetCISDeductionsHttpParser.{GetCISDeductionsResponse, GetCISDeductionsResponseHttpReads}
import connectors.parsers.UpdateCISDeductionsHttpParser.{UpdateCISDeductionsResponse, UpdateCISDeductionsResponseHttpReads}
import models.{CreateCISDeductions, UpdateCISDeductions}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.CISTaxYearHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CISDeductionsConnector @Inject()(val httpClient: HttpClientV2,
                                       val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesConnector {

  def update(nino: String, submissionId: String, model: UpdateCISDeductions)
            (implicit hc: HeaderCarrier): Future[UpdateCISDeductionsResponse] = {
    val updateUri: String = baseUrl + s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"

    def desCall(implicit hc: HeaderCarrier): Future[UpdateCISDeductionsResponse] = {
      httpClient.put(url"$updateUri")
        .withBody(Json.toJson(model))
        .execute[UpdateCISDeductionsResponse](UpdateCISDeductionsResponseHttpReads, ec)
    }

    desCall(desHeaderCarrier(updateUri))
  }

  def get(nino: String, taxYear: Int, source: String)(implicit hc: HeaderCarrier): Future[GetCISDeductionsResponse] = {

    val cisTaxYear = CISTaxYearHelper.cisTaxYearConverter(taxYear)

    val getUri: String = baseUrl + s"/income-tax/cis/deductions/$nino?periodStart=${cisTaxYear.fromDate}&periodEnd=${cisTaxYear.toDate}&source=$source"

    def desCall(implicit hc: HeaderCarrier): Future[GetCISDeductionsResponse] = {
      httpClient.get(url"$getUri").execute[GetCISDeductionsResponse]
    }

    desCall(desHeaderCarrier(getUri))
  }

  def delete(nino: String, submissionId: String)(implicit hc: HeaderCarrier): Future[DeleteCISDeductionsResponse] = {

    val deleteCISDeductionsUri: String = baseUrl + s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"

    def desCall(implicit hc: HeaderCarrier): Future[DeleteCISDeductionsResponse] = {
      httpClient.delete(url"$deleteCISDeductionsUri").execute[DeleteCISDeductionsResponse](DeleteCISDeductionsHttpReads, ec)
    }

    desCall(desHeaderCarrier(deleteCISDeductionsUri))
  }

  def create(nino: String, taxYear: Int, model: CreateCISDeductions)
            (implicit hc: HeaderCarrier): Future[CreateCISDeductionsResponse] = {
    val createCISDeductionsUri = baseUrl + s"/income-tax/cis/deductions/$nino"

    def desCall(implicit hc: HeaderCarrier): Future[CreateCISDeductionsResponse] = {
      httpClient.post(url"$createCISDeductionsUri")
        .withBody(Json.toJson(model.toApiModel(taxYear)))
        .execute[CreateCISDeductionsResponse]
    }

    desCall(desHeaderCarrier(createCISDeductionsUri))
  }

}
