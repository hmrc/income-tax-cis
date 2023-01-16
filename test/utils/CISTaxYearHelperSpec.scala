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

package utils

import models.CISDates
import support.UnitTest
import utils.CISTaxYearHelper.cisTaxYearConverter

class CISTaxYearHelperSpec extends UnitTest {

  ".cisTaxYearConverter" should {
    "return a cis dates model containing the correct dates when a tax year is passed" in {
      cisTaxYearConverter(2020) shouldBe CISDates(fromDate = "2019-04-06", toDate = "2020-04-05")
    }
  }
}
