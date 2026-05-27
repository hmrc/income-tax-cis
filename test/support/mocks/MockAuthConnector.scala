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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, when}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthConnector {

  val mockAuthConnector: AuthConnector =
    mock(classOf[AuthConnector])

  protected def mockAuthoriseSuccess[A](result: A): Unit =
    when(
      mockAuthConnector.authorise[A](
        any[Predicate](),
        any[Retrieval[A]]()
      )(
        any[HeaderCarrier](),
        any[ExecutionContext]()
      )
    ).thenReturn(Future.successful(result))

  protected def mockAuthoriseFailure[A](exception: Throwable): Unit =
    when(
      mockAuthConnector.authorise[A](
        any[Predicate](),
        any[Retrieval[A]]()
      )(
        any[HeaderCarrier](),
        any[ExecutionContext]()
      )
    ).thenReturn(Future.failed[A](exception))

  /** Stubs the individual user path through AuthorisedAction.async. */
  def mockAuth(enrolments: Enrolments): Unit = {
    when(
      mockAuthConnector.authorise[Option[AffinityGroup]](
        any[Predicate](),
        eqTo(affinityGroup)
      )(any[HeaderCarrier](), any[ExecutionContext]())
    ).thenReturn(Future.successful(None))

    when(
      mockAuthConnector.authorise[Enrolments ~ ConfidenceLevel](
        any[Predicate](),
        eqTo(allEnrolments and confidenceLevel)
      )(any[HeaderCarrier](), any[ExecutionContext]())
    ).thenReturn(Future.successful(enrolments and ConfidenceLevel.L250))
  }

  /** Stubs the agent path through AuthorisedAction.async. */
  def mockAuthAsAgent(enrolments: Enrolments): Unit = {
    when(
      mockAuthConnector.authorise[Option[AffinityGroup]](
        any[Predicate](),
        eqTo(affinityGroup)
      )(any[HeaderCarrier](), any[ExecutionContext]())
    ).thenReturn(Future.successful(Some(AffinityGroup.Agent)))

    when(
      mockAuthConnector.authorise[Enrolments](
        any[Predicate](),
        eqTo(allEnrolments)
      )(any[HeaderCarrier](), any[ExecutionContext]())
    ).thenReturn(Future.successful(enrolments))
  }

  def mockAuthReturnException(exception: Throwable): Unit =
    mockAuthoriseFailure[Any](exception)
}
