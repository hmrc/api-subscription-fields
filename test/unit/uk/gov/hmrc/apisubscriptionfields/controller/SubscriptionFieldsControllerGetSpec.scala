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

package unit.uk.gov.hmrc.apisubscriptionfields.controller

import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsDefined, JsString, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.apisubscriptionfields.controller.SubscriptionFieldsController
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

import scala.concurrent.Future

class SubscriptionFieldsControllerGetSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory with JsonFormatters {

  private val mockSubscriptionFieldsService = mock[SubscriptionFieldsService]
  private val controller = new SubscriptionFieldsController(mockSubscriptionFieldsService)

  private val responseJsonString =
    """{
      |  "id":"[application-id]___[api-context]___[api-version]",
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
      |  "fields": [
      |    {
      |      "id": "[application-id]___[api-context]___[api-version]",
      |      "fieldsId": "327d9145-4965-4d28-a2c5-39dedee50334",
      |      "fields": {
      |        "callback-id": "http://localhost",
      |        "token": "abc123"
      |      }
      |    },
      |    {
      |      "id": "[application-id]___[api-context]___[api-version2]",
      |      "fieldsId": "327d9145-4965-4d28-a2c5-39dedee50334",
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


  "GET /application/{application id}/context/{api-context}/version/{api-version}" should {

    "return OK when exists in the repo" in {
      (mockSubscriptionFieldsService.get(_:SubscriptionIdentifier)) expects FakeSubscriptionIdentifier returns Some(responseModel)

      val result = await(controller.getSubscriptionFields(fakeRawAppId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT_FOUND when not in the repo" in {
      (mockSubscriptionFieldsService.get(_: SubscriptionIdentifier)) expects FakeSubscriptionIdentifier returns None

      val result: Future[Result] = await(controller.getSubscriptionFields(fakeRawAppId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Id ($fakeRawAppId, $fakeRawContext, $fakeRawVersion) was not found"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      (mockSubscriptionFieldsService.get(_:SubscriptionIdentifier)) expects FakeSubscriptionIdentifier returns Future.failed(emulatedFailure)

      val result: Future[Result] = await(controller.getSubscriptionFields(fakeRawAppId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }

  }

  "GET /field/application/{application-id}" should {

    "return OK when exists in the repo" in {
      (mockSubscriptionFieldsService.get(_:AppId)) expects FakeAppId returns Some(bulkResponseModel)

      val result = await(controller.getBulkSubscriptionFieldsByApplicationId(fakeRawAppId)(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe bulkResponseJson
    }

    "return NOT_FOUND when not in the repo" in {
      (mockSubscriptionFieldsService.get(_:AppId)) expects FakeAppId returns None

      val result = await(controller.getBulkSubscriptionFieldsByApplicationId(fakeRawAppId)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"ApplicationId ($fakeRawAppId) was not found"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      (mockSubscriptionFieldsService.get(_:AppId)) expects FakeAppId returns Future.failed(emulatedFailure)

      val result = await(controller.getBulkSubscriptionFieldsByApplicationId(fakeRawAppId)(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }

  }

}
