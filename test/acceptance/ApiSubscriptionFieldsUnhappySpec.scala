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

    scenario("the API is called to GET non-existing subscription fields") {

      Given("the API is called to GET non-existing subscription fields")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Subscription fields not found for ($fakeRawClientId, $fakeRawContext, $fakeRawVersion)")
    }

    scenario("the API is called to GET with an unknown fields definition") {

      Given("the API is called to GET with an unknown fields definition")
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

    scenario("the API is called to GET an unknown application clientId") {

      Given("the API is called to GET an unknown application clientId")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = byClientIdEndpoint(fakeRawClientId))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"ClientId ($fakeRawClientId) was not found")
    }

    scenario("the API is called to DELETE an unknown subscription field") {

      Given("a request with an unknown subscription field")
      val request = ValidRequest.copyFakeRequest(method = DELETE, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Subscription fields not found for ($fakeRawClientId, $fakeRawContext, $fakeRawVersion)")
    }

    scenario("the API is called to PUT subscription fields with an invalid JSON payload") {

      Given("the API is called to PUT subscription fields with an invalid JSON payload")
      val request = ValidRequest.copyFakeRequest(method = PUT, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion), body = Json.parse("{}"))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 422 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNPROCESSABLE_ENTITY

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }

    scenario("the API is called to PUT subscription fields with an invalid non JSON payload") {

      Given("the API is called to PUT subscription fields with an invalid non JSON payload")
      val request = ValidRequest.copyFakeRequest(method = PUT, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion), body = InvalidNonJsonPayload)

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 415 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNSUPPORTED_MEDIA_TYPE

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
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Fields definition not found for ($fakeRawContext, unknown)")
    }

    scenario("the API is called to PUT a fields definition with an invalid JSON payload") {

      Given("the API is called to PUT a fields definition with an invalid JSON payload")
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

    scenario("the API is called to PUT a fields definition with an invalid non JSON payload") {

      Given("the API is called to PUT a fields definition with an invalid non JSON payload")
      val request = ValidRequest.copyFakeRequest(method = PUT, uri = definitionEndpoint(fakeRawContext, fakeRawVersion), body = InvalidNonJsonPayload)

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 415 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNSUPPORTED_MEDIA_TYPE

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }
  }
}
