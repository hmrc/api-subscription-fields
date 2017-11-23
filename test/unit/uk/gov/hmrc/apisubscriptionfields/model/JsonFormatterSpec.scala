/*
 * Copyright 2017 HM Revenue & Customs
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

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.apisubscriptionfields.model._
import util.{FieldsDefinitionTestData, SubscriptionFieldsTestData}

class JsonFormatterSpec extends WordSpec
  with Matchers
  with JsonFormatters
  with SubscriptionFieldsTestData
  with FieldsDefinitionTestData {

  import play.api.libs.json._

  private val fakeFields = Map( "f1" -> "v1" )
  private val subscriptionFieldsResponse = SubscriptionFieldsResponse(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeFieldsId, fakeFields)
  private val bulkSubscriptionFieldsResponse = BulkSubscriptionFieldsResponse(Seq(subscriptionFieldsResponse))

  private val fakeFieldsDefinitionResponse = FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, Seq(FakeFieldDefinitionUrl))
  private val bulkFieldsDefinitionResponse = BulkFieldsDefinitionsResponse(Seq(fakeFieldsDefinitionResponse))

  private def objectAsJsonString[A](a:A)(implicit t: Writes[A]) = Json.asciiStringify(Json.toJson(a))

  private val subscriptionFieldJson = s"""{"clientId":"$fakeRawClientId","apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldsId":"$FakeRawFieldsId","fields":{"f1":"v1"}}"""
  private val fieldDefinitionJson = s"""{"apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldDefinitions":[{"name":"name1","description":"desc1","type":"URL"}]}"""

  "SubscriptionFieldsResponse" should {

    "marshal json" in {
      objectAsJsonString(subscriptionFieldsResponse) shouldBe subscriptionFieldJson
    }

    "unmarshal text" in {
      Json.parse(subscriptionFieldJson).validate[SubscriptionFieldsResponse] match {
        case JsSuccess(r, _) => r shouldBe subscriptionFieldsResponse
        case JsError(e) => fail(s"Should have parsed json text but got $e")
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
        case JsError(e) => fail(s"Should have parsed json text but got $e")
      }
    }

  }

  "FieldsDefinitionResponse" should {

    "marshal json" in {
      objectAsJsonString(fakeFieldsDefinitionResponse) shouldBe fieldDefinitionJson
    }

    "unmarshal text" in {
      Json.parse(fieldDefinitionJson).validate[FieldsDefinitionResponse] match {
        case JsSuccess(r, _) => r shouldBe fakeFieldsDefinitionResponse
        case JsError(e) => fail(s"Should have parsed json text but got $e")
      }
    }

  }

  "BulkFieldsDefinitionsResponse" should {

    val json = s"""{"apis":[$fieldDefinitionJson]}"""

    "marshal json" in {
      objectAsJsonString(bulkFieldsDefinitionResponse) shouldBe json
    }

    "unmarshal text" in {
      Json.parse(json).validate[BulkFieldsDefinitionsResponse] match {
        case JsSuccess(r, _) => r shouldBe bulkFieldsDefinitionResponse
        case JsError(e) => fail(s"Should have parsed json text but got $e")
      }
    }

  }

}
