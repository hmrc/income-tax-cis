/*
 * Copyright 2024 HM Revenue & Customs
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

import common.CISSource.CONTRACTOR
import config.AppConfig
import connectors.errors.{ApiError, SingleErrorBody}
import models.get.{AllCISDeductions, CISSource}
import models.mongo.JourneyAnswers
import models.tasklist.SectionTitle.SelfEmploymentTitle
import models.tasklist.TaskStatus.{CheckNow, Completed, InProgress, NotStarted}
import models.tasklist.TaskTitle.CIS
import models.tasklist._
import play.api.Configuration
import play.api.libs.json.{JsObject, JsString, Json}
import support.ControllerUnitTest
import support.builders.CISDeductionsBuilder.aCISDeductions
import support.builders.CISSourceBuilder
import support.builders.GetPeriodDataBuilder.aGetPeriodData
import support.mocks.{MockAuthorisedAction, MockCISDeductionsService, MockJourneyAnswersRepository}
import support.providers.{AppConfigStubProvider, FakeRequestProvider}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class CommonTaskListServiceSpec extends ControllerUnitTest
  with MockCISDeductionsService
  with MockAuthorisedAction
  with FakeRequestProvider
  with AppConfigStubProvider
  with MockJourneyAnswersRepository {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

    val nino: String = "12345678"
    val taxYear: Int = 1234
    val mtditid: String = "dummyMtditid"

    val appConfig: AppConfig = appConfigStub

    def service: CommonTaskListService = new CommonTaskListService(
      appConfig = appConfigStub,
      cisDeductionsService = mockCISDeductionsService,
      journeyAnswersRepository = mockJourneyAnswersRepo
    )

    val baseUrl = "http://localhost:9338/update-and-submit-income-tax-return"
    val cisCustomerUrl = s"$baseUrl/construction-industry-scheme-deductions/$taxYear/summary"

    def cisTask(status: TaskStatus): TaskListSection = TaskListSection(
      sectionTitle = SelfEmploymentTitle,
      taskItems = Some(Seq(TaskListSectionItem(CIS, status, Some(cisCustomerUrl))))
    )

    def journeyAnswers(status: String): JourneyAnswers = JourneyAnswers(
      mtdItId = mtditid,
      taxYear = taxYear,
      journey = "cis",
      data = Json.obj("status" -> JsString(status)),
      lastUpdated = Instant.MIN
    )

    val customerCISResult: CISSource = CISSourceBuilder.aCISSource.copy(
      cisDeductions = Seq(
        aCISDeductions.copy(
          periodData = Seq(aGetPeriodData.copy(submissionDate = Instant.MIN.toString))
        )
      )
    )

    val contractorCISResult: CISSource = CISSourceBuilder.aCISSource.copy(
      cisDeductions = Seq(
        aCISDeductions.copy(
          periodData = Seq(aGetPeriodData.copy(source = CONTRACTOR, submissionDate = Instant.now().toString))
        )
      )
    )

    val emptyTaskListResult: TaskListSection = TaskListSection(sectionTitle = SelfEmploymentTitle, taskItems = None)
  }

  "CommonTaskListService.get" when {
    "errors occur" should {
      "return an empty task list when call to retrieve CIS deductions returns an API error" in new Test {
        mockGetCISDeductions(nino, taxYear, Left(ApiError(404, SingleErrorBody("DummyCode", "DummyReason"))))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe emptyTaskListResult
      }

      "throw an exception when call to retrieve CIS deductions fails" in new Test {
        mockGetCISDeductionsException(nino, taxYear, new RuntimeException("Dummy error"))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        assertThrows[RuntimeException](result)
      }

      "throw an exception when call to retrieve Journey Answers fails" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(None, None)))
        mockGetJourneyAnswersException(mtditid, taxYear, "cis", new RuntimeException("Dummy"))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        assertThrows[RuntimeException](result)
      }
    }

    "CIS deductions response contains only HMRC data" should {
      "return expected task list with 'CheckNow' status" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(None, Some(contractorCISResult))))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe cisTask(CheckNow)
      }
    }

    "HMRC data in CIS deductions response is newer than customer data" should {
      "return expected task list with 'CheckNow' status" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(Some(customerCISResult), Some(contractorCISResult))))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe cisTask(CheckNow)
      }
    }

    "HMRC data is omitted, or is not latest and Journey Answers are defined" should {
      "return expected task list with status from Journey Answers data if it can be parsed" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(None, None)))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", Some(journeyAnswers("completed")))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe cisTask(Completed)
      }

      "throw an exception if an error occurs while parsing Journey Answers status" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(None, None)))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", Some(journeyAnswers("").copy(data = JsObject.empty)))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        assertThrows[RuntimeException](result)
      }

      "return expected task list with 'NotStarted' status if Journey Answers status value is unexpected" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(None, None)))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", Some(journeyAnswers("beep boop")))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe cisTask(NotStarted)
      }
    }

    "HMRC data is omitted, or not latest, Journey Answers are not defined, and customer data exists" should {
      "return expected task list with 'InProgress' status if section completed feature switch is enabled" in new Test {
        override val service: CommonTaskListService = new CommonTaskListService(
          appConfig = new AppConfig(mock[Configuration], mock[ServicesConfig]) {
            override lazy val cisFrontendBaseUrl: String = "http://localhost:9338"
            override lazy val sectionCompletedQuestionEnabled: Boolean = true
          },
          cisDeductionsService = mockCISDeductionsService,
          journeyAnswersRepository = mockJourneyAnswersRepo
        )

        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(Some(customerCISResult), None)))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe cisTask(InProgress)
      }

      "return expected task list with 'Completed' status if section completed feature switch is disabled" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(Some(customerCISResult), None)))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe cisTask(Completed)
      }
    }

    "no data exists" should {
      "return with an empty task list" in new Test {
        mockGetCISDeductions(nino, taxYear, Right(AllCISDeductions(None, None)))
        mockGetJourneyAnswers(mtditid, taxYear, "cis", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe emptyTaskListResult
      }
    }
  }
}
