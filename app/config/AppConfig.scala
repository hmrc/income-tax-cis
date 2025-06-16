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
  def hipAuthTokenKey: String
  def hipAuthTokenFor(apiVersion: String): String
  def hipBaseUrl: String
  def hipEnvironment: String
  def baseUrl(serviceName: String): String
  protected def rootServices: String
  protected def defaultProtocol: String
  def getConfString(confKey: String, defString: => String): String
  def getConfInt(confKey: String, defInt: => Int): Int
  def throwConfigNotFoundError(key: String): RuntimeException
  def cisFrontendBaseUrl: String
  def desBaseUrl: String
  def desEnvironment: String
  def desAuthorisationToken: String
  def encryptionKey: String
  def mongoJourneyAnswersTTL: Int
  def sectionCompletedQuestionEnabled: Boolean
  def replaceJourneyAnswersIndexes: Boolean
  def hipMigration1789Enabled: Boolean
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

  override lazy val hipBaseUrl: String = baseUrl(serviceName = "hip-integration-framework")

  override def hipEnvironment: String = config.get[String]("microservice.services.hip-integration-framework.environment")

  override lazy val hipAuthTokenKey: String = "microservice.services.hip-integration-framework.authorisation-token"

  override def hipAuthTokenFor(apiVersion: String): String =
    config.get[String](hipAuthTokenKey + s".$apiVersion")

  override def baseUrl(serviceName: String): String = {
    val protocol = getConfString(s"$serviceName.protocol", defaultProtocol)
    val host = getConfString(s"$serviceName.host", throwConfigNotFoundError(s"$serviceName.host"))
    val port = getConfInt(s"$serviceName.port", throwConfigNotFoundError(s"$serviceName.port"))
    s"$protocol://$host:$port"
  }

  override protected lazy val rootServices = "microservice.services"

  override protected lazy val defaultProtocol: String =
    config
      .getOptional[String](s"$rootServices.protocol")
      .getOrElse("http")

  override def getConfString(confKey: String, defString: => String): String =
    config
      .getOptional[String](s"$rootServices.$confKey")
      .getOrElse(defString)

  override def getConfInt(confKey: String, defInt: => Int): Int =
    config
      .getOptional[Int](s"$rootServices.$confKey")
      .getOrElse(defInt)

  override def throwConfigNotFoundError(key: String) =
    throw new RuntimeException(s"Could not find config key '$key'")

  def cisFrontendBaseUrl: String = config.get[String]("microservice.services.income-tax-cis-frontend.url")

  def desBaseUrl: String = servicesConfig.baseUrl("des")
  def desEnvironment: String = config.get[String]("microservice.services.des.environment")
  def desAuthorisationToken: String = config.get[String]("microservice.services.des.authorisation-token")

  def sectionCompletedQuestionEnabled: Boolean = config.get[Boolean]("feature-switch.sectionCompletedQuestionEnabled")

  override lazy val hipMigration1789Enabled: Boolean = config.get[Boolean]("feature-switch.hip-migration.api-1789-enabled")
}
