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

import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.modules.reactivemongo.MongoDbConnection
import util.{ExternalServicesConfig, RequestHeaders}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


trait AcceptanceTestSpec extends FeatureSpec
  with GivenWhenThen
  with BeforeAndAfterAll
  with Matchers
  with GuiceOneAppPerSuite {

  protected val ValidRequest = FakeRequest()
    .withHeaders(RequestHeaders.ACCEPT_HMRC_JSON_HEADER)

  protected def idEndpoint(appId: String, apiContext: String, apiVersion: String) =
    s"/field/application/$appId/api-context/$apiContext/version/$apiVersion"

  protected def appIdEndpoint(appId: String) = s"/field/application/$appId"

  protected def definitionEndpoint(apiContext: String, apiVersion: String) = s"/definition/api-context/$apiContext/version/$apiVersion"

  protected def fieldsIdEndpoint(fieldsId: UUID) = s"/field/$fieldsId"

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(Map(
    "run.mode" -> "Stub",
    "Test.microservice.services.api-subscription-fields.host" -> ExternalServicesConfig.Host,
    "Test.microservice.services.api-subscription-fields.port" -> ExternalServicesConfig.Port
    ))
    .build()

  protected def await[A](future: Future[A]): A = Await.result(future, 5.seconds)

  override protected def beforeAll = {
    dropDatabase()
  }

  override protected def afterAll = {
    dropDatabase()
  }

  private def dropDatabase() = {
    await(new MongoDbConnection(){}.db().drop())
  }
}