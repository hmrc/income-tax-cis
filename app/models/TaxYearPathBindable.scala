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

package models

import play.api.mvc.PathBindable

object TaxYearPathBindable {

  /* Gets a representation of a taxYear in a YY-YY format (from a YYYY format).
   */
  def asTys(taxYear: TaxYear): String = {
    val end = taxYear.taxYear - 2000
    val start = end - 1
    s"$start-$end"
  }

  implicit def pathBindable: PathBindable[TaxYear] = new PathBindable[TaxYear] {

    override def bind(key: String, value: String): Either[String, TaxYear] =
      value match {
        case result if result.matches("^20\\d{2}$") => Right(TaxYear(taxYear = result.toInt))
        case _ => Left("Invalid taxYear")
      }

    override def unbind(key: String, value: TaxYear): String =
      value.taxYear.toString
  }
  case class TaxYear(taxYear: Int)
}