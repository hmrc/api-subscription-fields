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


import cats.data.{NonEmptyList => NEL}
import julienrf.json.derived
import play.api.libs.json._
import play.api.libs.functional.syntax._
import julienrf.json.derived.TypeTagSetting.ShortClassName
import Types._
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinitionType.FieldDefinitionType

trait NonEmptyListFormatters {

  implicit def nelReads[A](implicit r: Reads[A]): Reads[NEL[A]] =
    Reads
      .of[List[A]]
      .collect(
        JsonValidationError("expected a NonEmptyList but got an empty list")
      ) {
        case head :: tail => NEL(head, tail)
      }

  implicit def nelWrites[A](implicit w: Writes[A]): Writes[NEL[A]] =
    Writes
      .of[List[A]]
      .contramap(_.toList)
}

trait JsonFormatters extends NonEmptyListFormatters {
  // import play.api.data.format.Formats.uuidFormat
  implicit val SubscriptionFieldsIdjsonFormat = Json.valueFormat[SubscriptionFieldsId]

  import be.venneborg.refined.play.RefinedJsonFormats._
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.auto._
  import play.api.libs.json._

  implicit val FieldNameFormat = formatRefined[String, FieldNameRegex, Refined]

  implicit val FieldsFormat: Format[Fields] = refinedMapFormat[String, FieldNameRegex, Refined]

  implicit val validationRuleFormat: OFormat[ValidationRule] = derived.withTypeTag.oformat(ShortClassName)

  implicit val ValidationJF = Json.format[ValidationGroup]

  implicit val FieldDefinitionTypeReads = Reads.enumNameReads(FieldDefinitionType)

  val fieldDefinitionReads: Reads[FieldDefinition] = (
    (JsPath \ "name").read[FieldName] and
      (JsPath \ "description").read[String] and
      ((JsPath \ "hint").read[String] or Reads.pure("")) and
      (JsPath \ "type").read[FieldDefinitionType] and
      ((JsPath \ "shortDescription").read[String] or Reads.pure("")) and
      (JsPath \ "validation").readNullable[ValidationGroup]
  )(FieldDefinition.apply _)
  val fieldDefinitionWrites = Json.writes[FieldDefinition]

  implicit val FieldDefinitionJF = Format(fieldDefinitionReads, fieldDefinitionWrites)

  implicit val FieldsDefinitionRequestJF = Json.format[FieldsDefinitionRequest]
  implicit val SubscriptionFieldsRequestJF = Json.format[SubscriptionFieldsRequest]

  implicit val FieldsDefinitionResponseJF = Json.format[FieldsDefinitionResponse]
  implicit val BulkFieldsDefinitionsResponseJF = Json.format[BulkFieldsDefinitionsResponse]
  implicit val SubscriptionFieldsResponseJF = Json.format[SubscriptionFieldsResponse]
  implicit val SubscriptionFieldsJF = Json.format[SubscriptionFields]

  implicit val BulkSubscriptionFieldsResponseJF = Json.format[BulkSubscriptionFieldsResponse]


  implicit val SubsFieldValidationResponseJF: OFormat[SubsFieldValidationResponse] = derived.withTypeTag.oformat(ShortClassName)
  implicit val InvalidSubsFieldValidationResponseJF = Json.format[InvalidSubsFieldValidationResponse]
}

object JsonFormatters extends JsonFormatters
