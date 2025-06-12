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

import common.CISSource.{CUSTOMER, CONTRACTOR}
import connectors.{CISDeductionsConnector, HipConnector}
import connectors.errors.ApiError
import models.get.{CISSource, AllCISDeductions}
import models.submission.CISSubmission
import models.{CreateCISDeductionsSuccess, UpdateCISDeductions, CreateCISDeductions}
import uk.gov.hmrc.http.HeaderCarrier
import config.AppConfig
import models.TaxYearPathBindable.{TaxYear, asTys}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TODO: Refactor to use services instead of connector
class CISDeductionsService @Inject()(cisDeductionsConnector: CISDeductionsConnector,
                                     integrationFrameworkService: IntegrationFrameworkService,
                                     hipConnector: HipConnector,
                                     appConfig: AppConfig)
                                    (implicit ec: ExecutionContext) {

  def submitCISDeductions(nino: String, taxYear: Int, cisSubmission: CISSubmission)
                         (implicit hc: HeaderCarrier): Future[Either[ApiError, Option[String]]] = {
    (cisSubmission: @unchecked) match {
      case CISSubmission(Some(employerRef), Some(contractorName), periodData, None) =>
        createCisDeductions(nino, taxYear, CreateCISDeductions(employerRef, contractorName, periodData)).map(response =>
          response.map(success => Some(success.submissionId)))
      case CISSubmission(None, None, periodData, Some(submissionId)) =>
        updateCisDeductions(nino, taxYear, UpdateCISDeductions(periodData = periodData), submissionId)
    }
  }

  def getCISDeductions(nino: String, taxYear: Int)
                      (implicit hc: HeaderCarrier): Future[Either[ApiError, AllCISDeductions]] = {
    getCISDeductions(nino, taxYear, CONTRACTOR).flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right(contractorCISDeductions) => getCISDeductions(nino, taxYear, CUSTOMER).map {
        case Left(error) => Left(error)
        case Right(customerCISDeductions) =>
          Right(AllCISDeductions(customerCISDeductions = customerCISDeductions, contractorCISDeductions = contractorCISDeductions))
      }
    }
  }

  def deleteCISDeductionsSubmission(taxYear: Int,
                                    nino: String,
                                    submissionId: String)
                                   (implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    if (shouldUseIFApi(taxYear)) {
      integrationFrameworkService.deleteCisDeductions(taxYear, nino, submissionId)
    } else {
      cisDeductionsConnector.delete(nino, submissionId)
    }
  }

  private def getCISDeductions(nino: String, taxYear: Int, source: String)(implicit hc: HeaderCarrier): Future[Either[ApiError, Option[CISSource]]] = {
    if (shouldUseIFApi(taxYear)) {
      integrationFrameworkService.getCisDeductions(taxYear, nino, source)
    } else {
      cisDeductionsConnector.get(nino, taxYear, source)
    }
  }

  private def createCisDeductions(nino: String, taxYear: Int, createCisDeductions: CreateCISDeductions)
                                 (implicit hc: HeaderCarrier): Future[Either[ApiError, CreateCISDeductionsSuccess]] = {
    if (appConfig.hipMigration1789Enabled) {
      hipConnector.createCISDeductions(asTys(TaxYear(taxYear)),
        nino, createCisDeductions.employerRef,
        createCisDeductions.contractorName,
        createCisDeductions.periodData.map(_.deductionFromDate).min, createCisDeductions.periodData.map(_.deductionToDate).max, createCisDeductions.periodData.toArray)
    } else if (shouldUseIFApi(taxYear)) {
      integrationFrameworkService.createCisDeductions(taxYear, nino, createCisDeductions)
    } else {
      cisDeductionsConnector.create(nino, taxYear, createCisDeductions)
    }
  }

  private def updateCisDeductions(nino: String, taxYear: Int, updateCisDeductions: UpdateCISDeductions, submissionId: String)
                                 (implicit hc: HeaderCarrier): Future[Either[ApiError, None.type]] = {
    if (shouldUseIFApi(taxYear)) {
      integrationFrameworkService.updateCisDeductions(taxYear, nino, submissionId, updateCisDeductions)
        .map(response => response.map(_ => None))
    } else {
      cisDeductionsConnector.update(nino, submissionId, updateCisDeductions).map(response => response.map(_ => None))
    }
  }

  private def shouldUseIFApi(taxYear: Int): Boolean = {
    taxYear > 2023
  }
}
