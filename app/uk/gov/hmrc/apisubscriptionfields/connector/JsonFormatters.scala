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

package uk.gov.hmrc.apisubscriptionfields.connector

import play.api.libs.json._
import uk.gov.hmrc.apisubscriptionfields.model.SpecialJsonFormatters

trait JsonFormatters extends SpecialJsonFormatters {
  implicit val createTopicRequestJF = Json.format[CreateTopicRequest]
  implicit val createTopicResponseJF = Json.format[CreateTopicResponse]
  implicit val subscribersRequestJF = Json.format[SubscribersRequest]
  implicit val updateSubscribersRequestJF = Json.format[UpdateSubscribersRequest]
  implicit val updateSubscribersResponseJF = Json.format[UpdateSubscribersResponse]
}

object JsonFormatters extends JsonFormatters
