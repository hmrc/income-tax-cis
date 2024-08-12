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

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  private lazy val ifAuthorisationTokenKey: String = "microservice.services.integration-framework.authorisation-token"

  lazy val ifBaseUrl: String = servicesConfig.baseUrl(serviceName = "integration-framework")
  lazy val ifEnvironment: String = servicesConfig.getString(key = "microservice.services.integration-framework.environment")

  def authorisationTokenFor(apiVersion: String): String = servicesConfig.getString(ifAuthorisationTokenKey + s".$apiVersion")

  lazy val cisFrontendBaseUrl: String = config.get[String]("microservice.services.income-tax-cis-frontend.url")

  lazy val authBaseUrl: String = servicesConfig.baseUrl("auth")
  lazy val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  lazy val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  lazy val desBaseUrl: String = servicesConfig.baseUrl("des")
  lazy val desEnvironment: String = config.get[String]("microservice.services.des.environment")
  lazy val desAuthorisationToken: String = config.get[String]("microservice.services.des.authorisation-token")
}
