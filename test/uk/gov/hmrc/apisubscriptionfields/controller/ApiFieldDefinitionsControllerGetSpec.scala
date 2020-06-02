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
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.apisubscriptionfields.FieldDefinitionTestData
import uk.gov.hmrc.apisubscriptionfields.model.{BulkApiFieldDefinitionsResponse, ApiFieldDefinitionsResponse, JsonFormatters}
import uk.gov.hmrc.apisubscriptionfields.service.ApiFieldDefinitionsService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future.{successful,failed}
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.apisubscriptionfields.model.ApiFieldDefinitionsResponse
import org.scalatest.mockito.MockitoSugar

import org.mockito.Mockito.when

class ApiFieldDefinitionsControllerGetSpec extends UnitSpec with FieldDefinitionTestData with MockitoSugar with JsonFormatters with StubControllerComponentsFactory {

  private val mockFieldsDefinitionService = mock[ApiFieldDefinitionsService]
  private val controller = new ApiFieldDefinitionsController(stubControllerComponents(), mockFieldsDefinitionService)

  private val responseJsonString =
    """{
      |  "apiContext": "hello",
      |  "apiVersion": "1.0",
      |  "fieldDefinitions": [
      |    {
      |      "name": "callbackUrl",
      |      "description": "Callback URL",
      |      "hint": "Description Hint",
      |      "type": "URL",
      |      "shortDescription": "short desc"
      |    },
      |    {
      |      "name": "token",
      |      "description": "Secure Token",
      |      "hint": "Description Hint",
      |      "type": "SecureToken",
      |      "shortDescription": "",
      |      "validation": {
      |        "errorMessage": "error message",
      |        "rules": [
      |          {
      |            "RegexValidationRule": {
      |                 "regex": "test regex"
      |            }
      |           },
      |          {
      |            "RegexValidationRule": {
      |            "regex": "test regex"
      |            }
      |          }
      |        ]
      |      }
      |    }
      |  ]
      |}""".stripMargin
  private val responseJson = Json.parse(responseJsonString)
  private val responseModel = responseJson.as[ApiFieldDefinitionsResponse]

  private val allResponseJsonString =
    """{
      |  "apis": [
      |    {
      |      "apiContext": "hello",
      |      "apiVersion": "1.0",
      |      "fieldDefinitions": [
      |        {
      |          "name": "callbackUrl",
      |          "description": "Callback URL",
      |          "hint": "Description Hint",
      |          "type": "URL",
      |          "shortDescription": "short desc",
      |          "validation": {
      |             "errorMessage": "",
      |             "rules": [
      |               {
      |                 "RegexValidationRule": {
      |                 "regex": "test regex"
      |               }
      |             }
      |           ]
      |           }
      |        },
      |        {
      |          "name": "token",
      |          "description": "Secure Token",
      |          "hint": "Description Hint",
      |          "type": "SecureToken",
      |          "shortDescription": "",
      |          "validation": {
      |             "errorMessage": "",
      |             "rules": [
      |                {
      |                 "RegexValidationRule": {
      |                 "regex": "test regex"
      |               }
      |              }
      |             ]
      |           }
      |        }
      |      ]
      |    },
      |    {
      |      "apiContext": "ciao",
      |      "apiVersion": "2.0",
      |      "fieldDefinitions": [
      |        {
      |          "name": "address",
      |          "description": "where you live",
      |          "hint": "Description Hint",
      |          "type": "STRING",
      |          "shortDescription": "",
      |           "validation": {
      |             "errorMessage": "",
      |             "rules": [
      |                {
      |                 "RegexValidationRule": {
      |                 "regex": "test regex"
      |               }
      |              }
      |             ]
      |           }
      |        },
      |        {
      |          "name": "number",
      |          "description": "telephone number",
      |          "hint": "Description Hint",
      |          "type": "STRING",
      |          "shortDescription": ""
      |        }
      |      ]
      |    }
      |  ]
      |}""".stripMargin
  private val allResponseJson = Json.parse(allResponseJsonString)
  private val allResponseModel = allResponseJson.as[BulkApiFieldDefinitionsResponse]
  private val emptyAllResponseJson = Json.toJson(BulkApiFieldDefinitionsResponse(Seq()))

  "GET /definition/context/:apiContext/version/:apiVersion" should {
    "return OK when the expected record exists in the repo" in {
      when(mockFieldsDefinitionService.get(FakeContext, FakeVersion)).thenReturn(successful(Some(responseModel)))

      val result = await(controller.getFieldsDefinition(FakeContext, FakeVersion)(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe responseJson
    }

    "return NOT_FOUND when entity does not exist in the repo" in {
      when(mockFieldsDefinitionService.get(FakeContext, FakeVersion)).thenReturn(successful(None))

      val result = await(controller.getFieldsDefinition(FakeContext, FakeVersion)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Fields definition not found for ($fakeRawContext, $fakeRawVersion)"))
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      when(mockFieldsDefinitionService.get(FakeContext, FakeVersion)).thenReturn(failed(emulatedFailure))

      val result = await(controller.getFieldsDefinition(FakeContext, FakeVersion)(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }
  }

  "GET /definition" should {
    "return OK with all field definitions" in {
      when(mockFieldsDefinitionService.getAll).thenReturn(successful(allResponseModel))

      val result = await(controller.getAllFieldsDefinitions(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe allResponseJson
    }

    "return OK with an empty list when no field definitions exist in the repo" in {
      when(mockFieldsDefinitionService.getAll).thenReturn(successful(BulkApiFieldDefinitionsResponse(Seq())))

      val result = await(controller.getAllFieldsDefinitions(FakeRequest()))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe emptyAllResponseJson
    }

    "return INTERNAL_SERVER_ERROR when service throws exception" in {
      when(mockFieldsDefinitionService.getAll).thenReturn(failed(emulatedFailure))

      val result = await(controller.getAllFieldsDefinitions(FakeRequest()))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("UNKNOWN_ERROR"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString("An unexpected error occurred"))
    }
  }

}
