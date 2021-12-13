/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import com.codahale.metrics.SharedMetricRegistries
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.libs.json.{JsObject, Json}
import utils.TestUtils

class DesErrorBodyModelSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val model: DesErrorBodyModel = DesErrorBodyModel("SERVER_ERROR", "Service is unavailable")
  val jsonModel: JsObject = Json.obj(
    "code" -> "SERVER_ERROR",
    "reason" -> "Service is unavailable"
  )

  val errorsJsModel: JsObject = Json.obj(
    "failures" -> Json.arr(
      Json.obj("code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "The service is currently unavailable"),
      Json.obj("code" -> "INTERNAL_SERVER_ERROR",
        "reason" -> "The service is currently facing issues.")
    )
  )

  "The DesErrorModel" should {

    val model = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVER_ERROR","Service is unavailable"))
    val errorsModel = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorsBodyModel(Seq(
      DesErrorBodyModel("SERVICE_UNAVAILABLE","The service is currently unavailable"),
      DesErrorBodyModel("INTERNAL_SERVER_ERROR","The service is currently facing issues.")
    )))

    "parse to Json" in {
      model.toJson mustBe jsonModel
    }
    "parse to Json for multiple errors" in {
      errorsModel.toJson mustBe errorsJsModel
    }
  }

}
