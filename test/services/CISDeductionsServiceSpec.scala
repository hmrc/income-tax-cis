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
import connectors.errors.{ApiError, SingleErrorBody}
import models._
import models.get.AllCISDeductions
import play.api.http.Status.INTERNAL_SERVER_ERROR
import support.UnitTest
import support.builders.CISSourceBuilder.{contractorCISSource, customerCISSource}
import support.builders.CISSubmissionBuilder.aCISSubmission
import support.builders.CreateCISDeductionsBuilder.aCreateCISDeductions
import support.mocks.{MockCISDeductionsConnector, MockIntegrationFrameworkService}
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class CISDeductionsServiceSpec extends UnitTest
  with MockCISDeductionsConnector
  with MockIntegrationFrameworkService
  with TaxYearProvider {

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val nino = "AA66666B"
  private val taxYear2023_24 = 2024

  private val underTest = new CISDeductionsService(
    mockCISDeductionsConnector,
    mockIntegrationFrameworkService
  )

  "submitCISDeductions" should {
    "return an error from the create contractor call" in {
      mockCreate(nino, taxYear, aCreateCISDeductions, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.submitCISDeductions(nino, taxYear, aCISSubmission.copy(submissionId = None))) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
    }

    "return an id from the create contractor call" in {
      mockCreate(nino, taxYear, aCreateCISDeductions, Right(CreateCISDeductionsSuccess("id")))

      await(underTest.submitCISDeductions(nino, taxYear, aCISSubmission.copy(submissionId = None))) shouldBe Right(Some("id"))
    }

    "return None from the update contractor call" in {
      mockUpdate(nino, aCISSubmission.submissionId.get, UpdateCISDeductions(aCISSubmission.periodData), Right(()))

      await(underTest.submitCISDeductions(nino, taxYear, aCISSubmission.copy(employerRef = None, contractorName = None))) shouldBe Right(None)
    }

    "return an error from the update contractor call" in {
      mockUpdate(nino, aCISSubmission.submissionId.get, UpdateCISDeductions(aCISSubmission.periodData),
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.submitCISDeductions(nino, taxYear, aCISSubmission.copy(employerRef = None, contractorName = None))) shouldBe
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
    }
  }

  "getCISDeductions" should {
    "when DES Connector is used" when {
      "return an error from the first contractor call" in {
        mockGet(nino, taxYear, CONTRACTOR, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

        await(underTest.getCISDeductions(nino, taxYear)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      }

      "return an error from the second customer call" in {
        mockGet(nino, taxYear, CUSTOMER, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))
        mockGet(nino, taxYear, CONTRACTOR, Right(Some(contractorCISSource(taxYear))))

        await(underTest.getCISDeductions(nino, taxYear)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      }

      "return both customer and contractor data" in {
        mockGet(nino, taxYear, CUSTOMER, Right(Some(customerCISSource(taxYear))))
        mockGet(nino, taxYear, CONTRACTOR, Right(Some(contractorCISSource(taxYear))))

        await(underTest.getCISDeductions(nino, taxYear)) shouldBe Right(AllCISDeductions(Some(customerCISSource(taxYear)), Some(contractorCISSource(taxYear))))
      }
    }

    "when IF Connector is used" when {
      val taxYearForIF = 2024
      "return an error from the first contractor call" in {
        mockGetCisDeductions(taxYearForIF, nino, CONTRACTOR, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

        await(underTest.getCISDeductions(nino, taxYearForIF)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      }

      "return an error from the second customer call" in {
        mockGetCisDeductions(taxYearForIF, nino, CUSTOMER, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))
        mockGetCisDeductions(taxYearForIF, nino, CONTRACTOR, Right(Some(contractorCISSource(taxYear))))

        await(underTest.getCISDeductions(nino, taxYearForIF)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      }

      "return both customer and contractor data" in {
        mockGetCisDeductions(taxYearForIF, nino, CUSTOMER, Right(Some(customerCISSource(taxYear))))
        mockGetCisDeductions(taxYearForIF, nino, CONTRACTOR, Right(Some(contractorCISSource(taxYear))))

        await(underTest.getCISDeductions(nino, taxYearForIF)) shouldBe Right(AllCISDeductions(Some(customerCISSource(taxYear)), Some(contractorCISSource(taxYear))))
      }
    }
  }

  ".deleteCISDeductionsSubmission" should {
    "delegate to IF Connector when tax year is 2023-24 and return the result" in {
      mockDeleteCisDeductions(taxYear2023_24, nino, "submissionId", Right(()))

      await(underTest.deleteCISDeductionsSubmission(taxYear2023_24, nino, "submissionId")) shouldBe Right(())
    }

    "delegate to DES Connector when tax year is different than 2023-24" in {
      mockDelete(nino, "submissionId", Right(()))

      await(underTest.deleteCISDeductionsSubmission(taxYear, nino, "submissionId")) shouldBe Right(())
    }
  }
}
