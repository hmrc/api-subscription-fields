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

package acceptance

import org.scalatest.OptionValues
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model.ErrorCode.{INVALID_REQUEST_PAYLOAD, NOT_FOUND_CODE}
import uk.gov.hmrc.apisubscriptionfields.model.JsErrorResponse
import util.SubscriptionFieldsTestData

import scala.concurrent.Future

class ApiSubscriptionFieldsUnhappySpec extends AcceptanceTestSpec
  with OptionValues
  with SubscriptionFieldsTestData {

  feature("Subscription-Fields") {
    Logger.logger.info(s"App.mode = ${app.mode.toString}")

    scenario("the API is called to GET an unknown subscription identifier") {

      Given("the API is called to GET an unknown subscription identifier")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Id ($fakeRawAppId, $fakeRawContext, $fakeRawVersion) was not found")
    }

    scenario("the API is called to GET with an unknown fields identifier") {

      Given("the API is called to GET with an unknown fields identifier")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = fieldsIdEndpoint(FakeRawFieldsId))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"FieldsId (${FakeRawFieldsId.toString}) was not found")
    }

    scenario("the API is called to GET an unknown application identifier") {

      Given("the API is called to GET an unknown application identifier")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = appIdEndpoint(fakeRawAppId))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"ApplicationId ($fakeRawAppId) was not found")
    }

    scenario("the API is called to DELETE an unknown subscription fields identifier") {

      Given("a request with an unknown identifier")
      val request = ValidRequest.copyFakeRequest(method = DELETE, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Id ($fakeRawAppId, $fakeRawContext, $fakeRawVersion) was not found")
    }

    scenario("the API is called to PUT subscription fields with an invalid payload") {

      Given("the API is called to PUT subscription fields with an invalid payload")
      val request = ValidRequest.copyFakeRequest(method = PUT, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion), body = Json.parse("{}"))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 422 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNPROCESSABLE_ENTITY

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }
  }

  feature("Fields-Definition") {
    scenario("the API is called to GET an unknown fields definition") {

      Given("the API is called to GET an unknown fields definition")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = definitionEndpoint(fakeRawContext, "unknown"))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Id ($fakeRawContext, unknown) was not found")
    }

    scenario("the API is called to PUT a fields definition with an invalid payload") {

      Given("the API is called to PUT a fields definition with an invalid payload")
      val request = ValidRequest.copyFakeRequest(method = PUT, uri = definitionEndpoint(fakeRawContext, fakeRawVersion), body = Json.parse("{}"))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 422 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNPROCESSABLE_ENTITY

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }
  }
}
