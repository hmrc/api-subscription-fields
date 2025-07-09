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

import cats.data.NonEmptyList
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import uk.gov.hmrc.apiplatform.modules.subscriptionfields.domain.models.FieldValue

import uk.gov.hmrc.apisubscriptionfields.{FieldDefinitionTestData, SubscriptionFieldsTestData}

class JsonFormatterSpec extends AnyWordSpec with Matchers with JsonFormatters with SubscriptionFieldsTestData with FieldDefinitionTestData {

  import play.api.libs.json._

  private val fakeFields                     = Map(fieldN(1) -> FieldValue("v1"))
  private val subscriptionFields             = SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fakeFields)
  private val bulkSubscriptionFieldsResponse = BulkSubscriptionFieldsResponse(Seq(subscriptionFields))

  private val fakeApiFieldDefinitionsResponse                = ApiFieldDefinitions(FakeContext, FakeVersion, NonEmptyList.one(FakeFieldDefinitionUrl))
  private val fakeApiFieldDefinitionsResponseEmptyValidation = ApiFieldDefinitions(FakeContext, FakeVersion, NonEmptyList.one(FakeFieldDefinitionUrlValidationEmpty))
  private val bulkFieldsDefinitionResponse                   = BulkApiFieldDefinitionsResponse(Seq(fakeApiFieldDefinitionsResponse))

  private def objectAsJsonString[A](a: A)(implicit t: Writes[A]) = Json.asciiStringify(Json.toJson(a))

  private val subscriptionFieldJson =
    s"""{"clientId":"$fakeRawClientId","apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldsId":"$FakeRawFieldsId","fields":{"fieldB":"v1"}}"""

  private val fieldDefinitionJson =
    s"""{"apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldDefinitions":[{"name":"fieldB","description":"desc1","hint":"hint1","type":"URL","shortDescription":"short description","validation":{"errorMessage":"error message","rules":[{"UrlValidationRule":{}}]}}]}"""

  private val fieldDefinitionEmptyValidationJson =
    s"""{"apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldDefinitions":[{"name":"fieldB","description":"desc1","hint":"hint1","type":"URL","shortDescription":"short description"}]}"""

  "SubscriptionFields" should {
    "marshal json" in {
      objectAsJsonString(subscriptionFields) shouldBe subscriptionFieldJson
    }

    "unmarshal text" in {
      Json.parse(subscriptionFieldJson).validate[SubscriptionFields] match {
        case JsSuccess(r, _) => r shouldBe subscriptionFields
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }
  }

  "ApiFieldDefinitions" should {
    "marshal json" in {
      objectAsJsonString(fakeApiFieldDefinitionsResponse) shouldBe fieldDefinitionJson
    }

    "marshal json when ValidationGroup is empty" in {
      objectAsJsonString(fakeApiFieldDefinitionsResponseEmptyValidation) shouldBe fieldDefinitionEmptyValidationJson
    }

    "unmarshal text" in {
      Json.parse(fieldDefinitionJson).validate[ApiFieldDefinitions] match {
        case JsSuccess(r, _) => r shouldBe fakeApiFieldDefinitionsResponse
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }

    "unmarshal text  when ValidationGroup is empty" in {
      Json.parse(fieldDefinitionEmptyValidationJson).validate[ApiFieldDefinitions] match {
        case JsSuccess(r, _) => r shouldBe fakeApiFieldDefinitionsResponseEmptyValidation
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }
  }

  "BulkSubscriptionFieldsResponse" should {
    val json = s"""{"subscriptions":[$subscriptionFieldJson]}"""

    "marshal json" in {
      objectAsJsonString(bulkSubscriptionFieldsResponse) shouldBe json
    }

    "unmarshal text" in {
      Json.parse(json).validate[BulkSubscriptionFieldsResponse] match {
        case JsSuccess(r, _) => r shouldBe bulkSubscriptionFieldsResponse
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }
  }

  "BulkApiFieldDefinitionsResponse" should {
    val json = s"""{"apis":[$fieldDefinitionJson]}"""

    "marshal json" in {
      objectAsJsonString(bulkFieldsDefinitionResponse) shouldBe json
    }

    "unmarshal text" in {
      Json.parse(json).validate[BulkApiFieldDefinitionsResponse] match {
        case JsSuccess(r, _) => r shouldBe bulkFieldsDefinitionResponse
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }
  }
}
