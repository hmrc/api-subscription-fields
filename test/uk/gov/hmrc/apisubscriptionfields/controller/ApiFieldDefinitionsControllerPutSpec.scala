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

package uk.gov.hmrc.apisubscriptionfields.controller

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.apisubscriptionfields.FieldDefinitionTestData
import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters
import uk.gov.hmrc.apisubscriptionfields.service.ApiFieldDefinitionsService
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful
import scala.concurrent.Future

class ApiFieldDefinitionsControllerPutSpec extends AsyncHmrcSpec
  with FieldDefinitionTestData
  with JsonFormatters
  with StubControllerComponentsFactory {

  private val mockApiFieldDefinitionsService = mock[ApiFieldDefinitionsService]
  private val controller = new ApiFieldDefinitionsController(stubControllerComponents(), mockApiFieldDefinitionsService)

  "PUT /definition/context/:apiContext/version/:apiVersion" should {
    "return CREATED when created in the repo" in {
      when(mockApiFieldDefinitionsService.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions)).thenReturn(successful((FakeApiFieldDefinitionsResponse, true)))

      val json = mkJson(FieldDefinitionsRequest(NelOfFieldDefinitions))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe CREATED
      }
    }

    "return OK when updated in the repo" in {
      when(mockApiFieldDefinitionsService.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions)).thenReturn(successful((FakeApiFieldDefinitionsResponse, false)))

      val json = mkJson(FieldDefinitionsRequest(NelOfFieldDefinitions))
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe OK
      }
    }

    "error when request is invalid" in {
      val json = Json.parse("{}")
      testSubmitResult(mkRequest(json)) { result =>
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }
    }
  }

  private def testSubmitResult(request: Request[JsValue])(test: Future[Result] => Unit) {
    val action: Action[JsValue] = controller.upsertFieldsDefinition(FakeContext, FakeVersion)
    val result: Future[Result] = action.apply(request)
    test(result)
  }

  private def mkRequest(jsonBody: JsValue): Request[JsValue] =
    FakeRequest()
      .withJsonBody(jsonBody).map(r => r.json)

  private def mkJson(model: FieldDefinitionsRequest) = Json.toJson(model)(Json.writes[FieldDefinitionsRequest])
}
