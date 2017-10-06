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

package unit.apisubscriptionfields.model

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.apisubscriptionfields.model.{JsonFormatters, SubscriptionFieldsResponse}
import util.TestData

class JsonFormatterSpec extends WordSpec with Matchers with JsonFormatters with TestData {
  import play.api.libs.json._

  val fakeFields = Map( "f1" -> "v1" )
  val response = SubscriptionFieldsResponse(FakeFieldsId, fakeFields)

  private def objectAsJsonString[A](a:A)(implicit t: Writes[A]) = Json.asciiStringify(Json.toJson(a))

  "SubscriptionFieldsResponse" should {
    "marshal json" in {
      objectAsJsonString(response) shouldBe s"""{"fieldsId":"$FakeRawFieldsId","fields":{"f1":"v1"}}"""
    }

    "unmarshal text" in {
      Json.parse(s"""{"fieldsId":"$FakeRawFieldsId","fields":{"f1":"v1"}}""").validate[SubscriptionFieldsResponse] match {
        case JsSuccess(r, _) => r shouldBe response
        case JsError(e) => fail(s"Should have parsed json text but got $e")
      }
    }
  }
}
