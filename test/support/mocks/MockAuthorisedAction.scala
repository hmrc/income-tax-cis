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

package support.mocks

import actions.AuthorisedAction
import models.authorisation.Enrolment.{Individual, Nino}
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.mvc._
import play.api.test.Helpers.stubMessagesControllerComponents
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait MockAuthorisedAction extends MockFactory
  with MockAuthConnector { _: TestSuite =>

  private val mcc = stubMessagesControllerComponents()
  private val defaultActionBuilder: DefaultActionBuilder = DefaultActionBuilder(mcc.parsers.default)

  protected val mockAuthorisedAction: AuthorisedAction = new AuthorisedAction(
    defaultActionBuilder = defaultActionBuilder,
    authConnector = mockAuthConnector,
    cc = mcc
  )

  def mockAuthorisation(): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    val individualEnrolments: Enrolments = Enrolments(Set(
      Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, "1234567890")), "Activated"),
      Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, "1234567890")), "Activated")
    ))

    mockAuth(individualEnrolments)
  }
}
