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


import cats.data.{NonEmptyList => NEL}
import julienrf.json.derived
import play.api.libs.json._
import play.api.libs.functional.syntax._
import julienrf.json.derived.TypeTagSetting.ShortClassName
import Types._
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinitionType.FieldDefinitionType
import Types._
import play.api.libs.json.Json.JsValueWrapper

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

object NonEmptyListFormatters extends NonEmptyListFormatters

trait AccessRequirementsFormatters {
    import DevhubAccessRequirement._

  def ignoreDefaultField[T](value: T, default: T, jsonFieldName: String)(implicit w: Writes[T]) =
    if(value == default) None else Some((jsonFieldName, Json.toJsFieldJsValueWrapper(value)))

  implicit val DevhubAccessRequirementFormat: Format[DevhubAccessRequirement] = new Format[DevhubAccessRequirement] {

    override def writes(o: DevhubAccessRequirement): JsValue = JsString(o match {
      case AdminOnly => "adminOnly"
      case Anyone => "anyone"
      case NoOne => "noOne"
    })

    override def reads(json: JsValue): JsResult[DevhubAccessRequirement] = json match {
      case JsString("adminOnly") => JsSuccess(AdminOnly)
      case JsString("anyone") => JsSuccess(Anyone)
      case JsString("noOne") => JsSuccess(NoOne)
      case _ => JsError("Not a recognized DevhubAccessRequirement")
    }
  }

  implicit val DevhubAccessRequirementsReads: Reads[DevhubAccessRequirements] = (
    ((JsPath \ "read").read[DevhubAccessRequirement] or Reads.pure(DevhubAccessRequirement.Default)) and
    ((JsPath \ "write").read[DevhubAccessRequirement] or Reads.pure(DevhubAccessRequirement.Default))
  )(DevhubAccessRequirements.apply _)

  implicit val DevhubAccessRequirementsWrites: OWrites[DevhubAccessRequirements] = new OWrites[DevhubAccessRequirements] {
    def writes(requirements: DevhubAccessRequirements) = {
      Json.obj(
        (
          ignoreDefaultField(requirements.read, DevhubAccessRequirement.Default, "read") ::
          ignoreDefaultField(requirements.write, DevhubAccessRequirement.Default, "write") ::
          List.empty[Option[(String, JsValueWrapper)]]
        ).filterNot(_.isEmpty).map(_.get): _*
      )
    }
  }

  implicit val AccessRequirementsReads: Reads[AccessRequirements] = Json.reads[AccessRequirements]

  implicit val AccessRequirementsWrites: Writes[AccessRequirements] = Json.writes[AccessRequirements]
}

object AccessRequirementsFormatters extends AccessRequirementsFormatters

trait JsonFormatters
    extends NonEmptyListFormatters
    with AccessRequirementsFormatters {

  import be.venneborg.refined.play.RefinedJsonFormats._
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.auto._
  import play.api.libs.json._

  implicit val ClientIdJF = Json.valueFormat[ClientId]
  implicit val BoxIdJF = Json.valueFormat[BoxId]
  implicit val SubscriptionFieldsIdjsonFormat = Json.valueFormat[SubscriptionFieldsId]
  implicit val ApiContextJF = Json.valueFormat[ApiContext]
  implicit val ApiVersionJF = Json.valueFormat[ApiVersion]

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
    ((JsPath \ "access").read[AccessRequirements] or Reads.pure(AccessRequirements.Default))
  )(FieldDefinition.apply _)

  implicit val FieldDefinitionWrites: Writes[FieldDefinition] = new Writes[FieldDefinition] {

    def dropTail[A,B,C,D,E,F,G]( t: Tuple7[A,B,C,D,E,F,G] ): Tuple6[A,B,C,D,E,F] = (t._1, t._2, t._3, t._4, t._5, t._6)

    // This allows us to hide default AccessRequirements from JSON - as this is a rarely used field
    // but not one that business logic would want as an optional field and require getOrElse everywhere.
    override def writes(o: FieldDefinition): JsValue = {
      val common =
          (JsPath \ "name").write[FieldName] and
          (JsPath \ "description").write[String] and
          (JsPath \ "hint").write[String] and
          (JsPath \ "type").write[FieldDefinitionType] and
          (JsPath \ "shortDescription").write[String] and
          (JsPath \ "validation").writeNullable[ValidationGroup]

      (if(o.access == AccessRequirements.Default) {
        (common)(unlift(FieldDefinition.unapply).andThen(dropTail))
      } else {
        (common and (JsPath \ "access").write[AccessRequirements])(unlift(FieldDefinition.unapply))
      }).writes(o)
    }
  }

  implicit val ApiFieldDefinitionsJF: OFormat[ApiFieldDefinitions] = Json.format[ApiFieldDefinitions]
  implicit val BulkApiFieldDefinitionsResponseJF = Json.format[BulkApiFieldDefinitionsResponse]
  implicit val SubsFieldValidationResponseJF: OFormat[SubsFieldValidationResponse] = derived.withTypeTag.oformat(ShortClassName)
  implicit val InvalidSubsFieldValidationResponseJF = Json.format[InvalidSubsFieldValidationResponse]

  implicit val SubscriptionFieldsJF = Json.format[SubscriptionFields]
  implicit val BulkSubscriptionFieldsResponseJF = Json.format[BulkSubscriptionFieldsResponse]
}

object JsonFormatters extends JsonFormatters
