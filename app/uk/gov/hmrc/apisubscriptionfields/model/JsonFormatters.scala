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

import play.api.libs.json._

trait SharedJsonFormatters {
  implicit val SubscriptionFieldsIdJF = new Format[SubscriptionFieldsId] {
    def writes(s: SubscriptionFieldsId) = JsString(s.value.toString)

    def reads(json: JsValue) = json match {
      case JsNull => JsError()
      case _ => JsSuccess(SubscriptionFieldsId(json.as[UUID]))
    }
  }
}

trait JsonFormatters extends SharedJsonFormatters {

  implicit val SubscriptionFieldsResponseJF = Json.format[SubscriptionFieldsResponse]

  implicit val BulkSubscriptionFieldsResponseJF = Json.format[BulkSubscriptionFieldsResponse]

  implicit val SubscriptionFieldsRequestJF = Json.format[SubscriptionFieldsRequest]

  implicit val FieldDefinitionTypeReads = Reads.enumNameReads(FieldDefinitionType)

  implicit val FieldDefinitionJF = Json.format[FieldDefinition]

  implicit val FieldsDefinitionRequestJF = Json.format[FieldsDefinitionRequest]

  implicit val FieldsDefinitionResponseJF = Json.format[FieldsDefinitionResponse]
}

object JsonFormatters extends JsonFormatters
