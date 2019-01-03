/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.Logger
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.util.{FieldsDefinitionTestData, SubscriptionFieldsTestData}

import scala.concurrent.Future

class ApiSubscriptionFieldsHappySpec extends AcceptanceTestSpec
  with OptionValues
  with SubscriptionFieldsTestData
  with FieldsDefinitionTestData {

  import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters._

  feature("Subscription-Fields") {

    Logger.logger.info(s"App.mode = ${app.mode.toString}")

    scenario("the API is called to store some values for a new subscription field") {
      Given("a request with valid payload")
      val request = validSubscriptionPutRequest(SampleFields1)
        .copyFakeRequest(method = PUT, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 201 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe CREATED

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]
      val fieldsId = sfr.get.fieldsId

      sfr.isSuccess shouldBe true
      sfr.get shouldBe SubscriptionFieldsResponse(fakeRawClientId, "acontext", "1.0.2", fieldsId, SampleFields1)
    }

    scenario("the API is called to GET some existing subscription fields") {

      Given("a request with a known subscription field")
      val request = ValidRequest
        .copyFakeRequest(method = GET, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

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
      val request = ValidRequest
        .copyFakeRequest(method = GET, uri = allSubscriptionFieldsEndpoint)

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
      val requestId = ValidRequest
        .copyFakeRequest(method = GET, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

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
      val requestFieldsId = ValidRequest.copyFakeRequest(method = GET, uri = fieldsIdEndpoint(fieldsId.value))

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
      val request = ValidRequest.copyFakeRequest(method = GET, uri = byClientIdEndpoint(fakeRawClientId))

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
      val request = validSubscriptionPutRequest(SampleFields2)
        .copyFakeRequest(method = PUT, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

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
      val request = ValidRequest
        .copyFakeRequest(method = DELETE, uri = subscriptionFieldsEndpoint(fakeRawClientId, fakeRawContext, fakeRawVersion))

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
