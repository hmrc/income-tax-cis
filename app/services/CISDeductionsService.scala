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

package services

import common.CISSource.{CONTRACTOR, CUSTOMER}
import connectors.CISDeductionsConnector
import javax.inject.Inject
import models.get.{AllCISDeductions, CISSource}
import models.DesErrorModel
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CISDeductionsService @Inject()(cisDeductionsConnector: CISDeductionsConnector) {

  def getCISDeductions(nino: String, taxYear: Int)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllCISDeductions]] = {

    getContractorCISDeductions(nino,taxYear).flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right(contractorCISDeductions) => getCustomerCISDeductions(nino, taxYear).map {
        case Left(error) => Left(error)
        case Right(customerCISDeductions) => Right(
          AllCISDeductions(
            customerCISDeductions = customerCISDeductions,
            contractorCISDeductions = contractorCISDeductions
          )
        )
      }
    }
  }

  private def getCustomerCISDeductions(nino: String, taxYear: Int)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel,Option[CISSource]]] = {
    cisDeductionsConnector.get(nino, taxYear, CUSTOMER)
  }

  private def getContractorCISDeductions(nino: String, taxYear: Int)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel,Option[CISSource]]] = {
    cisDeductionsConnector.get(nino, taxYear, CONTRACTOR)
  }
}
