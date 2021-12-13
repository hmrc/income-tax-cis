/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

import uk.gov.hmrc.http.HttpResponse

class PagerDutyHelperTest extends TestUtils {

  "PagerDutyHelper" should {

    val status = 200

    "return string containing correlationId when response contains correlationId" in {
      val result = PagerDutyHelper.getCorrelationId(HttpResponse(status, "", Map("CorrelationId" -> Seq("some_correlation_id"))))
      result mustBe " CorrelationId: some_correlation_id"
    }

    "return empty string when response does not contain correlationId" in {
      val result = PagerDutyHelper.getCorrelationId(HttpResponse(status, ""))
      result mustBe ""
    }

  }
}
