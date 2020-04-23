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

import cats.data.NonEmptyList
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.apisubscriptionfields.{FieldDefinitionTestData, SubscriptionFieldsTestData}
import uk.gov.hmrc.apisubscriptionfields.model.DevhubAccessLevel.{Admininstator, Developer}

class JsonFormatterSpec extends WordSpec with Matchers with JsonFormatters with SubscriptionFieldsTestData with FieldDefinitionTestData {

  import play.api.libs.json._

  private val fakeFields = Map(fieldN(1) -> "v1")
  private val subscriptionFieldsResponse = SubscriptionFieldsResponse(fakeRawClientId, fakeRawContext, fakeRawVersion, FakeFieldsId, fakeFields)
  private val bulkSubscriptionFieldsResponse = BulkSubscriptionFieldsResponse(Seq(subscriptionFieldsResponse))

  private val fakeApiFieldDefinitionsResponse = ApiFieldDefinitionsResponse(fakeRawContext, fakeRawVersion, NonEmptyList.one(FakeFieldDefinitionUrl))
  private val fakeApiFieldDefinitionsResponseEmptyValidation = ApiFieldDefinitionsResponse(fakeRawContext, fakeRawVersion, NonEmptyList.one(FakeFieldDefinitionUrlValidationEmpty))
  private val bulkFieldsDefinitionResponse = BulkApiFieldDefinitionsResponse(Seq(fakeApiFieldDefinitionsResponse))

  private def objectAsJsonString[A](a: A)(implicit t: Writes[A]) = Json.asciiStringify(Json.toJson(a))

  private val subscriptionFieldJson =
    s"""{"clientId":"$fakeRawClientId","apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldsId":"$FakeRawFieldsId","fields":{"fieldB":"v1"}}"""
  private val fieldDefinitionJson =
    s"""{"apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldDefinitions":[{"name":"fieldB","description":"desc1","hint":"hint1","type":"URL","shortDescription":"short description","validation":{"errorMessage":"error message","rules":[{"UrlValidationRule":{}}]}}]}"""
  private val fieldDefinitionEmptyValidationJson =
    s"""{"apiContext":"$fakeRawContext","apiVersion":"$fakeRawVersion","fieldDefinitions":[{"name":"fieldB","description":"desc1","hint":"hint1","type":"URL","shortDescription":"short description"}]}"""

  "SubscriptionFieldsResponse" should {
    "marshal json" in {
      objectAsJsonString(subscriptionFieldsResponse) shouldBe subscriptionFieldJson
    }

    "unmarshal text" in {
      Json.parse(subscriptionFieldJson).validate[SubscriptionFieldsResponse] match {
        case JsSuccess(r, _) => r shouldBe subscriptionFieldsResponse
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }
  }

  "ApiFieldDefinitionsResponse" should {
    "marshal json" in {
      objectAsJsonString(fakeApiFieldDefinitionsResponse) shouldBe fieldDefinitionJson
    }

    "marshal json when ValidationGroup is empty" in {
      objectAsJsonString(fakeApiFieldDefinitionsResponseEmptyValidation) shouldBe fieldDefinitionEmptyValidationJson
    }

    "unmarshal text" in {
      Json.parse(fieldDefinitionJson).validate[ApiFieldDefinitionsResponse] match {
        case JsSuccess(r, _) => r shouldBe fakeApiFieldDefinitionsResponse
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }

    "unmarshal text  when ValidationGroup is empty" in {
      Json.parse(fieldDefinitionEmptyValidationJson).validate[ApiFieldDefinitionsResponse] match {
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

  "DevhubLevelRequirements" should {
    "marshall a default correctly" in {
      val rq = DevhubAccessLevelRequirements()

      Json.stringify(Json.toJson(rq)) shouldBe "{}"
    }

    "marshall a readOnly option" in {
      val rq = DevhubAccessLevelRequirements(readOnly = DevhubAccessLevel.Admininstator)

      Json.stringify(Json.toJson(rq)) shouldBe """{"readOnly":"administrator"}"""
    }

    "marshall a readWrite option" in {
      val rq = DevhubAccessLevelRequirements(readWrite = DevhubAccessLevelRequirement.NoOne)

      Json.stringify(Json.toJson(rq)) shouldBe """{"readWrite":"noone"}"""
    }

    "marshall a complete option" in {
      val rq = DevhubAccessLevelRequirements(readOnly = DevhubAccessLevel.Admininstator, readWrite = DevhubAccessLevelRequirement.NoOne)

      Json.stringify(Json.toJson(rq)) shouldBe """{"readOnly":"administrator","readWrite":"noone"}"""
    }

    "unmarshall a default correctly" in {
      Json.fromJson[DevhubAccessLevelRequirements](Json.parse("{}")) shouldBe JsSuccess(DevhubAccessLevelRequirements.Default)
    }

    "unmarshall a readOnly correctly" in {
      Json.fromJson[DevhubAccessLevelRequirements](Json.parse("""{"readOnly":"administrator"}""")) shouldBe JsSuccess(DevhubAccessLevelRequirements(readOnly = Admininstator, readWrite = Developer))
    }

    "unmarshall a readWrite correctly" in {
      Json.fromJson[DevhubAccessLevelRequirements](Json.parse("""{"readWrite":"noone"}""")) shouldBe JsSuccess(DevhubAccessLevelRequirements(readOnly = Developer, readWrite = DevhubAccessLevelRequirement.NoOne))
    }

    "unmarshall a complete option correctly" in {
      Json.fromJson[DevhubAccessLevelRequirements](Json.parse("""{"readOnly":"administrator","readWrite":"noone"}""")) shouldBe JsSuccess(DevhubAccessLevelRequirements(readOnly = Admininstator, readWrite = DevhubAccessLevelRequirement.NoOne))
    }
  }

  "AccessLevelRequirements" should {
    "marshalling a default correctly" in {
      val rq = AccessLevelRequirements.Default

      Json.stringify(Json.toJson(rq)) shouldBe """{"devhub":{}}"""
    }

    "marshalling with some devhub requirements correctly" in {
      val rq = AccessLevelRequirements(devhub = DevhubAccessLevelRequirements(readOnly = Admininstator))

      Json.stringify(Json.toJson(rq)) shouldBe """{"devhub":{"readOnly":"administrator"}}"""
    }

    "unmarshall with default correctly" in {
      Json.fromJson[AccessLevelRequirements](Json.parse("""{"devhub":{}}""")) shouldBe JsSuccess(AccessLevelRequirements.Default)
    }

    "unmarshall with non default correctly" in {
      Json.fromJson[AccessLevelRequirements](Json.parse("""{"devhub":{"readOnly":"administrator"}}""")) shouldBe JsSuccess(AccessLevelRequirements(devhub = DevhubAccessLevelRequirements(readOnly = Admininstator)))
    }
  }

  "FieldDefinition" should {
    "marshal json with non default access" in {
      objectAsJsonString(FakeFieldDefinitionWithAccess) should include(""","access":{"devhub":{"readOnly":"administrator"}}""")
    }

    "marshal json without mention of default access" in {
      objectAsJsonString(FakeFieldDefinitionWithAccess.copy(access = AccessLevelRequirements.Default)) should not include(""""access":{"devhub":{"readOnly":"administrator"}}""")
      objectAsJsonString(FakeFieldDefinitionWithAccess.copy(access = AccessLevelRequirements.Default)) should not include(""""access"""")
    }
  }
}
