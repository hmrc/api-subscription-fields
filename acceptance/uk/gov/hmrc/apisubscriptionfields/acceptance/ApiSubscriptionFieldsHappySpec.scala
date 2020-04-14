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

package uk.gov.hmrc.apisubscriptionfields.acceptance

import org.scalatest.OptionValues
import org.scalatest.BeforeAndAfterAll
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.request.RequestTarget
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.{FieldsDefinitionTestData, SubscriptionFieldsTestData}
import scala.concurrent.Future
    import scala.concurrent.Await
    import scala.concurrent.duration._

class ApiSubscriptionFieldsHappySpec extends AcceptanceTestSpec
  with OptionValues
  with JsonFormatters
  with SubscriptionFieldsTestData
  with FieldsDefinitionTestData
  with BeforeAndAfterAll {

  override def beforeAll() {
    val putRequest = validDefinitionPutRequest(FieldsDefinitionRequest(FakeFieldsDefinitions))
      .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

    val r = Await.result(route(app, putRequest).get, 10.seconds)
    Console.println(s"******$r***********")
  }

  override def afterAll() {
    val request = ValidRequest
      .withMethod(DELETE)
      .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

    route(app, request)
  }

  
  feature("Subscription-Fields") {

    Logger.logger.info(s"App.mode = ${app.mode.toString}")

    scenario("the API is called to store some values for a new subscription field") {
      val request: Request[AnyContentAsJson] =  createSubscriptionFieldsRequest()

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 201 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      val r = Await.result(resultFuture, 10.seconds)
      Console.println(s"******$r***********")
    
      status(resultFuture) shouldBe CREATED
      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]
      val fieldsId = sfr.get.fieldsId

      sfr.isSuccess shouldBe true
      sfr.get shouldBe SubscriptionFieldsResponse(fakeRawClientId, "acontext", "1.0.2", fieldsId, SampleFields1)
    }

    def createSubscriptionFieldsRequest(): FakeRequest[AnyContentAsJson] = {
      createRequest(PUT, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))
        .withJsonBody(Json.toJson(SubscriptionFieldsRequest(SampleFields1)))
    }

    def createSubscriptionFields()= {
      route(app, createSubscriptionFieldsRequest())
    }

    scenario("the API is called to GET some existing subscription fields") {

      Given("a request with a known subscription field")
      createSubscriptionFields()

      val request = createRequest(GET, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]
      val fieldsId = sfr.get.fieldsId

      sfr.isSuccess shouldBe true
      sfr.get shouldBe SubscriptionFieldsResponse(fakeRawClientId, "acontext", "1.0.2", fieldsId, SampleFields1)
    }

    scenario("the API is called to GET all existing subscription fields") {

      Given("a request with a known subscription field")
      createSubscriptionFields()

      val request = createRequest(GET, allSubscriptionFieldsEndpoint)

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[BulkSubscriptionFieldsResponse]
      val fieldsId = sfr.get.subscriptions.head.fieldsId

      sfr.isSuccess shouldBe true
      sfr.get shouldBe BulkSubscriptionFieldsResponse(Seq(SubscriptionFieldsResponse(fakeRawClientId, "acontext", "1.0.2", fieldsId, SampleFields1)))
    }

    scenario("the API is called to GET with a known fieldsId") {

      Given("a request with a known fieldsId")

      createSubscriptionFields()

      val requestId =  createRequest(GET, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

      When("an id GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, requestId)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]

      Given("a request with a known fieldsId")
      val fieldsId = sfr.get.fieldsId

      val requestFieldsId = createRequest(GET, fieldsIdEndpoint(fieldsId.value))


      When("a fieldsId GET request with data is sent to the API")
      val resultFieldsId: Option[Future[Result]] = route(app, requestFieldsId)

      Then(s"a response with a 200 status is received")
      resultFieldsId shouldBe 'defined
      val resultFieldsIdFuture = result.value

      status(resultFieldsIdFuture) shouldBe OK
      val sfrFieldsId = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]

      sfrFieldsId.isSuccess shouldBe true
      sfrFieldsId.get shouldBe SubscriptionFieldsResponse(fakeRawClientId, "acontext", "1.0.2", fieldsId, SampleFields1)
    }

    scenario("the API is called to GET existing subscription fields by application clientId") {

      Given("a request with a known client identifier")
      createSubscriptionFields()

      val request = createRequest(GET, byClientIdEndpoint(fakeRawClientId))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[BulkSubscriptionFieldsResponse]
      val fieldsId = sfr.get.subscriptions.head.fieldsId

      sfr.isSuccess shouldBe true
      sfr.get shouldBe BulkSubscriptionFieldsResponse(Seq(SubscriptionFieldsResponse(fakeRawClientId, "acontext", "1.0.2", fieldsId, SampleFields1)))
    }

    scenario("the API is called to update existing subscription fields") {

      Given("a request with valid payload")
      createSubscriptionFields()
      val request = validSubscriptionPutRequest(SampleFields2)
        .withTarget(RequestTarget(uriString = "", path = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]
      val fieldsId = sfr.get.fieldsId

      sfr.isSuccess shouldBe true
      sfr.get shouldBe SubscriptionFieldsResponse(fakeRawClientId, "acontext", "1.0.2", fieldsId, SampleFields2)
    }

    scenario("the API is called to DELETE existing subscription fields") {

      Given("a request with existing subscription fields")
      createSubscriptionFields()

      val request = createRequest(DELETE, subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

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
