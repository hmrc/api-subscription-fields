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
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.apisubscriptionfields.controller.FieldsDefinitionController
import uk.gov.hmrc.apisubscriptionfields.model.{FieldsDefinitionResponse, JsonFormatters}
import uk.gov.hmrc.apisubscriptionfields.service.FieldsDefinitionService
import uk.gov.hmrc.play.test.UnitSpec
import util.FieldsDefinitionTestData

import scala.concurrent.Future

class FieldsDefinitionControllerGetSpec extends UnitSpec with FieldsDefinitionTestData with MockFactory with JsonFormatters {

  private val mockFieldsDefinitionService = mock[FieldsDefinitionService]
  private val controller = new FieldsDefinitionController(mockFieldsDefinitionService)

  private val responseJsonString =
    """{ "fields": [
    |          {
    |            "name": "callback-url",
    |            "description": "Callback URL",
    |            "type": "URL"
    |          },
    |          {
    |            "name": "token",
    |            "description": "Secure Token",
    |            "type": "SecureToken"
    |          }
    |        ]
    |}""".stripMargin
  private val responseJson = Json.parse(responseJsonString)
  private val responseModel = responseJson.as[FieldsDefinitionResponse]

  "GET /definition/context/:apiContext/version/:apiVersion" should {
    "return OK when exists in the repo" in {
      mockFieldsDefinitionService.get _ expects FakeFieldsDefinitionIdentifier returns Future.successful(Some(responseModel))

      val result = await(controller.getFieldsDefinition(fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT_FOUND when entity does not exists in the repo" in {
      mockFieldsDefinitionService.get _ expects FakeFieldsDefinitionIdentifier returns Future.successful(None)

      val result = await(controller.getFieldsDefinition(fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Id ($fakeRawContext, $fakeRawVersion) was not found"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      mockFieldsDefinitionService.get _ expects FakeFieldsDefinitionIdentifier returns Future.failed(emulatedFailure)

      val result = await(controller.getFieldsDefinition(fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }
  }

}
