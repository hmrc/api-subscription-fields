/*
 * Copyright 2017 HM Revenue & Customs
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

case class AppId(value: String) extends AnyVal

case class ApiContext(value: String) extends AnyVal

case class ApiVersion(value: String) extends AnyVal

case class SubscriptionFieldsId(value: UUID) extends AnyVal

object SubscriptionIdentifier extends Decoder[SubscriptionIdentifier] {
  override val separator = Separator
  override val numOfParts: Int = 3

  override protected def decode(tokens: Seq[String]): SubscriptionIdentifier =
    SubscriptionIdentifier(AppId(tokens(0)), ApiContext(tokens(1)), ApiVersion(tokens(2)))
}

case class SubscriptionIdentifier(applicationId: AppId, apiContext: ApiContext, apiVersion: ApiVersion) extends Encoder[SubscriptionIdentifier] {
  override val separator = Separator

  def encode(): String =
    encode(applicationId.value, apiContext.value, apiVersion.value)

}

object FieldsDefinitionIdentifier extends Decoder[FieldsDefinitionIdentifier] {
  override val separator = Separator
  override val numOfParts: Int = 2

  override protected def decode(tokens: Seq[String]): FieldsDefinitionIdentifier =
    FieldsDefinitionIdentifier(ApiContext(tokens(0)), ApiVersion(tokens(1)))
}

case class FieldsDefinitionIdentifier(apiContext: ApiContext, apiVersion: ApiVersion) extends Encoder[FieldsDefinitionIdentifier] {
  override val separator = Separator

  def encode(): String = encode(apiContext.value, apiVersion.value)
}

object FieldDefinitionType extends Enumeration {
  type FieldDefinitionType = Value

  val URL = Value("URL")
  val SECURE_TOKEN = Value("SecureToken")
  val STRING = Value("STRING")

}

case class FieldDefinition(name: String, description: String, `type`: FieldDefinitionType)
