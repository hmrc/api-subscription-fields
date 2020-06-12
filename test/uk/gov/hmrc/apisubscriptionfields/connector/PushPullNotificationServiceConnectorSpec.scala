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

package uk.gov.hmrc.apisubscriptionfields.connector

import java.{util => ju}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import play.api.libs.json.Json
import play.api.http.Status.OK
import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.BeforeAndAfterAll
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.apisubscriptionfields.model.{SubscriptionFieldsId, TopicId}
import uk.gov.hmrc.apisubscriptionfields.model.ClientId

class PushPullNotificationServiceConnectorSpec
    extends AsyncHmrcSpec
    with GuiceOneAppPerSuite
    with JsonFormatters
    with BeforeAndAfterAll with BeforeAndAfterEach {

  private val stubPort = 11111
  private val stubHost = "localhost"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(("metrics.jvm", false))
      .configure(("microservice.services.push-pull-notification.uri", s"http://$stubHost:$stubPort"))
      .build()

  implicit lazy val materializer = app.materializer

  // Run wiremock server on local machine with specified port.
  private val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  override def beforeAll() {
    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  override def beforeEach {
    wireMockServer.resetAll()
  }

  override def afterAll() {
    wireMockServer.stop()
  }

  trait Setup {

    val topicName = "topic-name"
    val clientId = ClientId("client-id")
    val subscriptionFieldsId = SubscriptionFieldsId(ju.UUID.randomUUID)
    val topicId = TopicId(ju.UUID.randomUUID())

    val connector = app.injector.instanceOf[PushPullNotificationServiceConnector]
  }

  "PPNS Connector" should {
    "send proper request to post topics" in new Setup {
      val requestBody = Json.stringify(Json.toJson(CreateTopicRequest(topicName, clientId)))
      val response: CreateTopicResponse = CreateTopicResponse(topicId)

      val path = "/topics"
      wireMockServer.stubFor(
        put(path).withRequestBody(equalTo(requestBody))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(Json.stringify(Json.toJson(response)))
          .withStatus(OK)))

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val ret = await(connector.ensureTopicIsCreated(topicName, clientId))
      ret shouldBe (topicId)

      wireMockServer.verify(
        putRequestedFor(urlPathEqualTo(path))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("User-Agent", equalTo("api-subscription-fields"))
      )
    }

    "send proper request to subscribe" in new Setup {
      val callbackUrl = "my-callback"
      val updateRequest: UpdateSubscribersRequest = UpdateSubscribersRequest(List(SubscribersRequest(callbackUrl, "API_PUSH_SUBSCRIBER", Some(subscriptionFieldsId))))
      val requestBody = Json.stringify(Json.toJson(updateRequest))
      val response: UpdateSubscribersResponse = UpdateSubscribersResponse(topicId)

      val path = s"/topics/${topicId.value}/subscribers"
      wireMockServer.stubFor(
        put(path).withRequestBody(equalTo(requestBody))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(Json.stringify(Json.toJson(response)))
          .withStatus(OK)))

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val ret = await(connector.subscribe(subscriptionFieldsId, topicId, callbackUrl))
      ret shouldBe (())

      wireMockServer.verify(
        putRequestedFor(urlPathEqualTo(path))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("User-Agent", equalTo("api-subscription-fields"))
      )
    }
  }
}
