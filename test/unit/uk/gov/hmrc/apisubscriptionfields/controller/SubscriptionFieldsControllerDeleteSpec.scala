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

package uk.gov.hmrc.apisubscriptionfields.controller

import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsDefined, JsString}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.controller.SubscriptionFieldsController
import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

import scala.concurrent.Future

class SubscriptionFieldsControllerDeleteSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory with JsonFormatters {

  private val mockSubscriptionFieldsService = mock[SubscriptionFieldsService]
  private val controller = new SubscriptionFieldsController(mockSubscriptionFieldsService)

  "DELETE /field/application/:clientId/context/:apiContext/version/:apiVersion" should {
    "return NO_CONTENT (204) when successfully deleted from repo" in {
      (mockSubscriptionFieldsService.delete _).expects(FakeClientId, FakeContext, FakeVersion).returns(Future.successful(true))

      val result = await(controller.deleteSubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe NO_CONTENT
    }

    "return NOT_FOUND (404) when failed to delete from repo" in {
      (mockSubscriptionFieldsService.delete _).expects(FakeClientId, FakeContext, FakeVersion).returns(Future.successful(false))

      val result = await(controller.deleteSubscriptionFields(fakeRawClientId, fakeRawContext, fakeRawVersion)(FakeRequest()))

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "code") shouldBe JsDefined(JsString("NOT_FOUND"))
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Subscription fields not found for ($fakeRawClientId, $fakeRawContext, $fakeRawVersion)"))
    }
  }

}
