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

import actions.AuthorisedAction
import models.prePopulation.PrePopulationResponse
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PrePopulationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.PrePopulationLogging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PrePopulationController @Inject()(service: PrePopulationService,
                                        auth: AuthorisedAction,
                                        cc: ControllerComponents)
                                       (implicit ec: ExecutionContext) extends BackendController(cc)
  with PrePopulationLogging {
  val classLoggingContext: String = "PrePopulationController"

  def get(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit request => {
    val userDataLogString: String = s" for NINO: $nino, and tax year: $taxYear"
    val infoLogger: String => Unit = infoLog(methodLoggingContext = "get", dataLog = userDataLogString)
    val warnLogger: String => Unit = warnLog(methodLoggingContext = "get", dataLog = userDataLogString)

    infoLogger("Request received to check user's CIS data for pre-pop")

    service.get(taxYear, nino).bimap(
      serviceError => {
        warnLogger(s"An error occurred while checking the user's CIS data for pre-pop ${serviceError.toLogString}")
        InternalServerError
      },
      _ => {
        infoLogger("CIS pre-pop check completed successfully. Returning response")
        Ok(Json.toJson(new PrePopulationResponse(
          true
        )))
      }
    ).merge
  }}
}
