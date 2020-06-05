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

package uk.gov.hmrc.apisubscriptionfields

import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.request.RequestTarget
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model.ErrorCode.{INVALID_REQUEST_PAYLOAD, NOT_FOUND_CODE}
import uk.gov.hmrc.apisubscriptionfields.model.JsErrorResponse

import scala.concurrent.Future

class SubscriptionFieldDefinitionsUnhappySpec extends AcceptanceTestSpec
  with OptionValues
  with SubscriptionFieldsTestData {

  feature("Fields-Definition") {
    scenario("the API is called to GET an unknown fields definition") {

      Given("the API is called to GET an unknown fields definition")
      val request = ValidRequest.withTarget(RequestTarget(uriString = "", path = definitionEndpoint(fakeRawContext, "unknown"), queryString = Map.empty))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Fields definition not found for (${FakeContext.value}, unknown)")
    }

    scenario("the API is called to PUT a fields definition with an invalid JSON payload") {

      Given("the API is called to PUT a fields definition with an invalid JSON payload")
      val request = ValidRequest
        .withMethod(PUT)
        .withTarget(RequestTarget(uriString = "", path = definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))
        .withJsonBody(Json.parse("{}"))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 422 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNPROCESSABLE_ENTITY

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }

    scenario("the API is called to PUT a fields definition with an invalid field definition type") {

      val invalidFieldDefinition =
        """{
          |  "fieldDefinitions": [
          |    {
          |      "name" : "address",
          |      "description" : "where you live",
          |      "type" : "NOT_VALID_TYPE"
          |    }
          |  ]
          |}
          |""".stripMargin

      Given("the API is called to PUT a fields definition with an invalid JSON payload")

      val request = ValidRequest
        .withMethod(PUT)
        .withTarget(RequestTarget(uriString = "", path = definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))
        .withJsonBody(Json.parse(invalidFieldDefinition))


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

      val request = ValidRequest
        .withMethod(PUT)
        .withTarget(RequestTarget(uriString = "", path = definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))
        .withBody(InvalidNonJsonPayload)

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 415 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe UNSUPPORTED_MEDIA_TYPE

      And("the response body contains error message")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(INVALID_REQUEST_PAYLOAD, _: Json.JsValueWrapper)
    }

    scenario("the API is called to DELETE an unknown fields definition") {

      Given("a request with an unknown fields definition")

      val request = ValidRequest
        .withMethod(DELETE)
        .withTarget(RequestTarget(uriString = "", path = definitionEndpoint(fakeRawContext, "unknown"), queryString = Map.empty))


      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(NOT_FOUND_CODE, s"Fields definition not found for ($fakeRawContext, unknown)")
    }

  }

}
