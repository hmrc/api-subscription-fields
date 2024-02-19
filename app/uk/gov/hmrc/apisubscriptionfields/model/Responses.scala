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

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{Format, JsObject, Json}
import uk.gov.hmrc.apiplatform.modules.common.domain.services.SealedTraitJsonFormatting

import uk.gov.hmrc.apisubscriptionfields.model.Types._

sealed trait SubsFieldsUpsertResponse
case object NotFoundSubsFieldsUpsertResponse                                                  extends SubsFieldsUpsertResponse
case class FailedValidationSubsFieldsUpsertResponse(errorResponses: Map[FieldName, String])   extends SubsFieldsUpsertResponse
case class SuccessfulSubsFieldsUpsertResponse(wrapped: SubscriptionFields, isInsert: Boolean) extends SubsFieldsUpsertResponse

case class BulkSubscriptionFieldsResponse(subscriptions: Seq[SubscriptionFields])

case class BulkApiFieldDefinitionsResponse(apis: Seq[ApiFieldDefinitions])

sealed trait SubsFieldValidationResponse
case object ValidSubsFieldValidationResponse                                          extends SubsFieldValidationResponse
case class InvalidSubsFieldValidationResponse(errorResponses: Map[FieldName, String]) extends SubsFieldValidationResponse

sealed trait ErrorCode

object ErrorCode {

  case object INVALID_REQUEST_PAYLOAD extends ErrorCode
  case object UNKNOWN_ERROR           extends ErrorCode
  case object NOT_FOUND               extends ErrorCode

  val values = Set(INVALID_REQUEST_PAYLOAD, UNKNOWN_ERROR, NOT_FOUND)

  def apply(text: String): Option[ErrorCode] = ErrorCode.values.find(_.toString() == text.toUpperCase)

  def unsafeApply(text: String): ErrorCode = apply(text).getOrElse(throw new RuntimeException(s"$text is not a valid Error Code"))

  implicit val format: Format[ErrorCode] = SealedTraitJsonFormatting.createFormatFor[ErrorCode]("Error Code", apply)
}

object JsErrorResponse {

  def apply(errorCode: ErrorCode, message: JsValueWrapper): JsObject =
    Json.obj(
      "code"    -> errorCode.toString,
      "message" -> message
    )
}
