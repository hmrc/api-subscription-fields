/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.apisubscriptionfields.model.{ClientId, BoxId, SubscriptionFieldsId}

trait JsonFormatters {
  implicit val clientIdJF = Json.valueFormat[ClientId]
  implicit val boxIdJF = Json.valueFormat[BoxId]
  implicit val subscriptionFieldsIdJF = Json.valueFormat[SubscriptionFieldsId]

  implicit val createBoxRequestJF = Json.format[CreateBoxRequest]
  implicit val createBoxResponseJF = Json.format[CreateBoxResponse]
  implicit val subscriberRequestJF = Json.format[SubscriberRequest]
  implicit val updateSubscriberRequestJF = Json.format[UpdateSubscriberRequest]
  implicit val updateSubscriberResponseJF = Json.format[UpdateSubscriberResponse]
  implicit val updateCallBackUrlRequestJF = Json.format[UpdateCallBackUrlRequest]
  implicit val updateCallBackUrlResponseJF = Json.format[UpdateCallBackUrlResponse]
}

object JsonFormatters extends JsonFormatters
