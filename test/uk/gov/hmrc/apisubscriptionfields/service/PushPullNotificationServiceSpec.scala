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

package uk.gov.hmrc.apisubscriptionfields.service

import java.{util => ju}
import scala.concurrent.Future.{failed, successful}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.connector.PushPullNotificationServiceConnector
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.{AsyncHmrcSpec, FieldDefinitionTestData, SubscriptionFieldsTestData}

class PushPullNotificationServiceSpec extends AsyncHmrcSpec with SubscriptionFieldsTestData with FieldDefinitionTestData {

  val boxId: BoxId              = BoxId(ju.UUID.randomUUID())
  val clientId: ClientId        = ClientId(ju.UUID.randomUUID().toString)
  val apiContext: ApiContext    = ApiContext("aContext")
  val apiVersion: ApiVersionNbr = ApiVersionNbr("aVersion")

  trait Setup {
    val mockPPNSConnector: PushPullNotificationServiceConnector = mock[PushPullNotificationServiceConnector]

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val service = new PushPullNotificationService(mockPPNSConnector)
  }

  "creating PPNS box when needed" should {
    val ppnsFieldName     = fieldN(1)
    val expectedTopicName = s"${apiContext.value}##${apiVersion.value}##$ppnsFieldName"

    "succeed when box is created" in new Setup {
      when(mockPPNSConnector.ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)).thenReturn(successful(boxId))

      val result = await(service.ensureBoxIsCreated(clientId, apiContext, apiVersion, ppnsFieldName))

      result shouldBe boxId
    }

    "fail when box creation fails" in new Setup {
      when(mockPPNSConnector.ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)).thenReturn(failed(new RuntimeException))

      intercept[RuntimeException] {
        await(service.ensureBoxIsCreated(clientId, apiContext, apiVersion, ppnsFieldName))
      }
    }
  }

  "updating PPNS callback URL" should {
    val ppnsFieldValue = "localhost:9001/pingme"

    "succeed when update of callback URL is successful" in new Setup {
      when(mockPPNSConnector.updateCallBackUrl(clientId, boxId, ppnsFieldValue)(hc)).thenReturn(successful(Right(())))

      val result = await(service.updateCallbackUrl(clientId, boxId, ppnsFieldValue))

      result shouldBe Right(())
    }

    "fail when update of callback URL fails with an error message" in new Setup {
      val errorMessage = "Error Message"
      when(mockPPNSConnector.updateCallBackUrl(clientId, boxId, ppnsFieldValue)(hc)).thenReturn(successful(Left(errorMessage)))

      val result = await(service.updateCallbackUrl(clientId, boxId, ppnsFieldValue))

      result shouldBe Left(errorMessage)
    }

    "fail when update of callback URL fails with an exception" in new Setup {
      when(mockPPNSConnector.updateCallBackUrl(clientId, boxId, ppnsFieldValue)(hc)).thenReturn(failed(new RuntimeException))

      intercept[RuntimeException] {
        await(service.updateCallbackUrl(clientId, boxId, ppnsFieldValue))
      }
    }
  }
}
