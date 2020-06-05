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

import java.util.UUID

import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.request.RequestTarget
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.apisubscriptionfields.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import cats.data.NonEmptyList
import uk.gov.hmrc.apisubscriptionfields.controller.Helper

trait AcceptanceTestSpec extends FeatureSpec
  with GivenWhenThen
  with BeforeAndAfterAll
  with Matchers
  with GuiceOneServerPerSuite
  with FieldDefinitionTestData
  with Helper {

  protected val ValidRequest = FakeRequest()
    .withHeaders(RequestHeaders.ACCEPT_HMRC_JSON_HEADER)

  protected val allDefinitionsEndpoint = "/definition"

  protected val allSubscriptionFieldsEndpoint = "/field"

  protected val InvalidNonJsonPayload = "##INVALID_JSON_PAYLOAD##"

  protected def subscriptionFieldsEndpoint(clientId: String, apiContext: String, apiVersion: String) =
    s"/field/application/$clientId/context/$apiContext/version/$apiVersion"

  protected def byClientIdEndpoint(clientId: String) = s"/field/application/$clientId"

  protected def definitionEndpoint(apiContext: String, apiVersion: String) = s"/definition/context/$apiContext/version/$apiVersion"

  protected def fieldsIdEndpoint(fieldsId: UUID) = s"/field/$fieldsId"

  protected val SampleFields1 = Map(fieldN(1) -> "http://www.example.com/some-endpoint", fieldN(2) -> "value2")
  protected val SampleFields2 = Map(fieldN(1) -> "https://www.example2.com/updated", fieldN(3) -> "value3")

  protected def validSubscriptionPutRequest(fields: Types.Fields): FakeRequest[AnyContentAsJson] =
    fakeRequestWithHeaders.withMethod(PUT).withJsonBody(Json.toJson(makeSubscriptionFieldsRequest(fields)))

  protected def validDefinitionPutRequest(fieldDefinitions: NonEmptyList[FieldDefinition]): FakeRequest[AnyContentAsJson] =
    fakeRequestWithHeaders.withMethod(PUT).withJsonBody(Json.toJson(makeFieldDefinitionsRequest(fieldDefinitions)))

  protected def fakeRequestWithHeaders: FakeRequest[AnyContentAsEmpty.type] = {
    FakeRequest().withHeaders(RequestHeaders.ACCEPT_HMRC_JSON_HEADER, RequestHeaders.CONTENT_TYPE_HEADER)
  }

  protected def fieldsEndpoint(clientId: String, apiContext: String, apiVersion: String) =
    s"/field/application/$clientId/context/$apiContext/version/$apiVersion"

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(Map(
    "metrics.jvm" -> false,
    "run.mode" -> "Stub",
    "Test.microservice.services.api-subscription-fields.host" -> ExternalServicesConfig.Host,
    "Test.microservice.services.api-subscription-fields.port" -> ExternalServicesConfig.Port
  ))
    .build()

  protected def await[A](future: Future[A]): A = Await.result(future, 5.seconds)

  override protected def beforeAll: Unit = {
    dropDatabase()
  }

  override protected def afterAll: Unit = {
    dropDatabase()
  }

  private def dropDatabase(): Unit = {
   await( app.injector.instanceOf[ReactiveMongoComponent].mongoConnector.db().drop())
  }

  def createRequest(method: String, path: String) =
    ValidRequest
      .withMethod(method)
      .withTarget( RequestTarget(uriString ="", path= path, queryString = Map.empty))
}