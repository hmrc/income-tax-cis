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

import featureswitch.core.config.SectionCompletedQuestion
import featureswitch.core.models.FeatureSwitch
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigStub extends MockFactory {

  lazy val mockConfiguration = mock[Configuration]
  lazy val mockServicesConfig = mock[ServicesConfig]

  def config(desHost: String = "localhost",
             environment: String = "test",
             sectionCompletedQuestionEnabled: Boolean = false
            ): AppConfig = new AppConfigImpl(mockConfiguration, mockServicesConfig) {
    private val wireMockPort = 11111

    private lazy val authorisationToken: String = "secret"

    override lazy val ifBaseUrl: String = s"http://localhost:$wireMockPort"
    override lazy val ifEnvironment: String = environment

    override lazy val cisFrontendBaseUrl: String = "http://localhost:9338"

    override def authorisationTokenFor(apiVersion: String): String = authorisationToken + s".$apiVersion"

    override lazy val desBaseUrl: String = s"http://$desHost:$wireMockPort"
    override lazy val desAuthorisationToken: String = "authorisation-token"
    override lazy val desEnvironment: String = "environment"

    private def mockFeatureSwitchResponse(featureSwitch: FeatureSwitch, isEnabled: Boolean): Unit = {
      sys.props.remove(featureSwitch.configName)
      (mockServicesConfig.getBoolean(_: String)).expects(featureSwitch.configName).returning(isEnabled).anyNumberOfTimes()
    }
    mockFeatureSwitchResponse(SectionCompletedQuestion, isEnabled = sectionCompletedQuestionEnabled)
  }
}