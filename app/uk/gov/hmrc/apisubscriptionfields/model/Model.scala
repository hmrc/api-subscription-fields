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

import java.util.UUID

import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinitionType.FieldDefinitionType

case class ClientId(value: String) extends AnyVal

case class ApiContext(value: String) extends AnyVal

case class ApiVersion(value: String) extends AnyVal

case class SubscriptionFieldsId(value: UUID) extends AnyVal

sealed trait ValidationRule

case class RegexValidationRule(regex: String) extends ValidationRule

case class Validation(errorMessage: String, rules: Seq[ValidationRule])

object Validation {
}

object FieldDefinitionType extends Enumeration {
  type FieldDefinitionType = Value

  val URL = Value("URL")
  val SECURE_TOKEN = Value("SecureToken")
  val STRING = Value("STRING")
}

case class FieldDefinition(name: String, description: String, hint: String = "", `type`: FieldDefinitionType,
                           shortDescription: String, validation: Option[Validation] = None)