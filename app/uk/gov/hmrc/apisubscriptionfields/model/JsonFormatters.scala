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

import play.api.libs.json._
import uk.gov.hmrc.apiplatform.modules.common.domain.services.NonEmptyListFormatters

trait JsonFormatters extends NonEmptyListFormatters {
  implicit val BoxIdJF: Format[BoxId]                                       = Json.valueFormat[BoxId]
  implicit val SubscriptionFieldsIdjsonFormat: Format[SubscriptionFieldsId] = Json.valueFormat[SubscriptionFieldsId]

  implicit val ApiFieldDefinitionsJF: OFormat[ApiFieldDefinitions]                         = Json.format[ApiFieldDefinitions]
  implicit val BulkApiFieldDefinitionsResponseJF: OFormat[BulkApiFieldDefinitionsResponse] = Json.format[BulkApiFieldDefinitionsResponse]

  implicit val SubscriptionFieldsJF: OFormat[SubscriptionFields]                         = Json.format[SubscriptionFields]
  implicit val BulkSubscriptionFieldsResponseJF: OFormat[BulkSubscriptionFieldsResponse] = Json.format[BulkSubscriptionFieldsResponse]
}

object JsonFormatters extends JsonFormatters
