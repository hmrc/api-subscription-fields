/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields.model

trait ValidationRuleTestData {
  import eu.timepit.refined.auto._

  val lowerCaseValue = "bob"
  val mixedCaseValue = "Bob"

  val lowerCaseRule: ValidationRule = RegexValidationRule("""^[a-z]+$""")
  val mixedCaseRule: ValidationRule = RegexValidationRule("""^[a-zA-Z]+$""")

  val atLeastThreeLongRule: ValidationRule = RegexValidationRule("""^.{3}.*$""")
  val atLeastTenLongRule: ValidationRule = RegexValidationRule("""^.{10}.*$""")

  val validUrl = "https://www.example.com/here/and/there"
  val invalidUrls = List("www.example.com", "ftp://example.com/abc")

}
