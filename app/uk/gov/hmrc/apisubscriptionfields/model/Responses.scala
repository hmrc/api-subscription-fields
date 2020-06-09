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

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import cats.data.NonEmptyList
import Types._

sealed trait SubsFieldsUpsertResponse
case object NotFoundSubsFieldsUpsertResponse extends SubsFieldsUpsertResponse
case class FailedValidationSubsFieldsUpsertResponse(errorResponses: Map[FieldName, String]) extends SubsFieldsUpsertResponse
case class SuccessfulSubsFieldsUpsertResponse(wrapped: SubscriptionFieldsResponse, isInsert: Boolean) extends SubsFieldsUpsertResponse

// TODO - remove this due to duplication with SubscriptionFields
case class SubscriptionFieldsResponse(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, fieldsId: SubscriptionFieldsId, fields: Fields)

case class BulkSubscriptionFieldsResponse(subscriptions: Seq[SubscriptionFieldsResponse])

// TODO - remove this due to duplication with ApiFieldDefinitions
case class ApiFieldDefinitionsResponse(apiContext: ApiContext, apiVersion: ApiVersion, fieldDefinitions: NonEmptyList[FieldDefinition])

case class BulkApiFieldDefinitionsResponse(apis: Seq[ApiFieldDefinitionsResponse])

sealed trait SubsFieldValidationResponse
case object ValidSubsFieldValidationResponse extends SubsFieldValidationResponse
case class InvalidSubsFieldValidationResponse(errorResponses: Map[FieldName, String]) extends SubsFieldValidationResponse

object ErrorCode extends Enumeration {
  type ErrorCode = Value

  val INVALID_REQUEST_PAYLOAD = Value("INVALID_REQUEST_PAYLOAD")
  val UNKNOWN_ERROR = Value("UNKNOWN_ERROR")
  val NOT_FOUND_CODE = Value("NOT_FOUND")
}

object JsErrorResponse {
  def apply(errorCode: ErrorCode.Value, message: JsValueWrapper): JsObject =
    Json.obj(
      "code" -> errorCode.toString,
      "message" -> message
    )
}
