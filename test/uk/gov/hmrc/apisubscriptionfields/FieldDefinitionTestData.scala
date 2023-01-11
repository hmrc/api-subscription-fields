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

package uk.gov.hmrc.apisubscriptionfields

import java.util.UUID

import uk.gov.hmrc.apisubscriptionfields.model._
import Types._
import cats.data.NonEmptyList
import eu.timepit.refined.api.Refined
import uk.gov.hmrc.apisubscriptionfields.model.DevhubAccessRequirement._

trait FieldDefinitionTestData extends TestData {
  import eu.timepit.refined.auto._

  def fieldN(id: Int): FieldName = {
    val char = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(id)
    Refined.unsafeApply(s"field$char")
  }

  final val AlphanumericFieldName: FieldName = "alphanumericField"
  final val PasswordFieldName: FieldName     = "password"
  final val PPNSFieldFieldName: FieldName    = "callbackurl"

  final val FakeValidationRule: RegexValidationRule = RegexValidationRule(".*")

  final val FakeValidation: ValidationGroup = ValidationGroup("error message", NonEmptyList.one(FakeValidationRule))

  final val FakeUrlValidation: ValidationGroup = ValidationGroup("error message", NonEmptyList.one(UrlValidationRule))

  final val FakeFieldDefinitionUrl                         = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", Some(FakeUrlValidation))
  final val FakeFieldDefinitionUrlValidationEmpty          = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", None)
  final val FakeFieldDefinitionString                      = FieldDefinition(fieldN(2), "desc2", "hint2", FieldDefinitionType.STRING, "short description", Some(FakeValidation))
  final val FakeFieldDefinitionWithAccess: FieldDefinition =
    FakeFieldDefinitionString.copy(validation = None, access = AccessRequirements(devhub = DevhubAccessRequirements(read = AdminOnly)))
  final val FakeFieldDefinitionSecureToken                 = FieldDefinition(fieldN(3), "desc3", "hint3", FieldDefinitionType.SECURE_TOKEN, "short description", Some(FakeValidation))
  final val NelOfFieldDefinitions                          = NonEmptyList.fromListUnsafe(List(FakeFieldDefinitionUrl, FakeFieldDefinitionString, FakeFieldDefinitionSecureToken))
  final val FakeApiFieldDefinitions                        = ApiFieldDefinitions(FakeContext, FakeVersion, NelOfFieldDefinitions)
  final val FakeApiFieldDefinitionsResponse                = ApiFieldDefinitions(FakeContext, FakeVersion, FakeApiFieldDefinitions.fieldDefinitions)

  final val AlphaNumericRegexRule: RegexValidationRule     = RegexValidationRule("^[a-zA-Z0-9]+$")
  final val PasswordRegexRule: RegexValidationRule         = RegexValidationRule("^(?=.{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$")
  final val CallBackUrlRegexRule: RegexValidationRule      = RegexValidationRule("^https.*")
  final val FakeValidationForAlphanumeric: ValidationGroup = ValidationGroup("Needs to be alpha numeric", NonEmptyList.one(AlphaNumericRegexRule))
  final val FakeValidationForPassword: ValidationGroup     =
    ValidationGroup("Needs to be at least 8 chars with at least one lowercase, uppercase and special char", NonEmptyList.one(PasswordRegexRule))
  final val FakeValidationForPPNS: ValidationGroup         =
    ValidationGroup("CallBackUrl Validation", NonEmptyList.one(CallBackUrlRegexRule))

  final val FakeFieldDefinitionAlphnumericField = FieldDefinition(
    "alphanumericField",
    "an alphanumeric filed",
    "this is an alphanumeric value",
    FieldDefinitionType.STRING,
    "an alphanumeric field",
    Some(FakeValidationForAlphanumeric)
  )
  final val FakeFieldDefinitionPassword         =
    FieldDefinition("password", "password", "this is your password", FieldDefinitionType.SECURE_TOKEN, "password", Some(FakeValidationForPassword))

  final val FakeFieldDefinitionPPNSFields        =
    FieldDefinition("callbackurl", "callbackurl", "please enter a callback url", FieldDefinitionType.PPNS_FIELD, "callbackurl", Some(FakeValidationForPPNS))
  final val FakeApiFieldDefinitionssWithRegex    = NonEmptyList.fromListUnsafe(List(FakeFieldDefinitionAlphnumericField, FakeFieldDefinitionPassword))
  final val FakeApiFieldDefinitionsPPNSWithRegex =
    NonEmptyList.fromListUnsafe(List(FakeFieldDefinitionAlphnumericField, FakeFieldDefinitionPassword, FakeFieldDefinitionPPNSFields))

  final val FakeApiFieldDefinitionsWithRegex                              = ApiFieldDefinitions(FakeContext, FakeVersion, FakeApiFieldDefinitionssWithRegex)
  final val FakeApiFieldDefinitionsResponseWithRegex: ApiFieldDefinitions = ApiFieldDefinitions(FakeContext, FakeVersion, FakeApiFieldDefinitionssWithRegex)

  final val FakeApiFieldDefinitionsResponsePPNSWithRegex: ApiFieldDefinitions = ApiFieldDefinitions(FakeContext, FakeVersion, FakeApiFieldDefinitionsPPNSWithRegex)

  final val jsonInvalidRegexFieldsDefinitionRequest =
    """{
      |  "fieldDefinitions" : [ {
      |    "name" : "alphanumericField",
      |    "description" : "an alphanumeric filed",
      |    "hint" : "this is an alphanumeric value",
      |    "type" : "STRING",
      |    "shortDescription" : "an alphanumeric field",
      |    "validation" : {
      |      "errorMessage" : "Needs to be alpha numeric",
      |      "rules" : [ {
      |        "RegexValidationRule" : {
      |          "regex" : "^[a-zA-Z0-9]+$"
      |        }
      |      } ]
      |    }
      |  }, {
      |    "name" : "invalidRegexField",
      |    "description" : "description",
      |    "hint" : "this is your hint",
      |    "type" : "String",
      |    "shortDescription" : "short description",
      |    "validation" : {
      |      "errorMessage" : "An error message",
      |      "rules" : [ {
      |        "RegexValidationRule" : {
      |          "regex" : "*"
      |        }
      |      } ]
      |    }
      |  } ]
      |}""".stripMargin

  def createApiFieldDefinitions(
      apiContext: ApiContext = FakeContext,
      apiVersion: ApiVersion = FakeVersion,
      fieldDefinitions: NonEmptyList[FieldDefinition] = NelOfFieldDefinitions
  ) =
    ApiFieldDefinitions(apiContext, apiVersion, fieldDefinitions)

  def uniqueApiContext = ApiContext(UUID.randomUUID().toString)
}
