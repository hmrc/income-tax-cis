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

package config

import featureswitch.core.config.SectionCompletedQuestion
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import support.UnitTest
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigSpec extends UnitTest with MockFactory {

  private val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  private val mockConfiguration: Configuration = mock[Configuration]

  private val appConfig = new AppConfigImpl(mockConfiguration, mockServicesConfig)

  ".isEnabled(fs: FeatureSwitch)" when {
    "value has not been saved to Sys.props" should {
      "return the value from AppConfig" in {
        (mockServicesConfig.getBoolean _).expects(SectionCompletedQuestion.configName).returns(false).once()
        sys.props.get(SectionCompletedQuestion.configName) shouldBe None
        appConfig.isEnabled(SectionCompletedQuestion) shouldBe false
      }
    }

    "value has been saved to Sys.pros" should {
      "return `true`" when {
        "feature is enabled" in {
          (mockServicesConfig.getBoolean _).expects(SectionCompletedQuestion.configName).never()
          appConfig.enable(SectionCompletedQuestion)
          sys.props.get(SectionCompletedQuestion.configName) shouldBe Some("true")
          appConfig.isEnabled(SectionCompletedQuestion) shouldBe true
        }
      }

      "return `false`" when {
        "feature is disabled" in {
          (mockServicesConfig.getBoolean _).expects(SectionCompletedQuestion.configName).never()
          appConfig.disable(SectionCompletedQuestion)
          sys.props.get(SectionCompletedQuestion.configName) shouldBe Some("false")
          appConfig.isEnabled(SectionCompletedQuestion) shouldBe false
        }
      }
    }
  }

}
