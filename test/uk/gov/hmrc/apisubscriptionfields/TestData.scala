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

import cats.data.NonEmptyList
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.{FieldsDefinition, SubscriptionFields}

import scala.concurrent.Future
import eu.timepit.refined.auto._

trait TestData {
  final val unit = ()
  type EmulatedFailure = UnsupportedOperationException
  final val emulatedFailure = new EmulatedFailure("Emulated failure.")
  final val fakeRawClientId = UUID.randomUUID().toString
  final val fakeRawClientId2 = UUID.randomUUID().toString
  final val fakeRawContext = "acontext"
  final val fakeRawContext2 = "acontext2"
  final val fakeRawVersion = "1.0.2"
  final val FakeContext = ApiContext(fakeRawContext)
  final val FakeContext2 = ApiContext(fakeRawContext2)
  final val FakeVersion = ApiVersion(fakeRawVersion)

  def fieldN(id: Int): String = s"field_$id"
}

trait SubscriptionFieldsTestData extends TestData with ValidationRuleTestData {

  final val FakeClientId = ClientId(fakeRawClientId)
  final val FakeClientId2 = ClientId(fakeRawClientId2)

  final val FakeRawFieldsId = UUID.randomUUID()
  final val FakeFieldsId = SubscriptionFieldsId(FakeRawFieldsId)

  final val EmptyResponse: Future[Option[SubscriptionFieldsResponse]] = Future.successful(None)
  final val subscriptionFields = Map(fieldN(1) -> "X", fieldN(2) -> "Y")
  final val subscriptionFieldsMatchRegexValidation = Map("alphanumericField" -> "ABC123abc", "password" -> "Qw12@erty")
  final val subscriptionFieldsDoNotMatchRegexValidation = Map("alphanumericField" -> "ABC123abc=", "password" -> "Qw12erty")

  final val FakeApiSubscription = SubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeRawFieldsId, subscriptionFields)
  final val FakeSubscriptionFieldsId = SubscriptionFieldsId(FakeRawFieldsId)
  final val FakeSubscriptionFieldsResponse: SubscriptionFieldsResponse =
    SubscriptionFieldsResponse(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeSubscriptionFieldsId, subscriptionFields)
  final val FakeValidSubsFieldValidationResponse: SubsFieldValidationResponse = ValidSubsFieldValidationResponse
  final val FakeFieldErrorMessage1 = (("callbackUrl", "Invalid Callback URL"))
  final val FakeFieldErrorMessage2 = (("EORI", "Invalid EORI"))
  final val FakeFieldErrorMessages = Map(
    (FakeFieldErrorMessage1._1 -> FakeFieldErrorMessage1._2),
    (FakeFieldErrorMessage2._1 -> FakeFieldErrorMessage2._2)
  )
  final val FakeInvalidSubsFieldValidationResponse: SubsFieldValidationResponse = InvalidSubsFieldValidationResponse(FakeFieldErrorMessages)

  final val FakeFieldErrorForAlphanumeric = (("alphanumericField", "Needs to be alpha numeric"))
  final val FakeFieldErrorForPassword = (("password", "Needs to be at least 8 chars with at least one lowercase, uppercase and special char"))
  final val FakeInvalidSubsFieldValidationResponse2 = InvalidSubsFieldValidationResponse(errorResponses = Map(
    (FakeFieldErrorForAlphanumeric._1 -> FakeFieldErrorForAlphanumeric._2),
    (FakeFieldErrorForPassword._1 -> FakeFieldErrorForPassword._2)
  ))

  def createSubscriptionFieldsWithApiContext(clientId: String = fakeRawClientId, rawContext: String = fakeRawContext) = {
    val subscriptionFields = Map(fieldN(1) -> "value_1", fieldN(2) -> "value_2", fieldN(3) -> "value_3")
    SubscriptionFields(clientId, rawContext, fakeRawVersion, UUID.randomUUID(), subscriptionFields)
  }

  def uniqueClientId = UUID.randomUUID().toString
}

trait FieldsDefinitionTestData extends TestData {

  val FakeValidationRule: RegexValidationRule = RegexValidationRule(".*")
  val FakeValidation: ValidationGroup = ValidationGroup("error message", NonEmptyList.one(FakeValidationRule))
  val FakeUrlValidation: ValidationGroup = ValidationGroup("error message", NonEmptyList.one(UrlValidationRule))
  final val FakeFieldDefinitionUrl = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", Some(FakeUrlValidation))
  final val FakeFieldDefinitionUrlValidationEmpty = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", None)
  final val FakeFieldDefinitionString = FieldDefinition(fieldN(2), "desc2", "hint2", FieldDefinitionType.STRING, "short description", Some(FakeValidation))
  final val FakeFieldDefinitionSecureToken = FieldDefinition(fieldN(3), "desc3", "hint3", FieldDefinitionType.SECURE_TOKEN, "short description", Some(FakeValidation))
  final val FakeFieldsDefinitions = NonEmptyList.fromListUnsafe(List(FakeFieldDefinitionUrl, FakeFieldDefinitionString, FakeFieldDefinitionSecureToken))
  final val FakeFieldsDefinition = FieldsDefinition(fakeRawContext, fakeRawVersion, FakeFieldsDefinitions)
  final val FakeFieldsDefinitionResponse = FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, FakeFieldsDefinition.fieldDefinitions)

  final val alphaNumericRegexRule: RegexValidationRule = RegexValidationRule("^[a-zA-Z0-9]+$")
  final val passwordRegexRule: RegexValidationRule = RegexValidationRule("^(?=.{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$")
  final val FakeValidationForAlphanumeric: ValidationGroup = ValidationGroup("Needs to be alpha numeric", NonEmptyList.one(alphaNumericRegexRule))
  final val FakeValidationForPassword: ValidationGroup =
    ValidationGroup("Needs to be at least 8 chars with at least one lowercase, uppercase and special char", NonEmptyList.one(passwordRegexRule))
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

object SubscriptionFieldsTestData extends SubscriptionFieldsTestData

object RequestHeaders {

  val CONTENT_TYPE_HEADER: (String, String) = CONTENT_TYPE -> MimeTypes.JSON

  val CONTENT_TYPE_HEADER_INVALID: (String, String) = CONTENT_TYPE -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HMRC_JSON_HEADER: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HEADER_INVALID: (String, String) = ACCEPT -> MimeTypes.JSON

  val ValidHeaders = Map(CONTENT_TYPE_HEADER, ACCEPT_HMRC_JSON_HEADER)
}
