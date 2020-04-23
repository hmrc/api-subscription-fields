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
import Types._
import play.api.libs.json.Json.JsValueWrapper
import uk.gov.hmrc.apisubscriptionfields.model.DevhubAccessLevel.{Admininstator,Developer}

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

trait AccessLevelRequirementsFormatters {

  def ignoreDefaultField[T](value: T, default: T, jsonFieldName: String)(implicit w: Writes[T]) =
    if(value == default) None else Some((jsonFieldName, Json.toJsFieldJsValueWrapper(value)))

  implicit val DevhubAccessLevelRequirementFormat: Format[DevhubAccessLevelRequirement] = new Format[DevhubAccessLevelRequirement] {

    override def writes(o: DevhubAccessLevelRequirement): JsValue = JsString(o match {
      case Admininstator => "administrator"
      case Developer => "developer"
      case DevhubAccessLevelRequirement.NoOne => "noone"
    })

    override def reads(json: JsValue): JsResult[DevhubAccessLevelRequirement] = json match {
      case JsString("administrator") => JsSuccess(Admininstator)
      case JsString("developerdeveloper") => JsSuccess(Developer)
      case JsString("noone") => JsSuccess(DevhubAccessLevelRequirement.NoOne)
      case _ => JsError("Not a recognized DevhubAccessLevelRequirement")
    }
  }

  implicit val DevhubAccessLevelRequirementsReads: Reads[DevhubAccessLevelRequirements] = (
    ((JsPath \ "readOnly").read[DevhubAccessLevelRequirement] or Reads.pure(DevhubAccessLevelRequirement.Default)) and
    ((JsPath \ "readWrite").read[DevhubAccessLevelRequirement] or Reads.pure(DevhubAccessLevelRequirement.Default))
  )(DevhubAccessLevelRequirements.apply _)

  implicit val DevhubAccessLevelRequirementsWrites: OWrites[DevhubAccessLevelRequirements] = new OWrites[DevhubAccessLevelRequirements] {
    def writes(requirements: DevhubAccessLevelRequirements) = {
      Json.obj(
        (
          ignoreDefaultField(requirements.readOnly, DevhubAccessLevelRequirement.Default, "readOnly") ::
          ignoreDefaultField(requirements.readWrite, DevhubAccessLevelRequirement.Default, "readWrite") ::
          List.empty[Option[(String, JsValueWrapper)]]
        ).filterNot(_.isEmpty).map(_.get): _*
      )
    }
  }

  implicit val AccessLevelRequirementsReads: Reads[AccessLevelRequirements] = Json.reads[AccessLevelRequirements]

  implicit val AccessLevelRequirementsWrites: Writes[AccessLevelRequirements] = Json.writes[AccessLevelRequirements]
}

trait JsonFormatters extends NonEmptyListFormatters with AccessLevelRequirementsFormatters {
  import be.venneborg.refined.play.RefinedJsonFormats._
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.auto._
  import play.api.libs.json._


  final val defaultTypeFormat = (__ \ "type").format[String]

  implicit val SubscriptionFieldsIdjsonFormat = Json.valueFormat[SubscriptionFieldsId]

  implicit val FieldNameFormat = formatRefined[String, FieldNameRegex, Refined]

  implicit val FieldsFormat: Format[Fields] = refinedMapFormat[String, FieldNameRegex, Refined]

  implicit val ValidationRuleFormat: OFormat[ValidationRule] = derived.withTypeTag.oformat(ShortClassName)

  implicit val ValidationJF = Json.format[ValidationGroup]

  implicit val FieldDefinitionTypeReads = Reads.enumNameReads(FieldDefinitionType)

  implicit val FieldDefinitionReads: Reads[FieldDefinition] = (
    (JsPath \ "name").read[FieldName] and
    (JsPath \ "description").read[String] and
    ((JsPath \ "hint").read[String] or Reads.pure("")) and
    (JsPath \ "type").read[FieldDefinitionType] and
    ((JsPath \ "shortDescription").read[String] or Reads.pure("")) and
    (JsPath \ "validation").readNullable[ValidationGroup] and
    ((JsPath \ "access").read[AccessLevelRequirements] or Reads.pure(AccessLevelRequirements.Default))
  )(FieldDefinition.apply _)

  def ignoreDefaultField[T](value: T, default: T, jsonFieldName: String)(implicit w: Writes[T]) =
    if(value == default) None else Some((jsonFieldName, Json.toJsFieldJsValueWrapper(value)))

  def ignoreNone[T](value: Option[T], jsonFieldName: String)(implicit w: Writes[T]) =
    if(value.isEmpty) None else Some((jsonFieldName, Json.toJsFieldJsValueWrapper(value)))

  def dropTail[A,B,C,D,E,F,G]( t: Tuple7[A,B,C,D,E,F,G] ): Tuple6[A,B,C,D,E,F] = (t._1, t._2, t._3, t._4, t._5, t._6)

  implicit val FieldDefinitionWrites: Writes[FieldDefinition] = {
    val common =
        (JsPath \ "name").write[FieldName] and
        (JsPath \ "description").write[String] and
        (JsPath \ "hint").write[String] and
        (JsPath \ "type").write[FieldDefinitionType] and
        (JsPath \ "shortDescription").write[String] and
        (JsPath \ "validation").writeNullable[ValidationGroup]


    if(self.access == AccessLevelRequirements.Default) {
      (common)(unlift(FieldDefinition.unapply).andThen(dropTail))
    } else {
      (common and (JsPath \ "access").write[AccessLevelRequirements])(unlift(FieldDefinition.unapply))
    }
  }

  implicit val FieldsDefinitionJF: OFormat[FieldsDefinition] = Json.format[FieldsDefinition]

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
