/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import common.CISSource.{CONTRACTOR, CUSTOMER}
import models.CISDates
import models.get.{CISDeductions, CISSource, GetPeriodData}
import models.prePopulation.PrePopulationResponse
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.ControllerIntegrationTest
import support.helpers.AuthStub
import support.stubs.WireMockStubs
import uk.gov.hmrc.http.HttpResponse
import utils.CISTaxYearHelper

class PrePopulationControllerISpec extends ControllerIntegrationTest
  with WireMockStubs
  with AuthStub {

  trait Test {
    val nino: String = "AA123123A"
    val taxYear: Int = 2024
    val mtdItId: String = "555555555"
    val ifTaxYearParam = s"${(taxYear - 1).toString.takeRight(2)}-${taxYear.toString.takeRight(2)}"

    val cisTaxYear: CISDates = CISTaxYearHelper.cisTaxYearConverter(taxYear)

    def ifUrl(source: String): String = s"/income-tax/cis/deductions/$ifTaxYearParam/$nino" +
      s"\\?startDate=${cisTaxYear.fromDate}&endDate=${cisTaxYear.toDate}&source=$source"

    def request(): WSRequest = {
      authorised()
      buildRequest(s"/income-tax-cis/pre-population/$nino/$taxYear")
        .withFollowRedirects(false)
        .withHttpHeaders(
          (AUTHORIZATION, "Bearer 123"),
          ("mtditid", mtdItId)
        )
    }
  }

  "/pre-population/:nino/:taxYear" when {
    val notFoundHttpResponse: HttpResponse = HttpResponse(NOT_FOUND, "no teapot found")

    "IF returns a non-404 error when retrieving a user's CIS" should {
      "return an INTERNAL SERVER ERROR response" in new Test {
        stubGetHttpClientCall(ifUrl(CONTRACTOR), HttpResponse(IM_A_TEAPOT, "teapot time"))

        val result: WSResponse = await(request().get())
        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "IF returns a 404 error when retrieving a user's CIS" should {
      "return an empty pre-pop response" in new Test {
        stubGetHttpClientCall(ifUrl(CONTRACTOR), notFoundHttpResponse)
        stubGetHttpClientCall(ifUrl(CUSTOMER), notFoundHttpResponse)

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.noPrePop)
      }
    }

    "IF returns an empty response when retrieving a user's CIS" should {
      "return an empty pre-pop response" in new Test {
        val httpResponse: HttpResponse = HttpResponse(OK, """{"cisDeductions": []}""")
        stubGetHttpClientCall(ifUrl(CONTRACTOR), httpResponse)
        stubGetHttpClientCall(ifUrl(CUSTOMER), httpResponse)

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.noPrePop)
      }
    }

    "IF returns a zeroed response when retrieving a user's CIS" should {
      "return an empty pre-pop response" in new Test {
        val zeroedIfResponse: CISSource = CISSource(
          totalDeductionAmount = Some(0),
          totalCostOfMaterials = Some(0),
          totalGrossAmountPaid = Some(0),
          cisDeductions = Seq(
            CISDeductions(
              fromDate = "fromDate",
              toDate = "toDate",
              contractorName = Some("contractor name"),
              employerRef = "ref",
              totalDeductionAmount = Some(0),
              totalCostOfMaterials = Some(0),
              totalGrossAmountPaid = Some(0),
              periodData = Seq(
                GetPeriodData(
                  deductionFromDate = "fromDate",
                  deductionToDate = "toDate",
                  deductionAmount = Some(0),
                  costOfMaterials = Some(0),
                  grossAmountPaid = Some(0),
                  submissionDate = "submissionDate",
                  submissionId = Some("submissionId"),
                  source = "contractor"
                )
              )
            )
          )
        )

        val httpResponse: HttpResponse = HttpResponse(OK, Json.toJson(zeroedIfResponse).toString())
        stubGetHttpClientCall(ifUrl(CONTRACTOR), httpResponse)
        stubGetHttpClientCall(ifUrl(CUSTOMER), notFoundHttpResponse)

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.noPrePop)
      }
    }

    "IF returns relevant data when retrieving a user's CIS" should {
      "return the appropriate pre-population response when only customer data exists" in new Test {
        val customerIfResponse: CISSource = CISSource(
          totalDeductionAmount = Some(100),
          totalCostOfMaterials = Some(100),
          totalGrossAmountPaid = Some(100),
          cisDeductions = Seq(
            CISDeductions(
              fromDate = "fromDate",
              toDate = "toDate",
              contractorName = Some("contractor name"),
              employerRef = "ref",
              totalDeductionAmount = Some(100),
              totalCostOfMaterials = Some(100),
              totalGrossAmountPaid = Some(100),
              periodData = Seq(
                GetPeriodData(
                  deductionFromDate = "fromDate",
                  deductionToDate = "toDate",
                  deductionAmount = Some(100),
                  costOfMaterials = Some(100),
                  grossAmountPaid = Some(100),
                  submissionDate = "submissionDate",
                  submissionId = Some("submissionId"),
                  source = "customer"
                )
              )
            )
          )
        )

        val httpResponse: HttpResponse = HttpResponse(OK, Json.toJson(customerIfResponse).toString())
        stubGetHttpClientCall(ifUrl(CONTRACTOR), notFoundHttpResponse)
        stubGetHttpClientCall(ifUrl(CUSTOMER), httpResponse)

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse(hasCis = true))
      }

      "return the appropriate pre-population response when only non-zeroed HMRC-held data exists" in new Test {
        val contractorIfResponse: CISSource = CISSource(
          totalDeductionAmount = Some(100),
          totalCostOfMaterials = Some(100),
          totalGrossAmountPaid = Some(100),
          cisDeductions = Seq(
            CISDeductions(
              fromDate = "fromDate",
              toDate = "toDate",
              contractorName = Some("contractor name"),
              employerRef = "ref",
              totalDeductionAmount = Some(100),
              totalCostOfMaterials = Some(100),
              totalGrossAmountPaid = Some(100),
              periodData = Seq(
                GetPeriodData(
                  deductionFromDate = "fromDate",
                  deductionToDate = "toDate",
                  deductionAmount = Some(100),
                  costOfMaterials = Some(100),
                  grossAmountPaid = Some(100),
                  submissionDate = "submissionDate",
                  submissionId = Some("submissionId"),
                  source = "contractor"
                )
              )
            )
          )
        )

        val httpResponse: HttpResponse = HttpResponse(OK, Json.toJson(contractorIfResponse).toString())
        stubGetHttpClientCall(ifUrl(CONTRACTOR), httpResponse)
        stubGetHttpClientCall(ifUrl(CUSTOMER), notFoundHttpResponse)

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse(true))
      }

      "return the appropriate pre-population response for a mixed scenario" in new Test {
        val contractorIfResponse: CISSource = CISSource(
          totalDeductionAmount = Some(100),
          totalCostOfMaterials = Some(100),
          totalGrossAmountPaid = Some(100),
          cisDeductions = Seq(
            CISDeductions(
              fromDate = "fromDate",
              toDate = "toDate",
              contractorName = Some("contractor name"),
              employerRef = "ref",
              totalDeductionAmount = Some(100),
              totalCostOfMaterials = Some(100),
              totalGrossAmountPaid = Some(100),
              periodData = Seq(
                GetPeriodData(
                  deductionFromDate = "fromDate",
                  deductionToDate = "toDate",
                  deductionAmount = Some(100),
                  costOfMaterials = Some(100),
                  grossAmountPaid = Some(100),
                  submissionDate = "submissionDate",
                  submissionId = Some("submissionId"),
                  source = "contractor"
                ),
                GetPeriodData(
                  deductionFromDate = "fromDate2",
                  deductionToDate = "toDate2",
                  deductionAmount = Some(0),
                  costOfMaterials = Some(0),
                  grossAmountPaid = Some(0),
                  submissionDate = "submissionDate2",
                  submissionId = Some("submissionId2"),
                  source = "contractor2"
                )
              )
            )
          )
        )
        val customerIfResponse: CISSource = CISSource(
          totalDeductionAmount = Some(100),
          totalCostOfMaterials = Some(100),
          totalGrossAmountPaid = Some(100),
          cisDeductions = Seq(
            CISDeductions(
              fromDate = "fromDate",
              toDate = "toDate",
              contractorName = Some("contractor name"),
              employerRef = "ref",
              totalDeductionAmount = Some(100),
              totalCostOfMaterials = Some(100),
              totalGrossAmountPaid = Some(100),
              periodData = Seq(
                GetPeriodData(
                  deductionFromDate = "fromDate",
                  deductionToDate = "toDate",
                  deductionAmount = Some(100),
                  costOfMaterials = Some(100),
                  grossAmountPaid = Some(100),
                  submissionDate = "submissionDate",
                  submissionId = Some("submissionId"),
                  source = "customer"
                )
              )
            )
          )
        )

        val contractorHttpResponse: HttpResponse = HttpResponse(OK, Json.toJson(contractorIfResponse).toString())
        val customerHttpResponse: HttpResponse = HttpResponse(OK, Json.toJson(customerIfResponse).toString())

        stubGetHttpClientCall(ifUrl(CONTRACTOR), contractorHttpResponse)
        stubGetHttpClientCall(ifUrl(CUSTOMER), customerHttpResponse)

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse(hasCis = true))
      }
    }
  }
}
