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

package uk.gov.hmrc.apisubscriptionfields.connector

import java.{util => ju}

import akka.stream.Materializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.http.HeaderNames.{CONTENT_TYPE, USER_AGENT}
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ClientId
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec
import uk.gov.hmrc.apisubscriptionfields.connector.JsonFormatters
import uk.gov.hmrc.apisubscriptionfields.model._

class PushPullNotificationServiceConnectorSpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with JsonFormatters with BeforeAndAfterAll with BeforeAndAfterEach {

  private val stubPort = 11111
  private val stubHost = "localhost"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(("metrics.jvm", false))
      .configure(("microservice.services.push-pull-notification.uri", s"http://$stubHost:$stubPort"))
      .build()

  implicit lazy val materializer: Materializer = app.materializer

  // Run wiremock server on local machine with specified port.
  private val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  override def beforeAll(): Unit = {
    super.beforeAll()

    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  override def beforeEach(): Unit = {
    wireMockServer.resetAll()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()

    super.afterAll()
  }

  trait Setup {

    val boxName                                    = "box-name"
    val clientId: ClientId                         = ClientId("client-id")
    val subscriptionFieldsId: SubscriptionFieldsId = SubscriptionFieldsId(ju.UUID.randomUUID)
    val boxId: BoxId                               = BoxId(ju.UUID.randomUUID())

    val connector: PushPullNotificationServiceConnector = app.injector.instanceOf[PushPullNotificationServiceConnector]

    def primeStub(path: String, requestBody: String, responseBody: String): Unit = {
      wireMockServer.stubFor(
        put(path)
          .withRequestBody(equalTo(requestBody))
          .willReturn(
            aResponse()
              .withHeader(CONTENT_TYPE, "application/json")
              .withBody(responseBody)
              .withStatus(OK)
          )
      )
    }

    def primeError(path: String, requestBody: String): Unit = {
      wireMockServer.stubFor(
        put(path)
          .withRequestBody(equalTo(requestBody))
          .willReturn(
            aResponse()
              .withStatus(400)
          )
      )
    }

    def verifyPath(path: String): Unit = {
      wireMockServer.verify(
        putRequestedFor(urlPathEqualTo(path))
          .withHeader(CONTENT_TYPE, equalTo("application/json"))
          .withHeader(USER_AGENT, equalTo("api-subscription-fields"))
      )
    }
    implicit val hc: HeaderCarrier     = HeaderCarrier()
  }

  "PPNS Connector" should {
    "send proper request to post box" in new Setup {
      val requestBody: String  = Json.stringify(Json.toJson(CreateBoxRequest(boxName, clientId)))
      val responseBody: String = Json.stringify(Json.toJson(CreateBoxResponse(boxId)))

      val path = "/box"
      primeStub(path, requestBody, responseBody)

      val ret: BoxId = await(connector.ensureBoxIsCreated(boxName, clientId))
      ret shouldBe boxId

      verifyPath(path)
    }

    "send to post box and map response on error" in new Setup {
      val requestBody: String = Json.stringify(Json.toJson(CreateBoxRequest(boxName, clientId)))

      val path = "/box"
      primeError(path, requestBody)

      intercept[RuntimeException] {
        await(connector.ensureBoxIsCreated(boxName, clientId))
      }
    }

    "send proper request to subscribe" in new Setup {
      val callbackUrl          = "my-callback"
      val requestBody: String  = Json.stringify(Json.toJson(UpdateSubscriberRequest(SubscriberRequest(callbackUrl, "API_PUSH_SUBSCRIBER"))))
      val responseBody: String = Json.stringify(Json.toJson(UpdateSubscriberResponse(boxId)))

      val path = s"/box/${boxId.value}/subscriber"
      primeStub(path, requestBody, responseBody)

      val ret: Unit = await(connector.subscribe(boxId, callbackUrl))
      ret shouldBe (())

      verifyPath(path)
    }

    "send to subscribe and map response on error" in new Setup {
      val callbackUrl         = "my-callback"
      val requestBody: String = Json.stringify(Json.toJson(UpdateSubscriberRequest(SubscriberRequest(callbackUrl, "API_PUSH_SUBSCRIBER"))))

      val path = s"/box/${boxId.value}/subscriber"
      primeError(path, requestBody)

      intercept[RuntimeException] {
        await(connector.subscribe(boxId, callbackUrl))
      }
    }

    "send proper request to update callback and map response on success" in new Setup {
      val callbackUrl          = "my-callback"
      val requestBody: String  = Json.stringify(Json.toJson(UpdateCallBackUrlRequest(clientId, callbackUrl)))
      val responseBody: String = Json.stringify(Json.toJson(UpdateCallBackUrlResponse(successful = true, None)))

      val path = s"/box/${boxId.value}/callback"
      primeStub(path, requestBody, responseBody)

      val ret = await(connector.updateCallBackUrl(clientId, boxId, callbackUrl))
      ret shouldBe Right(())

      verifyPath(path)
    }

    "send proper request to update callback (when callback is empty) and map response on success" in new Setup {
      val callbackUrl          = ""
      val requestBody: String  = Json.stringify(Json.toJson(UpdateCallBackUrlRequest(clientId, callbackUrl)))
      val responseBody: String = Json.stringify(Json.toJson(UpdateCallBackUrlResponse(successful = true, None)))

      val path = s"/box/${boxId.value}/callback"
      primeStub(path, requestBody, responseBody)

      val ret = await(connector.updateCallBackUrl(clientId, boxId, callbackUrl))
      ret shouldBe Right(())

      verifyPath(path)
    }

    "send proper request to update callback and map response on failure" in new Setup {
      val callbackUrl          = "my-callback"
      val requestBody: String  = Json.stringify(Json.toJson(UpdateCallBackUrlRequest(clientId, callbackUrl)))
      val responseBody: String = Json.stringify(Json.toJson(UpdateCallBackUrlResponse(successful = false, Some("some error"))))

      val path = s"/box/${boxId.value}/callback"
      primeStub(path, requestBody, responseBody)

      val ret = await(connector.updateCallBackUrl(clientId, boxId, callbackUrl))
      ret shouldBe Left("some error")

      verifyPath(path)
    }

    "send to update callback and map response on error" in new Setup {
      val callbackUrl         = "my-callback"
      val requestBody: String = Json.stringify(Json.toJson(UpdateCallBackUrlRequest(clientId, callbackUrl)))

      val path = s"/box/${boxId.value}/callback"
      primeError(path, requestBody)

      intercept[RuntimeException] {
        await(connector.updateCallBackUrl(clientId, boxId, callbackUrl))
      }
    }

    "send proper request to update callback and map response on failure with Unknown Error" in new Setup {
      val callbackUrl          = "my-callback"
      val requestBody: String  = Json.stringify(Json.toJson(UpdateCallBackUrlRequest(clientId, callbackUrl)))
      val responseBody: String = Json.stringify(Json.toJson(UpdateCallBackUrlResponse(successful = false, None)))

      val path = s"/box/${boxId.value}/callback"
      primeStub(path, requestBody, responseBody)

      val ret = await(connector.updateCallBackUrl(clientId, boxId, callbackUrl))
      ret shouldBe Left("Unknown Error")

      verifyPath(path)
    }
  }
}
