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

package uk.gov.hmrc.apisubscriptionfields

import java.util.UUID

import uk.gov.hmrc.apisubscriptionfields.model._
import Types._
import cats.data.NonEmptyList
import uk.gov.hmrc.apisubscriptionfields.repository.FieldsDefinition
import eu.timepit.refined.api.Refined

trait FieldsDefinitionTestData extends TestData {
    import eu.timepit.refined.auto._

  def fieldN(id: Int): FieldName = {
    val char = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(id)
    Refined.unsafeApply(s"field$char")
  }

  final val AlphanumericFieldName: FieldName = "alphanumericField"
  final val PasswordFieldName: FieldName = "password"

  final val FakeValidationRule: RegexValidationRule = RegexValidationRule(".*")

  final val FakeValidation: ValidationGroup = ValidationGroup("error message", NonEmptyList.one(FakeValidationRule))

  final val FakeUrlValidation: ValidationGroup = ValidationGroup("error message", NonEmptyList.one(UrlValidationRule))

  final val FakeFieldDefinitionUrl = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", Some(FakeUrlValidation))
  final val FakeFieldDefinitionUrlValidationEmpty = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", None)
  final val FakeFieldDefinitionString = FieldDefinition(fieldN(2), "desc2", "hint2", FieldDefinitionType.STRING, "short description", Some(FakeValidation))
  final val FakeFieldDefinitionSecureToken = FieldDefinition(fieldN(3), "desc3", "hint3", FieldDefinitionType.SECURE_TOKEN, "short description", Some(FakeValidation))
  final val FakeFieldsDefinitions = NonEmptyList.fromListUnsafe(List(FakeFieldDefinitionUrl, FakeFieldDefinitionString, FakeFieldDefinitionSecureToken))
  final val FakeFieldsDefinition = FieldsDefinition(fakeRawContext, fakeRawVersion, FakeFieldsDefinitions)
  final val FakeFieldsDefinitionResponse = FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, FakeFieldsDefinition.fieldDefinitions)

  final val AlphaNumericRegexRule: RegexValidationRule = RegexValidationRule("^[a-zA-Z0-9]+$")
  final val PasswordRegexRule: RegexValidationRule = RegexValidationRule("^(?=.{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$")
  final val FakeValidationForAlphanumeric: ValidationGroup = ValidationGroup("Needs to be alpha numeric", NonEmptyList.one(AlphaNumericRegexRule))
  final val FakeValidationForPassword: ValidationGroup =
    ValidationGroup("Needs to be at least 8 chars with at least one lowercase, uppercase and special char", NonEmptyList.one(PasswordRegexRule))
  final val FakeFieldDefinitionAlphnumericField = FieldDefinition(
    "alphanumericField",
    "an alphanumeric filed",
    "this is an alphanumeric value",
    FieldDefinitionType.STRING,
    "an alphanumeric field",
    Some(FakeValidationForAlphanumeric)
  )
  final val FakeFieldDefinitionPassword =
    FieldDefinition("password", "password", "this is your password", FieldDefinitionType.SECURE_TOKEN, "password", Some(FakeValidationForPassword))
  final val FakeFieldsDefinitionsWithRegex = NonEmptyList.fromListUnsafe(List(FakeFieldDefinitionAlphnumericField, FakeFieldDefinitionPassword))
  final val FakeFieldsDefinitionWithRegex = FieldsDefinition(fakeRawContext, fakeRawVersion, FakeFieldsDefinitionsWithRegex)
  final val FakeFieldsDefinitionResponseWithRegex: FieldsDefinitionResponse = FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, FakeFieldsDefinitionsWithRegex)

  def createFieldsDefinition(apiContext: String = fakeRawContext, apiVersion: String = fakeRawVersion, fieldDefinitions: NonEmptyList[FieldDefinition] = FakeFieldsDefinitions) =
    FieldsDefinition(apiContext, apiVersion, fieldDefinitions)

  def uniqueApiContext = UUID.randomUUID().toString
}
