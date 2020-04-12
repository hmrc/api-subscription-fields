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

import java.util.UUID

import cats.data.{NonEmptyList => NEL, NonEmptyMap => NEM}
import julienrf.json.derived
import julienrf.json.derived.TypeTagSetting
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinitionType.FieldDefinitionType
import uk.gov.hmrc.apisubscriptionfields.model._
import cats.kernel.Order
import scala.collection.immutable.SortedMap

trait SharedJsonFormatters {
  implicit val SubscriptionFieldsIdJF = new Format[SubscriptionFieldsId] {
    def writes(s: SubscriptionFieldsId) = JsString(s.value.toString)

    def reads(json: JsValue) = json match {
      case JsNull => JsError()
      case _      => JsSuccess(SubscriptionFieldsId(json.as[UUID]))
    }
  }
}

trait NonEmptyFormatters {

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

  implicit def nemReads[K, V](implicit r: Reads[Map[K, V]], ordK: Order[K]): Reads[NEM[K, V]] =
    Reads
      .of[Map[K, V]]
      .collect(
        JsonValidationError("expected at least one entry but got none")
      ) {
        case kvs if kvs.size > 1 => NEM.fromMapUnsafe(SortedMap(kvs.toList: _*)(ordK.toOrdering))
      }

  implicit def nemWrites[K, V](implicit m: Writes[SortedMap[K, V]]): Writes[NEM[K, V]] =
    Writes
      .of[SortedMap[K, V]]
      .contramap(_.toSortedMap)
}

object NonEmptyFormatters extends NonEmptyFormatters

trait JsonFormatters extends SharedJsonFormatters with NonEmptyFormatters {

  implicit val validationRuleFormat: OFormat[ValidationRule] = derived.withTypeTag.oformat(TypeTagSetting.ShortClassName)

  implicit val ValidationJF = Json.format[ValidationGroup]

  implicit val FieldDefinitionTypeReads = Reads.enumNameReads(FieldDefinitionType)
  val fieldDefinitionReads: Reads[FieldDefinition] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      ((JsPath \ "hint").read[String] or Reads.pure("")) and
      (JsPath \ "type").read[FieldDefinitionType] and
      ((JsPath \ "shortDescription").read[String] or Reads.pure("")) and
      (JsPath \ "validation").readNullable[ValidationGroup]
  )(FieldDefinition.apply _)

  implicit val FieldsOrd = Order.fromOrdering(scala.math.Ordering.String)
  implicit val FieldsJF = Format(nemReads[String, String], nemWrites[String, String])

  val fieldDefinitionWrites = Json.writes[FieldDefinition]

  implicit val FieldDefinitionJF = Format(fieldDefinitionReads, fieldDefinitionWrites)

  implicit val FieldErrorMessageJF = Json.format[FieldErrorMessage]

  implicit val FieldsDefinitionRequestJF = Json.format[FieldsDefinitionRequest]
  implicit val SubscriptionFieldsRequestJF = Json.format[SubscriptionFieldsRequest]

  implicit val FieldsDefinitionResponseJF = Json.format[FieldsDefinitionResponse]
  implicit val BulkFieldsDefinitionsResponseJF = Json.format[BulkFieldsDefinitionsResponse]
  implicit val SubscriptionFieldsResponseJF = Json.format[SubscriptionFieldsResponse]

  implicit val BulkSubscriptionFieldsResponseJF = Json.format[BulkSubscriptionFieldsResponse]

  implicit val SubsFieldValidationResponseJF: OFormat[SubsFieldValidationResponse] = derived.withTypeTag.oformat(TypeTagSetting.ShortClassName)
  implicit val InvalidSubsFieldValidationResponseJF = Json.format[InvalidSubsFieldValidationResponse]
}

object JsonFormatters extends JsonFormatters
