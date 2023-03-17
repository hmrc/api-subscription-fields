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

import org.scalatest.OptionValues
import play.api.mvc._
import play.api.mvc.request.RequestTarget
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model._
import scala.concurrent.Future

class SubscriptionFieldDefinitionsHappySpec extends AcceptanceTestSpec
  with OptionValues
  with SubscriptionFieldsTestData
  with FieldDefinitionTestData
  with JsonFormatters {


  Feature("Fields-Definition") {


    Scenario("the API is called to store some new fields definitions") {
      Given("Definitiions are created ")
      val putRequest = validDefinitionPutRequest(NelOfFieldDefinitions)
        .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a PUT request with data is sent to the API")
      val putResult: Option[Future[Result]] = route(app, putRequest)

      Then(s"a response with a 201 status is received")
      putResult shouldBe Symbol("defined")
      val putResultFuture = putResult.value

      status(putResultFuture) shouldBe CREATED

      And("the response body should be a valid response")
      val sfr = contentAsJson(putResultFuture).validate[ApiFieldDefinitions]

      sfr.isSuccess shouldBe true
      sfr.get shouldBe ApiFieldDefinitions(FakeContext, FakeVersion, NelOfFieldDefinitions)
    }




    Scenario("the API is called to GET a known fields definition") {

      Then("a request with a known fields definition")
      val request = ValidRequest
          .withMethod(GET)
        .withTarget(RequestTarget(uriString = "", path = definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe Symbol("defined")
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val fdr = contentAsJson(resultFuture).validate[ApiFieldDefinitions]

      fdr.isSuccess shouldBe true
      fdr.get shouldBe FakeApiFieldDefinitionsResponse
    }

    Scenario("the API is called to GET all fields definitions") {

      Given("a request for all fields definition")
      val request = ValidRequest
        .withMethod(GET)
        .withTarget(RequestTarget(uriString = allDefinitionsEndpoint, path = allDefinitionsEndpoint, queryString = Map.empty))

      When("a GET request is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe Symbol("defined")
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val allFdr = contentAsJson(resultFuture).validate[BulkApiFieldDefinitionsResponse]

      allFdr.isSuccess shouldBe true
      allFdr.get shouldBe BulkApiFieldDefinitionsResponse(List(FakeApiFieldDefinitionsResponse))
    }

    Scenario("the API is called to update some existing fields definitions") {

      Given("a request with valid payload")
      val request =  validDefinitionPutRequest(NelOfFieldDefinitions)
        .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe Symbol("defined")
      val resultFuture = result.value
      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[ApiFieldDefinitions]

      sfr.isSuccess shouldBe true
      sfr.get shouldBe ApiFieldDefinitions(FakeContext, FakeVersion, NelOfFieldDefinitions)
    }

    Scenario("the API is called to delete some existing fields definitions") {

      Given("a request with valid payload")
      val request = ValidRequest
        .withMethod(DELETE)
        .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a DELETE request sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 204 status is received")
      result shouldBe Symbol("defined")
      val resultFuture = result.value

      status(resultFuture) shouldBe NO_CONTENT

      And("the response body is empty")
      contentAsString(resultFuture) shouldBe Symbol("empty")
    }

  }

}
