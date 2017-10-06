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

import java.util.UUID

import org.scalatest.OptionValues
import play.api.Logger
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model.ErrorCode.SUBSCRIPTION_FIELDS_ID_NOT_FOUND
import uk.gov.hmrc.apisubscriptionfields.model.{Fields, JsErrorResponse, SubscriptionFieldsRequest, SubscriptionFieldsResponse}
import util.{RequestHeaders, TestData}

import scala.concurrent.Future

class ApiSubscriptionFieldsSpec extends AcceptanceTestSpec
  with OptionValues
  with TestData {

  import play.api.libs.json._
  import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters._

  private val ValidGetRequest = FakeRequest()
    .withHeaders(RequestHeaders.ACCEPT_HMRC_JSON_HEADER)

  private val ValidDeleteRequest = FakeRequest()
    .withHeaders(RequestHeaders.ACCEPT_HMRC_JSON_HEADER)

  val SampleFields1 = Map("field1" -> "value1", "field2" -> "value2")
  val SampleFields2 = Map("field1" -> "value1b", "field3" -> "value3")

  def validPutRequest(fields: Fields): FakeRequest[AnyContentAsJson] = validPutRequest(SubscriptionFieldsRequest(fields))

  def validPutRequest(contents: SubscriptionFieldsRequest): FakeRequest[AnyContentAsJson] = FakeRequest()
    .withHeaders(RequestHeaders.ACCEPT_HMRC_JSON_HEADER, RequestHeaders.CONTENT_TYPE_HEADER)
    .withJsonBody(Json.toJson(contents))

  def fieldsEndpoint(appId: UUID, apiContext: String, apiVersion: String) =
    s"/application/${appId.toString}/context/$apiContext/version/$apiVersion"

  feature("Api-Subscription-Fields") {
    Logger.logger.info(s"App.mode = ${app.mode.toString}")

    scenario("the API is called to GET an unknown subscription fields identifier") {

      Given("ValidGetRequest.copyFakeRequest(method = GET, uri = endpoint(fakeAppId, fakeContext, fakeVersion))a request with an unknown identifier")
      val request = ValidGetRequest.copyFakeRequest(method = GET, uri = fieldsEndpoint(fakeAppId, fakeContext, fakeVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(SUBSCRIPTION_FIELDS_ID_NOT_FOUND, s"Subscription Fields were not found")
    }

    scenario("the API is called to DELETE an unknown subscription fields identifier") {

      Given("a request with an unknown identifier")
      val request = ValidDeleteRequest.copyFakeRequest(method = DELETE, uri = fieldsEndpoint(fakeAppId, fakeContext, fakeVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 404 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_FOUND

      And("the response body is empty")
      contentAsJson(resultFuture) shouldBe JsErrorResponse(SUBSCRIPTION_FIELDS_ID_NOT_FOUND, s"Subscription Fields were not found")
    }

    scenario("the API is called to store some values for a new subscription fields identifier") {
      Given("a request with valid payload")
      val request = validPutRequest(SampleFields1).copyFakeRequest(method = PUT, uri = fieldsEndpoint(fakeAppId, fakeContext, fakeVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 201 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe CREATED

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]

      sfr.isSuccess shouldBe true
      sfr.get should matchPattern { case SubscriptionFieldsResponse(_, SampleFields1) => }

    }

    scenario("the API is called to store more values for an existing subscription fields identifier") {
      Given("a request with valid payload")
      val request = validPutRequest(SampleFields2).copyFakeRequest(method = PUT, uri = fieldsEndpoint(fakeAppId, fakeContext, fakeVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]

      sfr.isSuccess shouldBe true
      sfr.get should matchPattern { case SubscriptionFieldsResponse(_, SampleFields2) => }

    }


    scenario("the API is called to DELETE an known subscription fields identifier") {

      Given("a request with an known identifier")
      val request = ValidDeleteRequest.copyFakeRequest(method = DELETE, uri = fieldsEndpoint(fakeAppId, fakeContext, fakeVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 204 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NO_CONTENT

      And("the response body is empty")
      contentAsString(resultFuture) shouldBe 'empty
    }
  }
}
