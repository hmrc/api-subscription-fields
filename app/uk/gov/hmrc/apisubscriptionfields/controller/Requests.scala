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

package uk.gov.hmrc.apisubscriptionfields.controller

import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinition
import cats.data.NonEmptyList

private[controller] case class SubscriptionFieldsRequest(fields: Fields)

private[controller] case class FieldDefinitionsRequest(fieldDefinitions: NonEmptyList[FieldDefinition])


object SubscriptionFieldsRequest {
  import play.api.libs.json._
  import be.venneborg.refined.play.RefinedJsonFormats._
  import eu.timepit.refined.auto._

  implicit val SubscriptionFieldsRequestJF = Json.format[SubscriptionFieldsRequest]
}

object FieldDefinitionsRequest {
  import play.api.libs.json._
  import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters._

  implicit val FieldDefinitionsRequestJF = Json.format[FieldDefinitionsRequest]
}
