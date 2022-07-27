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

import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import uk.gov.hmrc.apisubscriptionfields.FieldDefinitionTestData
import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters
import uk.gov.hmrc.apisubscriptionfields.service.ApiFieldDefinitionsService
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful
import play.api.libs.json._
import play.api.test.Helpers._

class ApiFieldDefinitionsControllerDeleteSpec
    extends AsyncHmrcSpec
    with FieldDefinitionTestData
    with JsonFormatters
    with StubControllerComponentsFactory {

  private val mockFieldsDefinitionService = mock[ApiFieldDefinitionsService]
  private val controller = new ApiFieldDefinitionsController(stubControllerComponents(), mockFieldsDefinitionService)

  "DELETE /definition/context/:apiContext/version/:apiVersion" should {
    "return NO_CONTENT (204) when successfully deleted from repo" in {
      when(mockFieldsDefinitionService.delete(FakeContext, FakeVersion)).thenReturn(successful(true))

      val result = controller.deleteFieldsDefinition(FakeContext, FakeVersion)(FakeRequest())

      status(result) shouldBe NO_CONTENT
    }

    "return NOT_FOUND (404) when failed to delete from repo" in {
      when(mockFieldsDefinitionService.delete(FakeContext, FakeVersion)).thenReturn(successful(false))

      val result = controller.deleteFieldsDefinition(FakeContext, FakeVersion)(FakeRequest())

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Fields definition not found for (${FakeContext.value}, ${FakeVersion.value})"))
    }
  }
}
