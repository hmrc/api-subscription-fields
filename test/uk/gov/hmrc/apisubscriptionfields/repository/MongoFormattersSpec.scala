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

package uk.gov.hmrc.apisubscriptionfields.repository

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.apisubscriptionfields.model.{FieldDefinition, FieldDefinitionType, JsonFormatters}
import uk.gov.hmrc.play.test.UnitSpec

class MongoFormattersSpec extends UnitSpec with JsonFormatters {
  "Field definition formatter" should {
    "Correctly unmarshall a JSON field definition with all the necessary fields" in {
      val fieldDefinition = FieldDefinition("name", "description", "hint", FieldDefinitionType.STRING, "short description")
      Json.fromJson[FieldDefinition](Json.parse("""{ "name" : "name", "description" : "description", "hint": "hint", "type" : "STRING", "shortDescription" : "short description"}""")) shouldBe JsSuccess(fieldDefinition)
    }

    "Correctly unmarshall a JSON field definition without the hint field" in {
      val fieldDefinition = FieldDefinition("name", "description", "", FieldDefinitionType.STRING, "short description")
      Json.fromJson[FieldDefinition](Json.parse("""{ "name" : "name", "description" : "description", "type" : "STRING", "shortDescription" : "short description"}""")) shouldBe JsSuccess(fieldDefinition)
    }

    "Correctly unmarshall a JSON field definition without the shortDescription field" in {
      val fieldDefinition = FieldDefinition("name", "description", "hint", FieldDefinitionType.STRING, "")
      Json.fromJson[FieldDefinition](Json.parse("""{ "name" : "name", "description" : "description", "hint": "hint", "type" : "STRING"}""")) shouldBe JsSuccess(fieldDefinition)
    }

  }
}
