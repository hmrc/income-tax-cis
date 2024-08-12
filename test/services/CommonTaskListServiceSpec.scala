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

import connectors.errors.{ApiError, SingleErrorBody}
import models.get.{AllCISDeductions, CISSource}
import models.tasklist._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.NOT_FOUND
import support.ControllerUnitTest
import support.builders.{AllCISDeductionsBuilder, CISSourceBuilder}
import support.mocks.{MockAuthorisedAction, MockCISDeductionsService}
import support.providers.{AppConfigStubProvider, FakeRequestProvider}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CommonTaskListServiceSpec extends ControllerUnitTest
  with MockCISDeductionsService
  with MockAuthorisedAction
  with FakeRequestProvider
  with AppConfigStubProvider {

  private implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  val service: CommonTaskListService = new CommonTaskListService(appConfigStub, mockCISDeductionsService)

  val nino: String = "12345678"
  val taxYear: Int = 1234

  val customerCISResult: CISSource = CISSourceBuilder.aCISSource

  val fullCISResult: Right[ApiError, AllCISDeductions] = Right(AllCISDeductionsBuilder.anAllCISDeductions)

  val emptyCISResult: Left[ApiError, AllCISDeductions] = Left(ApiError(NOT_FOUND, SingleErrorBody("code", "Some_Reason")))

  val fullTaskSection: TaskListSection =
    TaskListSection(SectionTitle.CISTitle,
      Some(List(
        TaskListSectionItem(TaskTitle.cisDeductions, TaskStatus.Completed,
          Some("http://localhost:9338/update-and-submit-income-tax-return/construction-industry-scheme-deductions/1234/check-construction-industry-scheme-deductions")),
      ))
    )

  "CommonTaskListService.get" should {

    "return a full task list section model" in {

      (mockCISDeductionsService.getCISDeductions(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(fullCISResult))

      val underTest = service.get(taxYear, nino)

      await(underTest) mustBe fullTaskSection
    }

    "return a minimal task list section model" in {

      (mockCISDeductionsService.getCISDeductions(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(AllCISDeductions(Some(customerCISResult), Some(CISSource(None, None, None, Seq()))))))

      val underTest = service.get(taxYear, nino)

      await(underTest) mustBe fullTaskSection.copy(
        taskItems = Some(List(
          TaskListSectionItem(
            TaskTitle.cisDeductions, TaskStatus.Completed, Some("http://localhost:9338/update-and-submit-income-tax-return/construction-industry-scheme-deductions/1234/check-construction-industry-scheme-deductions"))
        ))
      )
    }

    "return an empty task list section model" in {

      (mockCISDeductionsService.getCISDeductions(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(emptyCISResult))

      val underTest = service.get(taxYear, nino)

      await(underTest) mustBe TaskListSection(SectionTitle.CISTitle, None)
    }
  }
}
