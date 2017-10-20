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

package unit.uk.gov.hmrc.apisubscriptionfields.model

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.apisubscriptionfields.model.{BulkSubscriptionFieldsResponse, FieldsDefinitionResponse, JsonFormatters, SubscriptionFieldsResponse}
import util.{FieldsDefinitionTestData, SubscriptionFieldsTestData}

class JsonFormatterSpec extends WordSpec with Matchers with JsonFormatters with SubscriptionFieldsTestData with FieldsDefinitionTestData {
  import play.api.libs.json._

  private val fakeFields = Map( "f1" -> "v1" )
  private val subscriptionFieldsResponse = SubscriptionFieldsResponse(FakeRawIdentifier, FakeFieldsId, fakeFields)

  private val bulkSubscriptionFieldsResponse = BulkSubscriptionFieldsResponse(Seq(subscriptionFieldsResponse))

  private val fakeFieldsDefinitionResponse = FieldsDefinitionResponse(Seq(FakeFieldDefinitionUrl))

  private def objectAsJsonString[A](a:A)(implicit t: Writes[A]) = Json.asciiStringify(Json.toJson(a))

  "SubscriptionFieldsResponse" should {
    val json = s"""{"id":"$FakeRawIdentifier","fieldsId":"$FakeRawFieldsId","fields":{"f1":"v1"}}"""

    "marshal json" in {
      objectAsJsonString(subscriptionFieldsResponse) shouldBe json
    }

    "unmarshal text" in {
      Json.parse(json).validate[SubscriptionFieldsResponse] match {
        case JsSuccess(r, _) => r shouldBe subscriptionFieldsResponse
        case JsError(e) => fail(s"Should have parsed json text but got $e")
      }
    }
  }

  "BulkSubscriptionFieldsResponse" should {
    val json = s"""{"fields":[{"id":"$FakeRawIdentifier","fieldsId":"$FakeRawFieldsId","fields":{"f1":"v1"}}]}"""

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
    val json = """{"fields":[{"name":"name1","description":"desc1","type":"URL"}]}"""

    "marshal json" in {
      objectAsJsonString(fakeFieldsDefinitionResponse) shouldBe json
    }

    "unmarshal text" in {
      Json.parse(json).validate[FieldsDefinitionResponse] match {
        case JsSuccess(r, _) => r shouldBe fakeFieldsDefinitionResponse
        case JsError(e) => fail(s"Should have parsed json text but got $e")
      }
    }
  }

}
