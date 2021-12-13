/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

class DESTaxYearHelperSpec extends TestUtils {

  "DESTaxYearHelper" should {

    "return a string containing the last year and the last two digits of this year" in {
      val taxYear = 2020
      val result = DESTaxYearHelper.desTaxYearConverter(taxYear)
      result mustBe "2019-20"
    }

    "return a string containing the last year and the last two digits of this year for a tax year ending in 00" in {
      val taxYear = 2100
      val result = DESTaxYearHelper.desTaxYearConverter(taxYear)
      result mustBe "2099-00"
    }
  }
}
