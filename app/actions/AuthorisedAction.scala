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

package actions

import models.User
import models.authorisation.DelegatedAuthRules
import models.authorisation.Enrolment.{Agent, Individual, Nino}
import models.requests.AuthorisationRequest
import play.api.Logger
import play.api.mvc.Results.{InternalServerError, Unauthorized}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{
  affinityGroup,
  allEnrolments,
  confidenceLevel
}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(defaultActionBuilder: DefaultActionBuilder,
                                 val authConnector: AuthConnector,
                                 cc: ControllerComponents) extends AuthorisedFunctions {

  private lazy val logger: Logger = Logger.apply(this.getClass)
  private implicit val executionContext: ExecutionContext = cc.executionContext

  private val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level
  private val unauthorized: Future[Result] = Future.successful(Unauthorized)

  def async(block: AuthorisationRequest[AnyContent] => Future[Result]): Action[AnyContent] = defaultActionBuilder.async { implicit request =>
    request.headers.get("mtditid").fold {
      val logMessage = "[AuthorisedAction][async] - No MTDITID in the header. Returning unauthorised."
      logger.warn(logMessage)
      unauthorized
    } {
      mtdItId =>
        implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
        authorised().retrieve(affinityGroup) {
          case Some(AffinityGroup.Agent) => agentAuthentication(block, mtdItId)(request, headerCarrier)
          case _ => individualAuthentication(block, mtdItId)(request, headerCarrier)
        } recover {
          case _: NoActiveSession =>
            val logMessage = s"[AuthorisedAction][async] - No active session."
            logger.info(logMessage)
            Unauthorized
          case _: AuthorisationException =>
            val logMessage = s"[AuthorisedAction][async] - User failed to authenticate"
            logger.warn(logMessage)
            Unauthorized
        }
    }
  }

  private[actions] def individualAuthentication[A](block: AuthorisationRequest[A] => Future[Result], requestMtdItId: String)
                                                  (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        val optionalMtdItId: Option[String] = enrolmentGetIdentifierValue(Individual.key, Individual.value, enrolments)
        val optionalNino: Option[String] = enrolmentGetIdentifierValue(Nino.key, Nino.value, enrolments)

        (optionalMtdItId, optionalNino) match {
          case (Some(authMTDITID), Some(_)) =>
            enrolments.enrolments.collectFirst {
              case Enrolment(Individual.key, enrolmentIdentifiers, _, _)
                if enrolmentIdentifiers.exists(identifier => identifier.key == Individual.value && identifier.value == requestMtdItId) =>
                block(AuthorisationRequest(User(requestMtdItId, None), request))
            } getOrElse {
              val logMessage = s"[AuthorisedAction][individualAuthentication] Non-agent with an invalid MTDITID. " +
                s"MTDITID in auth matches MTDITID in request: ${authMTDITID == requestMtdItId}"
              logger.warn(logMessage)
              unauthorized
            }
          case (_, None) =>
            val logMessage = s"[AuthorisedAction][individualAuthentication] - User has no nino."
            logger.warn(logMessage)
            unauthorized
          case (None, _) =>
            val logMessage = s"[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment."
            logger.warn(logMessage)
            unauthorized
        }
      case _ =>
        val logMessage = "[AuthorisedAction][individualAuthentication] User has confidence level below 250."
        logger.warn(logMessage)
        unauthorized
    }
  }

  private val agentAuthLogString: String = "[AuthorisedAction][agentAuthentication]"

  private def agentAuthPredicate(mtdId: String): Predicate =
    Enrolment(Individual.key)
      .withIdentifier(Individual.value, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.agentDelegatedAuthRule)

  private def agentRecovery(): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      val logMessage = s"$agentAuthLogString - No active session."
      logger.info(logMessage)
      unauthorized
    case _: AuthorisationException =>
      logger.warn(s"$agentAuthLogString - Agent does not have delegated authority for Client.")
      unauthorized
    case e =>
      logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      Future(InternalServerError)
  }

  private def handleForValidAgent[A](block: AuthorisationRequest[A] => Future[Result],
                                     mtdItId: String,
                                     enrolments: Enrolments)
                                    (implicit request: Request[A]): Future[Result] = {
    enrolmentGetIdentifierValue(Agent.key, Agent.value, enrolments) match {
      case Some(arn) => block(AuthorisationRequest(User(mtdItId, Some(arn)), request))
      case None =>
        val logMessage = s"$agentAuthLogString - Agent with no HMRC-AS-AGENT enrolment."
        logger.warn(logMessage)
        unauthorized
    }
  }

  private[actions] def agentAuthentication[A](block: AuthorisationRequest[A] => Future[Result], mtdItId: String)
                                             (implicit request: Request[A], hc: HeaderCarrier): Future[Result] =
    authorised(agentAuthPredicate(mtdItId))
      .retrieve(allEnrolments)(enrolments => handleForValidAgent(block, mtdItId, enrolments))
      .recoverWith(agentRecovery())

  private[actions] def enrolmentGetIdentifierValue(checkedKey: String,
                                                   checkedIdentifier: String,
                                                   enrolments: Enrolments): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
      case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
    }
  }.flatten
}
