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

import org.scalatest.TestData
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.{FieldsDefinition, SubscriptionFields}

import scala.concurrent.Future

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
}


trait SubscriptionFieldsTestData extends TestData {

  final val FakeClientId = ClientId(fakeRawClientId)
  final val FakeClientId2 = ClientId(fakeRawClientId2)

  final val FakeRawFieldsId = UUID.randomUUID()
  final val FakeFieldsId = SubscriptionFieldsId(FakeRawFieldsId)

  final val EmptyResponse: Future[Option[SubscriptionFieldsResponse]] = Future.successful(None)
  final val subscriptionFields = Map("A" -> "X", "B" -> "Y")

  final val FakeApiSubscription = SubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeRawFieldsId, subscriptionFields)
  final val FakeSubscriptionFieldsId = SubscriptionFieldsId(FakeRawFieldsId)
  final val FakeSubscriptionFieldsResponse: SubscriptionFieldsResponse = SubscriptionFieldsResponse(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeSubscriptionFieldsId, subscriptionFields)

  def createSubscriptionFieldsWithApiContext(clientId: String = fakeRawClientId, rawContext: String = fakeRawContext) = {
    val subscriptionFields = Map("field_1" -> "value_1", "field_2" -> "value_2", "field_3" -> "value_3")
    SubscriptionFields(clientId, rawContext, fakeRawVersion,  UUID.randomUUID(), subscriptionFields)
  }

  def uniqueClientId = UUID.randomUUID().toString
}

trait FieldsDefinitionTestData extends TestData {
  val FakeValidationRule = ValidationRule(ValidationRuleType.REGEX, "test regex")
  val FakeValidation = Validation("error message", Seq(FakeValidationRule))
  val FakeValidationEmpty = Validation("", Seq.empty)
  final val FakeFieldDefinitionUrl = FieldDefinition("name1", "desc1", "hint1", FieldDefinitionType.URL, "short description", FakeValidation)
  final val FakeFieldDefinitionUrlValidationEmpty = FieldDefinition("name1", "desc1", "hint1", FieldDefinitionType.URL, "short description", FakeValidationEmpty)
  final val FakeFieldDefinitionString = FieldDefinition("name2", "desc2", "hint2", FieldDefinitionType.STRING, "short description", FakeValidation)
  final val FakeFieldDefinitionSecureToken = FieldDefinition("name3", "desc3", "hint3", FieldDefinitionType.SECURE_TOKEN, "short description", FakeValidation)
  final val FakeFieldsDefinitions = Seq(FakeFieldDefinitionUrl, FakeFieldDefinitionString, FakeFieldDefinitionSecureToken)
  final val FakeFieldsDefinition = FieldsDefinition(fakeRawContext, fakeRawVersion, FakeFieldsDefinitions)
  final val FakeFieldsDefinitionResponse = FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, FakeFieldsDefinition.fieldDefinitions)

  def createFieldsDefinition(apiContext: String = fakeRawContext, apiVersion: String = fakeRawVersion, fieldDefinitions: Seq[FieldDefinition] = FakeFieldsDefinitions) =
    FieldsDefinition(apiContext, apiVersion, fieldDefinitions)

  def uniqueApiContext = UUID.randomUUID().toString
}

object SubscriptionFieldsTestData extends SubscriptionFieldsTestData

object RequestHeaders {

  val CONTENT_TYPE_HEADER: (String, String) = CONTENT_TYPE -> MimeTypes.JSON

  val CONTENT_TYPE_HEADER_INVALID: (String, String) = CONTENT_TYPE -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HMRC_JSON_HEADER: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HEADER_INVALID: (String, String) = ACCEPT -> MimeTypes.JSON

  val ValidHeaders = Map(
  CONTENT_TYPE_HEADER,
  ACCEPT_HMRC_JSON_HEADER)
}
