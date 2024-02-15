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

package uk.gov.hmrc.apisubscriptionfields.connector

import play.api.libs.json._

import uk.gov.hmrc.apisubscriptionfields.model.{BoxId, SubscriptionFieldsId}

trait JsonFormatters {
  implicit val boxIdJF: Format[BoxId]                               = Json.valueFormat[BoxId]
  implicit val subscriptionFieldsIdJF: Format[SubscriptionFieldsId] = Json.valueFormat[SubscriptionFieldsId]

  implicit val createBoxRequestJF: OFormat[CreateBoxRequest]                   = Json.format[CreateBoxRequest]
  implicit val createBoxResponseJF: OFormat[CreateBoxResponse]                 = Json.format[CreateBoxResponse]
  implicit val subscriberRequestJF: OFormat[SubscriberRequest]                 = Json.format[SubscriberRequest]
  implicit val updateSubscriberRequestJF: OFormat[UpdateSubscriberRequest]     = Json.format[UpdateSubscriberRequest]
  implicit val updateSubscriberResponseJF: OFormat[UpdateSubscriberResponse]   = Json.format[UpdateSubscriberResponse]
  implicit val updateCallBackUrlRequestJF: OFormat[UpdateCallBackUrlRequest]   = Json.format[UpdateCallBackUrlRequest]
  implicit val updateCallBackUrlResponseJF: OFormat[UpdateCallBackUrlResponse] = Json.format[UpdateCallBackUrlResponse]
}

object JsonFormatters extends JsonFormatters
