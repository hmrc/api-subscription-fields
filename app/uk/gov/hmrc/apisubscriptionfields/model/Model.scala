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

package uk.gov.hmrc.apisubscriptionfields.model

import cats.data.{NonEmptyList => NEL}
import org.apache.commons.validator.routines.{DomainValidator, UrlValidator}

import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinitionType.FieldDefinitionType
import uk.gov.hmrc.apisubscriptionfields.model.Types._

sealed trait ValidationRule {

  def validate(value: FieldValue): Boolean = {
    if (value == "") true
    else validateAgainstRule(value)
  }
  protected def validateAgainstRule(value: FieldValue): Boolean
}

case class RegexValidationRule(regex: RegexExpr) extends ValidationRule {
  def validateAgainstRule(value: FieldValue): Boolean = value.matches(regex.value)
}

// Taken from: https://stackoverflow.com/a/5078838
case object UrlValidationRule extends ValidationRule {
  DomainValidator.updateTLDOverride(DomainValidator.ArrayType.GENERIC_PLUS, Array("mdtp"));
  private val schemes           = Array("http", "https")
  private lazy val urlValidator = new org.apache.commons.validator.routines.UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS)

  def validateAgainstRule(value: FieldValue): Boolean = {
    urlValidator.isValid(value)
  }
}

case class ValidationGroup(errorMessage: String, rules: NEL[ValidationRule])

object FieldDefinitionType extends Enumeration {
  type FieldDefinitionType = Value

  @deprecated("We don't use URL type for any validation", since = "0.5x")
  val URL          = Value("URL")
  val SECURE_TOKEN = Value("SecureToken")
  val STRING       = Value("STRING")
  val PPNS_FIELD   = Value("PPNSField")
}

case class FieldDefinition(
    name: FieldName,
    description: String,
    hint: String = "",
    `type`: FieldDefinitionType,
    shortDescription: String,
    validation: Option[ValidationGroup] = None,
    access: AccessRequirements = AccessRequirements.Default
  )

case class ApiFieldDefinitions(apiContext: ApiContext, apiVersion: ApiVersion, fieldDefinitions: NEL[FieldDefinition])

case class SubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, fieldsId: SubscriptionFieldsId, fields: Fields)
