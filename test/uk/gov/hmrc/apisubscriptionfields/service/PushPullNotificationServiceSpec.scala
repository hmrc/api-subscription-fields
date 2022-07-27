/*
 * Copyright 2022 HM Revenue & Customs
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

import java.{util=>ju}
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinitionType._
import uk.gov.hmrc.apisubscriptionfields.{FieldDefinitionTestData, SubscriptionFieldsTestData}
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec
import scala.concurrent.Future.{successful,failed}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.apisubscriptionfields.connector.PushPullNotificationServiceConnector
import scala.concurrent.ExecutionContext.Implicits.global

class PushPullNotificationServiceSpec extends AsyncHmrcSpec with SubscriptionFieldsTestData with FieldDefinitionTestData {

  val boxId: BoxId = BoxId(ju.UUID.randomUUID())
  val clientId: ClientId = ClientId(ju.UUID.randomUUID().toString)
  val apiContext: ApiContext = ApiContext("aContext")
  val apiVersion: ApiVersion = ApiVersion("aVersion")

  trait Setup {
    val mockPPNSConnector: PushPullNotificationServiceConnector = mock[PushPullNotificationServiceConnector]

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val service = new PushPullNotificationService(mockPPNSConnector)
  }

  "subscribing to PPNS" should {
      val ppnsFieldName = fieldN(1)
      val callbackUrl = "123"
      val oCallbackUrl = Some(callbackUrl)
      val ppnsFieldDefinition = FieldDefinition(ppnsFieldName, "description-1", "hint-1", PPNS_FIELD, "short-description-1" )
      val expectedTopicName = s"${apiContext.value}##${apiVersion.value}##$ppnsFieldName"


    "succeed and return PPNSCallBackUrlSuccessResponse when update of callback URL is successful" in new Setup {
      when(mockPPNSConnector.ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)).thenReturn(successful(boxId))
      when(mockPPNSConnector.updateCallBackUrl(clientId, boxId, callbackUrl)(hc)).thenReturn(successful(PPNSCallBackUrlSuccessResponse))

      val result: PPNSCallBackUrlValidationResponse = await(service.subscribeToPPNS(clientId, apiContext, apiVersion, oCallbackUrl, ppnsFieldDefinition))

      result shouldBe PPNSCallBackUrlSuccessResponse
      verify(mockPPNSConnector).ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)
      verify(mockPPNSConnector).updateCallBackUrl(eqTo(clientId), eqTo(boxId), eqTo(callbackUrl))(*)
    }

    "succeed and return PPNSCallBackUrlSuccessResponse but not call updatecallBackUrl when field is empty" in new Setup {
      when(mockPPNSConnector.ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)).thenReturn(successful(boxId))


      val result: PPNSCallBackUrlValidationResponse = await(service.subscribeToPPNS(clientId, apiContext, apiVersion, None, ppnsFieldDefinition))

      result shouldBe PPNSCallBackUrlSuccessResponse
      verify(mockPPNSConnector).ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)
      verify(mockPPNSConnector, times(0)).updateCallBackUrl(eqTo(clientId), eqTo(boxId), eqTo(callbackUrl))(*)
    }

    "return PPNSCallBackUrlFailedResponse when update of callback URL fails" in new Setup {
      val errorMessage = "Error Message"
      when(mockPPNSConnector.ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)).thenReturn(successful(boxId))
      when(mockPPNSConnector.updateCallBackUrl(clientId, boxId, callbackUrl)(hc)).thenReturn(successful(PPNSCallBackUrlFailedResponse(errorMessage)))

      val result: PPNSCallBackUrlValidationResponse = await(service.subscribeToPPNS(clientId, apiContext, apiVersion, oCallbackUrl, ppnsFieldDefinition))

      result shouldBe PPNSCallBackUrlFailedResponse(errorMessage)
      verify(mockPPNSConnector).ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)
      verify(mockPPNSConnector).updateCallBackUrl(eqTo(clientId), eqTo(boxId), eqTo(callbackUrl))(*)
    }

   "fail when box creation fails" in new Setup {
     when(mockPPNSConnector.ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)).thenReturn(failed(new RuntimeException))

     intercept[RuntimeException] {
       await(service.subscribeToPPNS(clientId, apiContext, apiVersion, oCallbackUrl, ppnsFieldDefinition))
     }
   }

   "fail when update callback url fails" in new Setup {
      when(mockPPNSConnector.ensureBoxIsCreated(eqTo(expectedTopicName), eqTo(clientId))(*)).thenReturn(successful(boxId))
      when(mockPPNSConnector.updateCallBackUrl(clientId, boxId, callbackUrl)(hc)).thenReturn(failed(new RuntimeException))

     intercept[RuntimeException] {
       await(service.subscribeToPPNS(clientId, apiContext, apiVersion, oCallbackUrl, ppnsFieldDefinition))
     }
   }
  }
}
