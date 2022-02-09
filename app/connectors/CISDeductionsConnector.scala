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

package connectors

import config.AppConfig
import connectors.httpParsers.CreateCISDeductionsParser.{CreateCISDeductionsResponseHttpReads, CreateCISDeductionsResponse}
import connectors.httpParsers.UpdateCISDeductionsHttpParser.{UpdateCISDeductionsResponse, UpdateCISDeductionsResponseHttpReads}
import connectors.httpParsers.DeleteCISDeductionsHttpParser.{DeleteCISDeductionsHttpReads, DeleteCISDeductionsResponse}
import models.{CreateCISDeductionsApiModel, CreateCISDeductionsModel, UpdateCISDeductions}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CISDeductionsConnector @Inject()(val http: HttpClient,
                                       val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesConnector {

  def update(nino: String, submissionId: String, model: UpdateCISDeductions)
            (implicit hc: HeaderCarrier): Future[UpdateCISDeductionsResponse] = {

    val updateCISDeductionsUri: String = appConfig.desBaseUrl + s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"

    def desCall(implicit hc: HeaderCarrier): Future[UpdateCISDeductionsResponse] = {
      http.PUT[UpdateCISDeductions, UpdateCISDeductionsResponse](
        updateCISDeductionsUri, model)(UpdateCISDeductions.format.writes, UpdateCISDeductionsResponseHttpReads, hc, ec)
    }

    desCall(desHeaderCarrier(updateCISDeductionsUri))
  }

  def delete(nino: String, submissionId: String)(implicit hc: HeaderCarrier): Future[DeleteCISDeductionsResponse] = {

    val deleteCISDeductionsUri: String = appConfig.desBaseUrl + s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"

    def desCall(implicit hc: HeaderCarrier): Future[DeleteCISDeductionsResponse] = {
      http.DELETE[DeleteCISDeductionsResponse](deleteCISDeductionsUri)(DeleteCISDeductionsHttpReads, hc, ec)
    }

    desCall(desHeaderCarrier(deleteCISDeductionsUri))
  }

  def create(nino: String, taxYear: Int, model: CreateCISDeductionsModel)
            (implicit hc: HeaderCarrier): Future[CreateCISDeductionsResponse] = {
    val createCISDeductionsUri = appConfig.desBaseUrl + s"/income-tax/cis/deductions/$nino"

    def desCall(implicit hc: HeaderCarrier): Future[CreateCISDeductionsResponse] = {
      http.POST[CreateCISDeductionsApiModel, CreateCISDeductionsResponse](
        createCISDeductionsUri, model.toApiModel(taxYear))(CreateCISDeductionsApiModel.format.writes, CreateCISDeductionsResponseHttpReads, hc,ec)
    }

    desCall(desHeaderCarrier(createCISDeductionsUri))
  }

}
