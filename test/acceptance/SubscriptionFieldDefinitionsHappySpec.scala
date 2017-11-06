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
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model._
import util.{FieldsDefinitionTestData, SubscriptionFieldsTestData}

import scala.concurrent.Future

class SubscriptionFieldDefinitionsHappySpec extends AcceptanceTestSpec
  with OptionValues
  with SubscriptionFieldsTestData
  with FieldsDefinitionTestData {

  import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters._

  feature("Fields-Definition") {

    scenario("the API is called to store some new fields definitions") {

      Given("a request with valid payload")
      val request = validDefinitionPutRequest(FakeFieldsDefinitions)
        .copyFakeRequest(method = PUT, uri = definitionEndpoint(fakeRawContext, fakeRawVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 201 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe CREATED

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[FieldsDefinitionResponse]

      sfr.isSuccess shouldBe true
      sfr.get shouldBe FieldsDefinitionResponse(fakeRawContext, fakeRawVersion, FakeFieldsDefinitions)
    }

    scenario("the API is called to GET a known fields definition") {

      Given("a request with a known fields definition")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = definitionEndpoint(fakeRawContext, fakeRawVersion))

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

      Given("a request for all fields definition")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = allDefinitionsEndpoint)

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

      Given("a request with valid payload")
      val request = validDefinitionPutRequest(Seq.empty)
        .copyFakeRequest(method = PUT, uri = definitionEndpoint(fakeRawContext, fakeRawVersion))

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

      Given("a request with valid payload")
      val request = validDefinitionPutRequest(Seq.empty)
        .copyFakeRequest(method = DELETE, uri = definitionEndpoint(fakeRawContext, fakeRawVersion))

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
