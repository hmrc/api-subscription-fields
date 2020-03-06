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

package uk.gov.hmrc.apisubscriptionfields.controller

import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsDefined, JsString, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.apisubscriptionfields.SubscriptionFieldsTestData
import uk.gov.hmrc.apisubscriptionfields.model.{ApiContext, ApiVersion, BulkSubscriptionFieldsResponse, ClientId, JsonFormatters, SubscriptionFieldsId, SubscriptionFieldsResponse}
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SubscriptionFieldsControllerGetSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory with JsonFormatters with StubControllerComponentsFactory {

  private val mockSubscriptionFieldsService = mock[SubscriptionFieldsService]
  private val controller = new SubscriptionFieldsController(stubControllerComponents(), mockSubscriptionFieldsService)

  private val responseJsonString =
    """{
      |  "clientId": "afsdknbw34ty4hebdv",
      |  "apiContext": "ciao-api",
      |  "apiVersion": "1.0",
      |  "fieldsId":"327d9145-4965-4d28-a2c5-39dedee50334",
      |  "fields":{
      |    "callback-id":"http://localhost",
      |    "token":"abc123"
      |  }
      |}""".stripMargin
  private val responseJson = Json.parse(responseJsonString)
  private val responseModel = responseJson.as[SubscriptionFieldsResponse]

  private val bulkResponseJsonString =
    """{
      |  "subscriptions": [
      |    {
      |      "clientId": "afsdknbw34ty4hebdv",
      |      "apiContext": "ciao-api",
      |      "apiVersion": "1.0",
      |      "fieldsId": "327d9145-4965-4d28-a2c5-39dedee50334",
      |      "fields": {
      |        "callback-id": "http://localhost",
      |        "token": "abc123"
      |      }
      |    },
      |    {
      |      "clientId": "afsdknbw34ty4hebdv",
      |      "apiContext": "ciao-api",
      |      "apiVersion": "2.0",
      |      "fieldsId": "327d9145-4965-4d28-a2c5-39dedee50335",
      |      "fields": {
      |        "callback-id": "https://application.sage.com/return-route",
      |        "token": "zyx456"
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin
  private val bulkResponseJson = Json.parse(bulkResponseJsonString)
  private val bulkResponseModel = bulkResponseJson.as[BulkSubscriptionFieldsResponse]

  "GET /field/application/{client-id}/context/{context}/version/{api-version}" should {

    "return OK when the expected record exists in the repo" in {
      (mockSubscriptionFieldsService.get(_:ClientId, _:ApiContext, _:ApiVersion)) expects(FakeClientId, FakeContext, FakeVersion) returns Some(responseModel)

      val result = await(controller.getSubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT_FOUND when not in the repo" in {
      (mockSubscriptionFieldsService.get(_:ClientId, _:ApiContext, _:ApiVersion)) expects(FakeClientId, FakeContext, FakeVersion) returns None

      val result: Future[Result] = await(controller.getSubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Subscription fields not found for ($fakeRawClientId, $fakeRawContext, $fakeRawVersion)"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      (mockSubscriptionFieldsService.get(_:ClientId, _:ApiContext, _:ApiVersion)) expects(FakeClientId, FakeContext, FakeVersion) returns Future.failed(emulatedFailure)

      val result: Future[Result] = await(controller.getSubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }

  }

  "GET /field/{fields-id}" should {

    "return OK when the expected record exists in the repo" in {
      (mockSubscriptionFieldsService.get(_:SubscriptionFieldsId)) expects FakeFieldsId returns Some(responseModel)

      val result = await(controller.getSubscriptionFieldsByFieldsId(FakeRawFieldsId)(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT_FOUND when not in the repo" in {
      (mockSubscriptionFieldsService.get(_:SubscriptionFieldsId)) expects FakeFieldsId returns None

      val result: Future[Result] = await(controller.getSubscriptionFieldsByFieldsId(FakeRawFieldsId)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"FieldsId ($FakeRawFieldsId) was not found"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      (mockSubscriptionFieldsService.get(_:SubscriptionFieldsId)) expects FakeFieldsId returns Future.failed(emulatedFailure)

      val result: Future[Result] = await(controller.getSubscriptionFieldsByFieldsId(FakeRawFieldsId)(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }
  }

  "GET /field/application/{client-id}" should {

    "return OK when the expected record exists in the repo" in {
      (mockSubscriptionFieldsService.get(_:ClientId)) expects FakeClientId returns Some(bulkResponseModel)

      val result = await(controller.getBulkSubscriptionFieldsByClientId(fakeRawClientId)(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe bulkResponseJson
    }

    "return NOT_FOUND when not in the repo" in {
      (mockSubscriptionFieldsService.get(_:ClientId)) expects FakeClientId returns None

      val result = await(controller.getBulkSubscriptionFieldsByClientId(fakeRawClientId)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"ClientId ($fakeRawClientId) was not found"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      (mockSubscriptionFieldsService.get(_:ClientId)) expects FakeClientId returns Future.failed(emulatedFailure)

      val result = await(controller.getBulkSubscriptionFieldsByClientId(fakeRawClientId)(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }

  }

  "GET /field" should {
    "return OK with all field subscriptions" in {
      mockSubscriptionFieldsService.getAll _ expects () returns bulkResponseModel

      val result = await(controller.getAllSubscriptionFields(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe bulkResponseJson
    }

    "return OK with an empty list when no field subscriptions exist in the repo" in {
      mockSubscriptionFieldsService.getAll _ expects () returns BulkSubscriptionFieldsResponse(subscriptions = Seq())

      val result = await(controller.getAllSubscriptionFields(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(BulkSubscriptionFieldsResponse(Seq()))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      mockSubscriptionFieldsService.getAll _ expects () returns Future.failed(emulatedFailure)

      val result = await(controller.getAllSubscriptionFields(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }
  }

}
