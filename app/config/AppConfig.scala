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

package config

import com.google.inject.ImplementedBy
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {
  def ifBaseUrl: String
  def ifEnvironment: String
  def authorisationTokenFor(apiVersion: String): String
  def cisFrontendBaseUrl: String
  def desBaseUrl: String
  def desEnvironment: String
  def desAuthorisationToken: String
  def encryptionKey: String
  def mongoJourneyAnswersTTL: Int
  def emaSupportingAgentsEnabled: Boolean
  def sectionCompletedQuestionEnabled: Boolean
  def replaceJourneyAnswersIndexes: Boolean
}

@Singleton
class AppConfigImpl @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  private def ifAuthorisationTokenKey: String = "microservice.services.integration-framework.authorisation-token"

  def ifBaseUrl: String = servicesConfig.baseUrl(serviceName = "integration-framework")
  def ifEnvironment: String = servicesConfig.getString(key = "microservice.services.integration-framework.environment")

  //Journey answers Mongo config
  lazy val encryptionKey: String = servicesConfig.getString("mongodb.encryption.key")
  lazy val mongoJourneyAnswersTTL: Int = Duration(servicesConfig.getString("mongodb.journeyAnswersTimeToLive")).toDays.toInt
  lazy val replaceJourneyAnswersIndexes: Boolean = servicesConfig.getBoolean("mongodb.replaceJourneyAnswersIndexes")

  def authorisationTokenFor(apiVersion: String): String = servicesConfig.getString(ifAuthorisationTokenKey + s".$apiVersion")

  def cisFrontendBaseUrl: String = config.get[String]("microservice.services.income-tax-cis-frontend.url")

  def desBaseUrl: String = servicesConfig.baseUrl("des")
  def desEnvironment: String = config.get[String]("microservice.services.des.environment")
  def desAuthorisationToken: String = config.get[String]("microservice.services.des.authorisation-token")
  
  def emaSupportingAgentsEnabled: Boolean = config.get[Boolean]("feature-switch.ema-supporting-agents-enabled")
  def sectionCompletedQuestionEnabled: Boolean = config.get[Boolean]("feature-switch.sectionCompletedQuestionEnabled")
}
