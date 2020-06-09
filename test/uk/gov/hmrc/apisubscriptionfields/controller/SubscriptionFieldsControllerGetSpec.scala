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

import play.api.libs.json.{JsDefined, JsString, Json}
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.SubscriptionFieldsTestData
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.concurrent.Future.{successful,failed}
import play.api.test.StubControllerComponentsFactory
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec
import play.api.test.FakeRequest

class SubscriptionFieldsControllerGetSpec extends AsyncHmrcSpec with SubscriptionFieldsTestData with JsonFormatters with StubControllerComponentsFactory {

  private val mockSubscriptionFieldsService = mock[SubscriptionFieldsService]
  private val controller = new SubscriptionFieldsController(stubControllerComponents(), mockSubscriptionFieldsService)

  private val responseJsonString =
    """{
      |  "clientId": "b624ef7f-8fb5-4ae1-add5-168ebc9fdfcc",
      |  "apiContext": "ciao-api",
      |  "apiVersion": "1.0",
      |  "fieldsId":"327d9145-4965-4d28-a2c5-39dedee50334",
      |  "fields":{
      |    "callbackId":"http://localhost",
      |    "token":"abc123"
      |  }
      |}""".stripMargin
  private val responseJson = Json.parse(responseJsonString)
  private val responseModel = responseJson.as[SubscriptionFields]

  private val bulkResponseJsonString =
    """{
      |  "subscriptions": [
      |    {
      |      "clientId": "b624ef7f-8fb5-4ae1-add5-168ebc9fdfcc",
      |      "apiContext": "ciao-api",
      |      "apiVersion": "1.0",
      |      "fieldsId": "327d9145-4965-4d28-a2c5-39dedee50334",
      |      "fields": {
      |        "callbackId": "http://localhost",
      |        "token": "abc123"
      |      }
      |    },
      |    {
      |      "clientId": "b624ef7f-8fb5-4ae1-add5-168ebc9fdfcc",
      |      "apiContext": "ciao-api",
      |      "apiVersion": "2.0",
      |      "fieldsId": "327d9145-4965-4d28-a2c5-39dedee50335",
      |      "fields": {
      |        "callbackId": "https://application.sage.com/return-route",
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
      when(mockSubscriptionFieldsService.get(FakeClientId, FakeContext, FakeVersion)).thenReturn(successful(Some(responseModel)))

      val result = controller.getSubscriptionFields(FakeClientId, FakeContext, FakeVersion)(FakeRequest())

      status(result) shouldBe OK
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT_FOUND when not in the repo" in {
      when(mockSubscriptionFieldsService.get(FakeClientId, FakeContext, FakeVersion)).thenReturn(successful(None))

      val result: Future[Result] = controller.getSubscriptionFields(FakeClientId, FakeContext, FakeVersion)(FakeRequest())

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Subscription fields not found for (${FakeClientId.value}, ${FakeContext.value}, ${FakeVersion.value})"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      when(mockSubscriptionFieldsService.get(FakeClientId, FakeContext, FakeVersion)).thenReturn(failed(emulatedFailure))

      val result: Future[Result] = controller.getSubscriptionFields(FakeClientId, FakeContext, FakeVersion)(FakeRequest())

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }

  }

  "GET /field/{fields-id}" should {

    "return OK when the expected record exists in the repo" in {
      when(mockSubscriptionFieldsService.getBySubscriptionFieldId(FakeFieldsId)).thenReturn(successful(Some(responseModel)))

      val result = controller.getSubscriptionFieldsByFieldsId(FakeFieldsId)(FakeRequest())

      status(result) shouldBe OK
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT_FOUND when not in the repo" in {
      when(mockSubscriptionFieldsService.getBySubscriptionFieldId(FakeFieldsId)).thenReturn(successful(None))

      val result: Future[Result] = controller.getSubscriptionFieldsByFieldsId(FakeFieldsId)(FakeRequest())

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"FieldsId ($FakeRawFieldsId) was not found"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      when(mockSubscriptionFieldsService.getBySubscriptionFieldId(FakeFieldsId)).thenReturn(failed(emulatedFailure))

      val result: Future[Result] = controller.getSubscriptionFieldsByFieldsId(FakeFieldsId)(FakeRequest())

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }
  }

  "GET /field/application/{client-id}" should {

    "return OK when the expected record exists in the repo" in {
      when(mockSubscriptionFieldsService.getByClientId(FakeClientId)).thenReturn(successful(Some(bulkResponseModel)))

      val result = controller.getBulkSubscriptionFieldsByClientId(FakeClientId)(FakeRequest())

      status(result) shouldBe OK
      contentAsJson(result) shouldBe bulkResponseJson
    }

    "return NOT_FOUND when not in the repo" in {
      when(mockSubscriptionFieldsService.getByClientId(FakeClientId)).thenReturn(successful(None))

      val result = controller.getBulkSubscriptionFieldsByClientId(FakeClientId)(FakeRequest())

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"ClientId (${FakeClientId.value}) was not found"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      when(mockSubscriptionFieldsService.getByClientId(FakeClientId)).thenReturn(failed(emulatedFailure))

      val result = controller.getBulkSubscriptionFieldsByClientId(FakeClientId)(FakeRequest())

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }

  }

  "GET /field" should {
    "return OK with all field subscriptions" in {
      when(mockSubscriptionFieldsService.getAll).thenReturn(successful(bulkResponseModel))

      val result = controller.getAllSubscriptionFields(FakeRequest())

      status(result) shouldBe OK
      contentAsJson(result) shouldBe bulkResponseJson
    }

    "return OK with an empty list when no field subscriptions exist in the repo" in {
      when(mockSubscriptionFieldsService.getAll).thenReturn(successful(BulkSubscriptionFieldsResponse(subscriptions = Seq())))

      val result = controller.getAllSubscriptionFields(FakeRequest())

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(BulkSubscriptionFieldsResponse(Seq()))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      when(mockSubscriptionFieldsService.getAll).thenReturn(failed(emulatedFailure))

      val result = controller.getAllSubscriptionFields(FakeRequest())

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }
  }

}
