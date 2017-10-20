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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apisubscriptionfields.model.{Fields, SubscriptionFieldsRequest, SubscriptionFieldsResponse, _}
import util.{FieldsDefinitionTestData, RequestHeaders, SubscriptionFieldsTestData}

import scala.concurrent.Future

class ApiSubscriptionFieldsSpec extends AcceptanceTestSpec
  with OptionValues
  with SubscriptionFieldsTestData
  with FieldsDefinitionTestData {

  import uk.gov.hmrc.apisubscriptionfields.model.JsonFormatters._

  private val SampleFields1 = Map("field1" -> "value1", "field2" -> "value2")
  private val SampleFields2 = Map("field1" -> "value1b", "field3" -> "value3")

  private def validSubscriptionPutRequest(fields: Fields): FakeRequest[AnyContentAsJson] =
    validSubscriptionPutRequest(SubscriptionFieldsRequest(fields))

  private def validSubscriptionPutRequest(contents: SubscriptionFieldsRequest): FakeRequest[AnyContentAsJson] =
    fakeRequestWithHeaders.withJsonBody(Json.toJson(contents))

  private def validDefinitionPutRequest(fieldDefinitions: Seq[FieldDefinition]): FakeRequest[AnyContentAsJson] =
    validDefinitionPutRequest(FieldsDefinitionRequest(fieldDefinitions))

  private def validDefinitionPutRequest(contents: FieldsDefinitionRequest): FakeRequest[AnyContentAsJson] =
    fakeRequestWithHeaders.withJsonBody(Json.toJson(contents))

  private def fakeRequestWithHeaders: FakeRequest[AnyContentAsEmpty.type] = {
    FakeRequest().withHeaders(RequestHeaders.ACCEPT_HMRC_JSON_HEADER, RequestHeaders.CONTENT_TYPE_HEADER)
  }
  def fieldsEndpoint(appId: String, apiContext: String, apiVersion: String) =
    s"/field/application/$appId/api-context/$apiContext/version/$apiVersion"

  feature("Subscription-Fields") {
    Logger.logger.info(s"App.mode = ${app.mode.toString}")

    scenario("the API is called to store some values for a new subscription identifier") {
      Given("a request with valid payload")
      val request = validSubscriptionPutRequest(SampleFields1).copyFakeRequest(method = PUT, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 201 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe CREATED

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]

      sfr.isSuccess shouldBe true
      sfr.get should matchPattern { case SubscriptionFieldsResponse(FakeRawIdentifier, _, SampleFields1) => }

    }

    scenario("the API is called to GET with a known subscription identifier") {

      Given("a request with a known identifier")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]

      sfr.isSuccess shouldBe true
      sfr.get should matchPattern { case SubscriptionFieldsResponse(FakeRawIdentifier, _, SampleFields1) => }
    }

    scenario("the API is called to GET with a known fields identifier") {

      Given("a request with a known identifier")
      val requestId = ValidRequest.copyFakeRequest(method = GET, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion))

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
      sfrFieldsId.get should matchPattern { case SubscriptionFieldsResponse(FakeRawIdentifier, `fieldsId`, SampleFields1) => }
    }

    scenario("the API is called to GET with a known application identifier") {

      Given("a request with a known application identifier")
      val request = ValidRequest.copyFakeRequest(method = GET, uri = appIdEndpoint(fakeRawAppId))

      When("a GET request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[BulkSubscriptionFieldsResponse]

      sfr.isSuccess shouldBe true
      sfr.get should matchPattern { case BulkSubscriptionFieldsResponse(Seq(SubscriptionFieldsResponse(FakeRawIdentifier, _, SampleFields1))) => }
    }

    scenario("the API is called to store more values for an existing subscription identifier") {

      Given("a request with valid payload")
      val request = validSubscriptionPutRequest(SampleFields2).copyFakeRequest(method = PUT, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK

      And("the response body should be a valid response")
      val sfr = contentAsJson(resultFuture).validate[SubscriptionFieldsResponse]

      sfr.isSuccess shouldBe true
      sfr.get should matchPattern { case SubscriptionFieldsResponse(FakeRawIdentifier, _, SampleFields2) => }

    }

    scenario("the API is called to DELETE a known subscription identifier") {

      Given("a request with a known identifier")
      val request = ValidRequest.copyFakeRequest(method = DELETE, uri = idEndpoint(fakeRawAppId, fakeRawContext, fakeRawVersion))

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

  feature("Fields-Definition") {

    scenario("the API is called to store some new fields definitions") {

      Given("a request with valid payload")
      val request = validDefinitionPutRequest(FakeFieldsDefinitions).copyFakeRequest(method = PUT, uri = definitionEndpoint(fakeRawContext, fakeRawVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 201 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe CREATED
    }

    scenario("the API is called to GET a known fields definition") {

      Given("a request with a known identifier")
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
      fdr.get should matchPattern { case FieldsDefinitionResponse(FakeFieldsDefinitions) => }
    }

    scenario("the API is called to update some existing fields definitions") {

      Given("a request with valid payload")
      val request = validDefinitionPutRequest(Seq.empty).copyFakeRequest(method = PUT, uri = definitionEndpoint(fakeRawContext, fakeRawVersion))

      When("a PUT request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request)

      Then(s"a response with a 200 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe OK
    }

  }

}
