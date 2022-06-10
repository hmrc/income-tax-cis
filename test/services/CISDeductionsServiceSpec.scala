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

import builders.CISSourceBuilder.{contractorCISSource, customerCISSource}
import builders.CISSubmissionBuilder.{aCreateCISSubmission, anUpdateCISSubmission}
import common.CISSource.{CONTRACTOR, CUSTOMER}
import connectors.CISDeductionsConnector
import connectors.httpParsers.GetCISDeductionsHttpParser.GetCISDeductionsResponse
import models._
import models.get.AllCISDeductions
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class CISDeductionsServiceSpec extends TestUtils {
  private val mockCISDeductionsConnector = mock[CISDeductionsConnector]

  private val underTest = new CISDeductionsService(
    mockCISDeductionsConnector
  )

  private def mockGetCISDeductions(nino: String,
                                   taxYear: Int,
                                   source: String,
                                   connectorResult: GetCISDeductionsResponse) = {
    (mockCISDeductionsConnector.get(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(nino, taxYear, source, *)
      .returning(Future.successful(connectorResult))
  }

  private def mockUpdateCISDeductions(nino: String,
                                      submissionId: String,
                                      connectorResult: Either[DesErrorModel, Unit]) = {
    (mockCISDeductionsConnector.update(_: String, _: String, _: UpdateCISDeductions)(_: HeaderCarrier))
      .expects(nino, submissionId, *, *)
      .returning(Future.successful(connectorResult))
  }

  private def mockCreateCISDeductions(nino: String,
                                      taxYear: Int,
                                      connectorResult: Either[DesErrorModel, CreateCISDeductionsSuccess]) = {
    (mockCISDeductionsConnector.create(_: String, _: Int, _: CreateCISDeductions)(_: HeaderCarrier))
      .expects(nino, taxYear, *, *)
      .returning(Future.successful(connectorResult))
  }

  private val nino = "AA66666B"
  private val taxYear = 2022

  "submitCISDeductions" should {
    "return an error from the create contractor call" in {

      mockCreateCISDeductions(nino, taxYear, Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)))

      val result = underTest.submitCISDeductions(nino, taxYear, aCreateCISSubmission)

      await(result) mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))
    }
    "return an id from the create contractor call" in {

      mockCreateCISDeductions(nino, taxYear, Right(CreateCISDeductionsSuccess("id")))

      val result = underTest.submitCISDeductions(nino, taxYear, aCreateCISSubmission)

      await(result) mustBe Right(Some("id"))
    }
    "return None from the update contractor call" in {

      mockUpdateCISDeductions(nino, anUpdateCISSubmission.submissionId.get, Right(()))

      val result = underTest.submitCISDeductions(nino, taxYear, anUpdateCISSubmission)

      await(result) mustBe Right(None)
    }
    "return an error from the update contractor call" in {

      mockUpdateCISDeductions(nino, anUpdateCISSubmission.submissionId.get, Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)))

      val result = underTest.submitCISDeductions(nino, taxYear, anUpdateCISSubmission)

      await(result) mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))
    }
  }

  "getCISDeductions" should {
    "return an error from the first contractor call" in {
      mockGetCISDeductions(nino, taxYear, CONTRACTOR, Left(DesErrorModel(
        INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError
      )))

      await(underTest.getCISDeductions(nino, taxYear)) mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))
    }

    "return an error from the second customer call" in {
      mockGetCISDeductions(nino, taxYear, CUSTOMER, Left(DesErrorModel(
        INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError
      )))
      mockGetCISDeductions(nino, taxYear, CONTRACTOR, Right(Some(contractorCISSource(taxYear))))

      await(underTest.getCISDeductions(nino, taxYear)) mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))
    }

    "return both customer and contractor data" in {
      mockGetCISDeductions(nino, taxYear, CUSTOMER, Right(Some(customerCISSource(taxYear))))
      mockGetCISDeductions(nino, taxYear, CONTRACTOR, Right(Some(contractorCISSource(taxYear))))

      await(underTest.getCISDeductions(nino, taxYear)) mustBe Right(AllCISDeductions(Some(customerCISSource(taxYear)), Some(contractorCISSource(taxYear))))
    }

    "return no data for customer and contractor" in {
      mockGetCISDeductions(nino, taxYear, CUSTOMER, Right(None))
      mockGetCISDeductions(nino, taxYear, CONTRACTOR, Right(None))

      await(underTest.getCISDeductions(nino, taxYear)) mustBe Right(AllCISDeductions(None, None))
    }

    "return customer data" in {
      mockGetCISDeductions(nino, taxYear, CUSTOMER, Right(Some(customerCISSource(taxYear))))
      mockGetCISDeductions(nino, taxYear, CONTRACTOR, Right(None))

      await(underTest.getCISDeductions(nino, taxYear)) mustBe Right(AllCISDeductions(Some(customerCISSource(taxYear)), None))
    }

    "return contractor data" in {
      mockGetCISDeductions(nino, taxYear, CUSTOMER, Right(None))
      mockGetCISDeductions(nino, taxYear, CONTRACTOR, Right(Some(contractorCISSource(taxYear))))

      await(underTest.getCISDeductions(nino, taxYear)) mustBe Right(AllCISDeductions(None, Some(contractorCISSource(taxYear))))
    }
  }
}
