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

import play.api.libs.functional.syntax._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import uk.gov.hmrc.apiplatform.modules.common.domain.services.NonEmptyListFormatters

import uk.gov.hmrc.apisubscriptionfields.model.Types._

trait AccessRequirementsFormatters {
  import DevhubAccessRequirement._

  def ignoreDefaultField[T](value: T, default: T, jsonFieldName: String)(implicit w: Writes[T]) =
    if (value == default) None else Some((jsonFieldName, Json.toJsFieldJsValueWrapper(value)))

  implicit val DevhubAccessRequirementFormat: Format[DevhubAccessRequirement] = new Format[DevhubAccessRequirement] {

    override def writes(o: DevhubAccessRequirement): JsValue = JsString(o match {
      case AdminOnly => "adminOnly"
      case Anyone    => "anyone"
      case NoOne     => "noOne"
    })

    override def reads(json: JsValue): JsResult[DevhubAccessRequirement] = json match {
      case JsString("adminOnly") => JsSuccess(AdminOnly)
      case JsString("anyone")    => JsSuccess(Anyone)
      case JsString("noOne")     => JsSuccess(NoOne)
      case _                     => JsError("Not a recognized DevhubAccessRequirement")
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

trait JsonFormatters extends NonEmptyListFormatters with AccessRequirementsFormatters with DefaultReads with DefaultWrites with RefinedJson {
  implicit val BoxIdJF: Format[BoxId]                                       = Json.valueFormat[BoxId]
  implicit val SubscriptionFieldsIdjsonFormat: Format[SubscriptionFieldsId] = Json.valueFormat[SubscriptionFieldsId]

  implicit val RegexValidationRuleFormat: OFormat[RegexValidationRule] = Json.format[RegexValidationRule]

  implicit val ValidationRuleReads: Reads[ValidationRule] = new Reads[ValidationRule] {

    def reads(json: JsValue): JsResult[ValidationRule] = json match {
      case JsObject(fields) if (fields.keys.size == 1) =>
        fields.toList.head match {
          case ("RegexValidationRule", v) => Json.fromJson[RegexValidationRule](v)
          case ("UrlValidationRule", _)   => JsSuccess(UrlValidationRule)
          case (k, v)                     => JsError(s"$k is not a valid validation rule")
        }
      case _                                           => JsError("Cannot read validation rule")
    }
  }

  implicit val ValidationRuleWrites: Writes[ValidationRule] = new Writes[ValidationRule] {

    def writes(o: ValidationRule): JsValue = o match {
      case r: RegexValidationRule => Json.obj("RegexValidationRule" -> Json.toJson(r))
      case u @ UrlValidationRule  => Json.obj("UrlValidationRule" -> Json.obj())
    }
  }

  implicit val ValidationJF: OFormat[ValidationGroup] = Json.format[ValidationGroup]

  implicit val FieldNameFormat: Format[FieldName] = formatRefined[String, FieldNameRegex]

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

    def dropTail[A, B, C, D, E, F, G](t: Tuple7[A, B, C, D, E, F, G]): Tuple6[A, B, C, D, E, F] = (t._1, t._2, t._3, t._4, t._5, t._6)

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

      (if (o.access == AccessRequirements.Default) {
         (common)(unlift(FieldDefinition.unapply).andThen(dropTail))
       } else {
         (common and (JsPath \ "access").write[AccessRequirements])(unlift(FieldDefinition.unapply))
       }).writes(o)
    }
  }

  implicit val ApiFieldDefinitionsJF: OFormat[ApiFieldDefinitions]                         = Json.format[ApiFieldDefinitions]
  implicit val BulkApiFieldDefinitionsResponseJF: OFormat[BulkApiFieldDefinitionsResponse] = Json.format[BulkApiFieldDefinitionsResponse]

  implicit val SubscriptionFieldsJF: OFormat[SubscriptionFields]                         = Json.format[SubscriptionFields]
  implicit val BulkSubscriptionFieldsResponseJF: OFormat[BulkSubscriptionFieldsResponse] = Json.format[BulkSubscriptionFieldsResponse]
}

object JsonFormatters extends JsonFormatters
