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

import cats.data.EitherT
import config.AppConfig
import connectors.errors.ApiError
import featureswitch.core.config.SectionCompletedQuestion
import models.get.{AllCISDeductions, CISSource}
import models.mongo.JourneyAnswers
import models.tasklist.SectionTitle.SelfEmploymentTitle
import models.tasklist.TaskStatus.{CheckNow, Completed, InProgress, NotStarted}
import models.tasklist.TaskTitle.CIS
import models.tasklist._
import play.api.Logging
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListService @Inject()(appConfig: AppConfig,
                                      cisDeductionsService: CISDeductionsService,
                                      journeyAnswersRepository: JourneyAnswersRepository) extends Logging {

  private def getTasks(cisDeductions: AllCISDeductions,
                       taxYear: Int,
                       journeyAnswers: Option[JourneyAnswers]): Option[Seq[TaskListSectionItem]] = {
    val baseUrl: String = s"${appConfig.cisFrontendBaseUrl}/update-and-submit-income-tax-return"
    val cisCustomerUrl: String = s"$baseUrl/construction-industry-scheme-deductions/$taxYear/summary"

    def getSubmissionDate(cisSource: Option[CISSource]): Long = {
      cisSource.flatMap(_
        .cisDeductions.headOption.flatMap(_.periodData
          .map(_.submissionDate).maxOption
          .map(Instant.parse(_).getEpochSecond)
        )).getOrElse(0)
    }

    def cisTask(status: TaskStatus): Option[Seq[TaskListSectionItem]] = Some(Seq(
      TaskListSectionItem(CIS, status, Some(cisCustomerUrl))
    ))

    val hmrcDataLatest: Boolean = getSubmissionDate(cisDeductions.contractorCISDeductions) >=
      getSubmissionDate(cisDeductions.customerCISDeductions)

    val hasCustomerData: Boolean = cisDeductions.customerCISDeductions.exists(_.cisDeductions.nonEmpty)
    val hasHMRCData: Boolean = cisDeductions.contractorCISDeductions.exists(_.cisDeductions.nonEmpty)

    (hasHMRCData && hmrcDataLatest, hasCustomerData, journeyAnswers) match {
      case (true, _, _) => cisTask(CheckNow)
      case (_, _, Some(ja)) =>
        val status: TaskStatus = ja.data.value("status").validate[TaskStatus].asOpt match {
          case Some(TaskStatus.Completed) => Completed
          case Some(TaskStatus.InProgress) => InProgress
          case _ =>
            logger.info("[CommonTaskListService][getStatus] status stored in an invalid format, setting as 'Not yet started'.")
            NotStarted
        }

        cisTask(status)
      case (_, true, _) => cisTask(if (appConfig.isEnabled(SectionCompletedQuestion)) InProgress else Completed)
      case (_, _, _) => None
    }
  }

  def get(taxYear: Int, nino: String, mtditid: String)
         (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TaskListSection] = {
    val result: EitherT[Future, ApiError, Option[Seq[TaskListSectionItem]]] = for {
      cisResult <- EitherT(cisDeductionsService.getCISDeductions(nino, taxYear))
      ja <- EitherT.right(journeyAnswersRepository.get(mtditid, taxYear, "cis"))
    } yield getTasks(cisResult, taxYear, ja)

    result
      .leftMap(_ => Option.empty[Seq[TaskListSectionItem]])
      .merge
      .map(TaskListSection(SelfEmploymentTitle, _))
  }
}
