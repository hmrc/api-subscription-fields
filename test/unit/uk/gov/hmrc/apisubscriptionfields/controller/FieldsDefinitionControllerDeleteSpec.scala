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
import play.api.libs.json.{JsDefined, JsString}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.controller.FieldsDefinitionController
import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters
import uk.gov.hmrc.apisubscriptionfields.service.FieldsDefinitionService
import uk.gov.hmrc.play.test.UnitSpec
import util.FieldsDefinitionTestData

import scala.concurrent.Future

class FieldsDefinitionControllerDeleteSpec extends UnitSpec with FieldsDefinitionTestData with MockFactory with JsonFormatters {

  private val mockFieldsDefinitionService = mock[FieldsDefinitionService]
  private val controller = new FieldsDefinitionController(mockFieldsDefinitionService)

  "DELETE /definition/context/:apiContext/version/:apiVersion" should {
    "return NO_CONTENT (204) when successfully deleted from repo" in {
      (mockFieldsDefinitionService.delete _).expects(FakeContext, FakeVersion).returns(Future.successful(true))

      val result = await(controller.deleteFieldsDefinition(fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe NO_CONTENT
    }

    "return NOT_FOUND (404) when failed to delete from repo" in {
      (mockFieldsDefinitionService.delete _).expects(FakeContext, FakeVersion).returns(Future.successful(false))

      val result = await(controller.deleteFieldsDefinition(fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Fields definition not found for ($fakeRawContext, $fakeRawVersion)"))
    }
  }

}
