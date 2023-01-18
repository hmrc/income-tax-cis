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

import common.CISSource.{CONTRACTOR, CUSTOMER}
import connectors.CISDeductionsConnector
import connectors.errors.ApiError
import models.get.{AllCISDeductions, CISSource}
import models.submission.CISSubmission
import models.{CreateCISDeductions, UpdateCISDeductions}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TODO: Refactor to use services instead of connector
class CISDeductionsService @Inject()(cisDeductionsConnector: CISDeductionsConnector,
                                     integrationFrameworkService: IntegrationFrameworkService)
                                    (implicit ec: ExecutionContext) {

  def submitCISDeductions(nino: String, taxYear: Int, cisSubmission: CISSubmission)
                         (implicit hc: HeaderCarrier): Future[Either[ApiError, Option[String]]] = {
    (cisSubmission: @unchecked) match {
      case CISSubmission(Some(employerRef), Some(contractorName), periodData, None) =>
        cisDeductionsConnector.create(nino, taxYear, CreateCISDeductions(employerRef, contractorName, periodData)).map(response =>
          response.map(success => Some(success.submissionId)))
      case CISSubmission(None, None, periodData, Some(submissionId)) =>
        cisDeductionsConnector.update(nino, submissionId, UpdateCISDeductions(periodData = periodData)).map(response => response.map(_ => None))
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

  private def shouldUseIFApi(taxYear: Int): Boolean = {
    taxYear - 1 == 2023
  }

  private def getCISDeductions(nino: String, taxYear: Int, source: String)(implicit hc: HeaderCarrier): Future[Either[ApiError, Option[CISSource]]] = {
    if (shouldUseIFApi(taxYear)) {
      integrationFrameworkService.getCisDeductions(taxYear, nino, source)
    } else {
      cisDeductionsConnector.get(nino, taxYear, source)
    }
  }
}
