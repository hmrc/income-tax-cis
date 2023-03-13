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

package services

import connectors.IntegrationFrameworkConnector
import connectors.errors.ApiError
import models.{CreateCISDeductions, CreateCISDeductionsSuccess, UpdateCISDeductions}
import models.get.CISSource
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class IntegrationFrameworkService @Inject()(integrationFrameworkConnector: IntegrationFrameworkConnector) {

  def getCisDeductions(taxYear: Int,
                       nino: String,
                       source: String)
                      (implicit hc: HeaderCarrier): Future[Either[ApiError, Option[CISSource]]] = {
    integrationFrameworkConnector.getCisDeductions(taxYear, nino, source)
  }

  def createCisDeductions(taxYear: Int,
                          nino: String,
                          createCISDeductions: CreateCISDeductions)
                         (implicit hc: HeaderCarrier): Future[Either[ApiError, CreateCISDeductionsSuccess]] = {
    integrationFrameworkConnector.create(taxYear, nino, createCISDeductions)
  }

  def updateCisDeductions(taxYear: Int,
                          nino: String,
                          submissionId: String,
                          updateCISDeductions: UpdateCISDeductions)
                         (implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    integrationFrameworkConnector.update(taxYear, nino, submissionId, updateCISDeductions)
  }

  def deleteCisDeductions(taxYear: Int,
                          nino: String,
                          submissionId: String)
                         (implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    integrationFrameworkConnector.deleteCisDeductions(taxYear, nino, submissionId)
  }
}
