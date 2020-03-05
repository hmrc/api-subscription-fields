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
import play.api.mvc._
import play.api.mvc.request.RequestTarget
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.{FieldsDefinitionTestData, SubscriptionFieldsTestData}

import scala.concurrent.Future

class SubscriptionFieldDefinitionsHappySpec extends AcceptanceTestSpec
  with OptionValues
  with SubscriptionFieldsTestData
  with FieldsDefinitionTestData
  with JsonFormatters {

  feature("Fields-Definition") {

    scenario("the API is called to store some new fields definitions") {
      createDefinitions()
    }


    def createDefinitions(): Unit ={
      Given("Definitiions are created ")
      val putRequest = validDefinitionPutRequest(FakeFieldsDefinitions)
        .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a PUT request with data is sent to the API")
      val putResult: Option[Future[Result]] = route(app, putRequest)

      Then(s"a response with a 201 status is received")
      putResult shouldBe 'defined
      val putResultFuture = putResult.value

      status(putResultFuture) shouldBe CREATED

      And("the response body should be a valid response")
      val sfr = contentAsJson(putResultFuture).validate[FieldsDefinitionResponse]

      sfr.isSuccess shouldBe true
      sfr.get shouldBe FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, FakeFieldsDefinitions)
    }

    scenario("the API is called to GET a known fields definition") {

      //createDefinitions()

      Then("a request with a known fields definition")
      val request = ValidRequest
          .withMethod(GET)
        .withTarget(RequestTarget(uriString = "", path = definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val fdr = contentAsJson(resultFuture).validate[FieldsDefinitionResponse]

      fdr.isSuccess shouldBe true
      fdr.get shouldBe FakeFieldsDefinitionResponse
    }

    scenario("the API is called to GET all fields definitions") {

     // createDefinitions()

      Given("a request for all fields definition")
      val request = ValidRequest
        .withMethod(GET)
        .withTarget(RequestTarget(uriString = allDefinitionsEndpoint, path = allDefinitionsEndpoint, queryString = Map.empty))

      When("a GET request is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val allFdr = contentAsJson(resultFuture).validate[BulkFieldsDefinitionsResponse]

      allFdr.isSuccess shouldBe true
      allFdr.get shouldBe BulkFieldsDefinitionsResponse(List(FakeFieldsDefinitionResponse))
    }

    scenario("the API is called to update some existing fields definitions") {

      //createDefinitions()

      Given("a request with valid payload")
      val request =  validDefinitionPutRequest(Seq.empty)
        .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[FieldsDefinitionResponse]

      sfr.isSuccess shouldBe true
      sfr.get shouldBe FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, Seq.empty)
    }

    scenario("the API is called to delete some existing fields definitions") {

      //createDefinitions()

      Given("a request with valid payload")
      val request = ValidRequest
        .withMethod(DELETE)
        .withTarget( RequestTarget(uriString="", path=definitionEndpoint(fakeRawContext, fakeRawVersion), queryString = Map.empty))

      When("a DELETE request sent to the API")
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
