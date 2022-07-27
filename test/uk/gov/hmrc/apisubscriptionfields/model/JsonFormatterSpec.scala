/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinitionType._

class JsonFormatterSpec extends WordSpec with Matchers with JsonFormatters with SubscriptionFieldsTestData with FieldDefinitionTestData {

  import play.api.libs.json._

  private val fakeFields = Map(fieldN(1) -> "v1")
  private val subscriptionFields = SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fakeFields)
  private val bulkSubscriptionFieldsResponse = BulkSubscriptionFieldsResponse(Seq(subscriptionFields))

  private val fakeApiFieldDefinitionsResponse = ApiFieldDefinitions(FakeContext, FakeVersion, NonEmptyList.one(FakeFieldDefinitionUrl))
  private val fakeApiFieldDefinitionsResponseEmptyValidation = ApiFieldDefinitions(FakeContext, FakeVersion, NonEmptyList.one(FakeFieldDefinitionUrlValidationEmpty))
  private val bulkFieldsDefinitionResponse = BulkApiFieldDefinitionsResponse(Seq(fakeApiFieldDefinitionsResponse))

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

  "DevhubAccessRequirements" should {
    import DevhubAccessRequirement._

    "marshall a default correctly" in {
      val rq = DevhubAccessRequirements.Default

      Json.stringify(Json.toJson(rq)) shouldBe "{}"
    }

    "marshall a read option" in {
      val rq = DevhubAccessRequirements(read = AdminOnly)

      Json.stringify(Json.toJson(rq)) shouldBe """{"read":"adminOnly","write":"adminOnly"}"""
    }

    "marshall a write option" in {
      val rq = DevhubAccessRequirements(read = DevhubAccessRequirement.Default, write = NoOne)

      Json.stringify(Json.toJson(rq)) shouldBe """{"write":"noOne"}"""
    }

    "marshall a complete option" in {
      val rq = DevhubAccessRequirements(read = AdminOnly, write = NoOne)

      Json.stringify(Json.toJson(rq)) shouldBe """{"read":"adminOnly","write":"noOne"}"""
    }

    "unmarshall a default correctly" in {
      Json.fromJson[DevhubAccessRequirements](Json.parse("{}")) shouldBe JsSuccess(DevhubAccessRequirements.Default)
    }

    "unmarshall a read correctly" in {
      Json.fromJson[DevhubAccessRequirements](Json.parse("""{"read":"adminOnly"}""")) shouldBe JsSuccess(DevhubAccessRequirements(read = AdminOnly, write = AdminOnly))
    }

    "unmarshall a write correctly" in {
      Json.fromJson[DevhubAccessRequirements](Json.parse("""{"write":"noOne"}""")) shouldBe JsSuccess(DevhubAccessRequirements(read = Anyone, write = NoOne))
    }

    "unmarshall a complete option correctly" in {
      Json.fromJson[DevhubAccessRequirements](Json.parse("""{"read":"adminOnly","write":"noOne"}""")) shouldBe JsSuccess(DevhubAccessRequirements(read = AdminOnly, write = NoOne))
    }
  }

  "AccessRequirements" should {
    import DevhubAccessRequirement._

    "marshalling a default correctly" in {
      val rq = AccessRequirements.Default

      Json.stringify(Json.toJson(rq)) shouldBe """{"devhub":{}}"""
    }

    "marshalling with some devhub requirements correctly" in {
      // read is set explicity, but write will be given this greater restriction too.
      val rq = AccessRequirements(devhub = DevhubAccessRequirements.apply(read = AdminOnly))

      Json.stringify(Json.toJson(rq)) shouldBe """{"devhub":{"read":"adminOnly","write":"adminOnly"}}"""
    }

    "unmarshall with default correctly" in {
      Json.fromJson[AccessRequirements](Json.parse("""{"devhub":{}}""")) shouldBe JsSuccess(AccessRequirements.Default)
    }

    "unmarshall with non default correctly" in {
      Json.fromJson[AccessRequirements](Json.parse("""{"devhub":{"read":"adminOnly"}}""")) shouldBe JsSuccess(AccessRequirements(devhub = DevhubAccessRequirements(read = AdminOnly)))
    }
  }

  "FieldDefinition" should {
    "marshal json with non default access" in {
      objectAsJsonString(FakeFieldDefinitionWithAccess) should include(""","access":{"devhub":{"read":"adminOnly","write":"adminOnly"}}""")
    }

    "marshal json without mention of default access" in {
      objectAsJsonString(FakeFieldDefinitionWithAccess.copy(access = AccessRequirements.Default)) should not include(""""access":{"devhub":{"read":"adminOnly", "write":"adminOnly"}}""")
      objectAsJsonString(FakeFieldDefinitionWithAccess.copy(access = AccessRequirements.Default)) should not include(""""access"""")
    }

    "unmarshal json string into a FieldDefinition" in {
      val json = """{"name":"fieldB","description":"desc1","hint":"hint1","type":"PPNSField","shortDescription":"short description"}"""

      Json.fromJson[FieldDefinition](Json.parse(json)) match {
        case JsSuccess(r, _) => r.`type` shouldBe PPNS_FIELD
        case JsError(e)      => fail(s"Should have parsed json text but got $e")
      }
    }
  }
}
