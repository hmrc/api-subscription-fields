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
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.apisubscriptionfields.controller.ApiSubscriptionFieldsController
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import uk.gov.hmrc.play.test.UnitSpec
import util.SubscriptionFieldsTestData

import scala.concurrent.Future

class ApiSubscriptionFieldsControllerSpec extends UnitSpec with SubscriptionFieldsTestData with MockFactory {

  private val mockSubscriptionFieldsService = mock[SubscriptionFieldsService]
  private val controller = new ApiSubscriptionFieldsController(mockSubscriptionFieldsService)

  "GET /application/{application id}/context/{api-context}/version/{api-version}" should {
    "return NOT_FOUND when not in the repo" in {
      (mockSubscriptionFieldsService.get(_: SubscriptionIdentifier)) expects FakeSubscriptionIdentifier returns None

      val result: Future[Result] = controller.getSubscriptionFields(fakeAppId, fakeContext, fakeVersion)(FakeRequest())

      status(result) shouldBe NOT_FOUND
      (contentAsJson(result) \ "message") shouldBe JsDefined(JsString(s"Id ($fakeAppId, $fakeContext, $fakeVersion) was not found"))
    }
  }

}
