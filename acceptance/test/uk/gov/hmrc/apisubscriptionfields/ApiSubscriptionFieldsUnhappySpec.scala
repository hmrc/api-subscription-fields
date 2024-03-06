/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields

import scala.concurrent.Future

import org.scalatest.OptionValues

import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._

import uk.gov.hmrc.apisubscriptionfields.model.{ErrorCode, JsErrorResponse}
import uk.gov.hmrc.apisubscriptionfields.utils.ApplicationLogger

class ApiSubscriptionFieldsUnhappySpec extends AcceptanceTestSpec
    with OptionValues
    with SubscriptionFieldsTestData
    with ApplicationLogger {

  Feature("Subscription-Fields") {
    appLogger.logger.info(s"App.mode = ${app.mode.toString}")

    Scenario("the API is called to GET non-existing subscription fields") {

      Given("the API is called to GET non-existing subscription fields")
      val request                        = createRequest(GET, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))
      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(ErrorCode.NOT_FOUND, s"Subscription fields not found for ($fakeRawClientId, $fakeRawContext, $fakeRawVersion)")
    }

    Scenario("the API is called to GET with an unknown fieldsId") {

      Given("the API is called to GET with an unknown fieldsId")
      val request = createRequest(GET, fieldsIdEndpoint(FakeRawFieldsId))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(ErrorCode.NOT_FOUND, s"FieldsId (${FakeRawFieldsId.toString}) was not found")
    }

    Scenario("the API is called to GET an unknown application clientId") {

      Given("the API is called to GET an unknown application clientId")
      val request = createRequest(GET, byClientIdEndpoint(fakeRawClientId))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(ErrorCode.NOT_FOUND, s"ClientId ($fakeRawClientId) was not found")
    }

    Scenario("the API is called to DELETE an unknown subscription field") {

      Given("a request with an unknown subscription field")
      val request = createRequest(DELETE, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(ErrorCode.NOT_FOUND, s"Subscription fields not found for ($fakeRawClientId, $fakeRawContext, $fakeRawVersion)")
    }

    Scenario("the API is called to PUT subscription fields with an invalid JSON payload") {

      Given("the API is called to PUT subscription fields with an invalid JSON payload")
      val request = createRequest(PUT, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))
        .withJsonBody(Json.parse("{}"))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 422 status is received")
      result shouldBe defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNPROCESSABLE_ENTITY

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }

    Scenario("the API is called to PUT subscription fields with an invalid non JSON payload") {

      Given("the API is called to PUT subscription fields with an invalid non JSON payload")
      val request = createRequest(PUT, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))
        .withBody(InvalidNonJsonPayload)

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 415 status is received")
      result shouldBe defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNSUPPORTED_MEDIA_TYPE

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }

  }

}
