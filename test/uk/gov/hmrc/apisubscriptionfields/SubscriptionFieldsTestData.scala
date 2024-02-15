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
import scala.concurrent.Future

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}

import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.model._

trait SubscriptionFieldsTestData extends FieldDefinitionTestData with ValidationRuleTestData {
  import eu.timepit.refined.auto._

  final val FakeRawFieldsId = UUID.randomUUID()
  final val FakeFieldsId    = SubscriptionFieldsId(FakeRawFieldsId)

  final val EmptyResponse: Future[Option[SubscriptionFields]] = Future.successful(None)
  final val FakeSubscriptionFields: Map[FieldName, String]    = Map(fieldN(1) -> "X", fieldN(2) -> "Y")
  final val SubscriptionFieldsMatchRegexValidation: Fields    = Map(AlphanumericFieldName -> "ABC123ab", PasswordFieldName -> "Qw12@ert")
  final val SubscriptionFieldsNonMatchRegexValidation: Fields = Map(AlphanumericFieldName -> "ABC123a", PasswordFieldName -> "Qw12@er")

  final val SubscriptionFieldsMatchRegexValidationPPNS: Fields      =
    Map(AlphanumericFieldName -> "ABC123abc", PasswordFieldName -> "Qw12@erty", PPNSFieldFieldName -> "https://www.mycallbackurl.com")
  final val SubscriptionFieldsDoNotMatchRegexValidationPPNS: Fields = Map(AlphanumericFieldName -> "ABC123abc", PasswordFieldName -> "Qw12@erty", PPNSFieldFieldName -> "foo")
  final val SubscriptionFieldsEmptyValueRegexValidationPPNS: Fields = Map(AlphanumericFieldName -> "ABC123abc", PasswordFieldName -> "Qw12@erty", PPNSFieldFieldName -> "")
  final val SubscriptionFieldsDoNotMatchRegexValidation: Fields     = Map(AlphanumericFieldName -> "ABC123abc=", PasswordFieldName -> "Qw12erty")

  final val FakeApiSubscription                                               = SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, FakeSubscriptionFields)
  final val FakeSubscriptionFieldsResponse: SubscriptionFields                = SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, FakeSubscriptionFields)
  final val FakeValidSubsFieldValidationResponse: SubsFieldValidationResponse = ValidSubsFieldValidationResponse

  final val CallbackUrlFieldName: FieldName    = "callbackUrl"
  final val FakeFieldErrorMessage1: FieldError = ((CallbackUrlFieldName, "Invalid Callback URL"))

  final val EoriFieldName: FieldName = "EORI"
  final val FakeFieldErrorMessage2   = ((EoriFieldName, "Invalid EORI"))

  final val FakeFieldErrorMessages                                              = Map(
    (CallbackUrlFieldName -> FakeFieldErrorMessage1._2),
    (EoriFieldName        -> FakeFieldErrorMessage2._2)
  )
  final val FakeInvalidSubsFieldValidationResponse: SubsFieldValidationResponse = InvalidSubsFieldValidationResponse(FakeFieldErrorMessages)

  final val FakeFieldErrorForAlphanumeric: FieldError = ((AlphanumericFieldName, "Needs to be alpha numeric"))
  final val FakeFieldErrorForPassword                 = ((PasswordFieldName, "Needs to be at least 8 chars with at least one lowercase, uppercase and special char"))

  final val FakeInvalidSubsFieldValidationResponse2 = InvalidSubsFieldValidationResponse(errorResponses =
    Map(
      (AlphanumericFieldName -> FakeFieldErrorForAlphanumeric._2),
      (PasswordFieldName     -> FakeFieldErrorForPassword._2)
    )
  )

  def subsFieldsFor(fields: Fields): SubscriptionFields = SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fields)

  // TODO sort this
  def createSubscriptionFieldsWithApiContext(clientId: ClientId = FakeClientId, rawContext: String = fakeRawContext) = {
    val subscriptionFields: Fields = Map(fieldN(1) -> "value_1", fieldN(2) -> "value_2", fieldN(3) -> "value_3")
    SubscriptionFields(clientId, ApiContext(rawContext), ApiVersionNbr(fakeRawVersion), FakeFieldsId, subscriptionFields)
  }

  def uniqueClientId = ClientId(UUID.randomUUID().toString)
}

object SubscriptionFieldsTestData extends SubscriptionFieldsTestData
