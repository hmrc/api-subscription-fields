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
import cats.data.{NonEmptyList => NEL}

class PushPullNotificationServiceSpec extends AsyncHmrcSpec with SubscriptionFieldsTestData with FieldDefinitionTestData {

  val topicId: TopicId = TopicId(ju.UUID.randomUUID())
  val clientId: ClientId = ClientId(ju.UUID.randomUUID().toString)
  val apiContext: ApiContext = ApiContext("aContext")
  val apiVersion: ApiVersion = ApiVersion("aVersion")
  val subscriptionFieldsId: SubscriptionFieldsId = SubscriptionFieldsId(ju.UUID.randomUUID())

  trait Setup {
    val mockPPNSConnector = mock[PushPullNotificationServiceConnector]

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val service = new PushPullNotificationService(mockPPNSConnector)
  }

  "subscribing to PPNS" should {
      val ppnsFieldName = fieldN(1)
      val callbackUrl = "123"
      val fieldDef1 = FieldDefinition(ppnsFieldName, "description-1", "hint-1", PPNS_TOPIC, "short-description-1" )
      val fieldDef2 = FieldDefinition(fieldN(2), "description-2", "hint-2", STRING, "short-description-2" )
      val fieldDefns: NEL[FieldDefinition] = NEL.of(fieldDef1, fieldDef2)
      val fields: Types.Fields = Map(fieldN(1) -> callbackUrl, fieldN(2) -> "something else")
      val expectedTopicName = s"${apiContext.value}/${apiVersion.value}/${ppnsFieldName}"

    "succeed and return topicId when fields with a PPNS_TOPIC are provided" in new Setup {

      when(mockPPNSConnector.ensureTopicIsCreated(eqTo(expectedTopicName), any[ClientId])(*)).thenReturn(successful(topicId))
      when(mockPPNSConnector.subscribe(subscriptionFieldsId,topicId,callbackUrl)(hc)).thenReturn(successful(topicId))

      await(service.subscribeToPPNS(clientId, apiContext, apiVersion, subscriptionFieldsId, fieldDefns, fields)) shouldBe (())
    }

    "gracefully do nothing when no PPNS_TOPIC field is provided" in new Setup {
      val localFieldDefns: NEL[FieldDefinition] = NEL.of(fieldDef2)
      val localFields: Types.Fields = Map(fieldN(2) -> "something else")
      await(service.subscribeToPPNS(clientId, apiContext, apiVersion, subscriptionFieldsId, localFieldDefns, localFields)) shouldBe (())

      verify(mockPPNSConnector, never).ensureTopicIsCreated(*, any[ClientId])(*)
    }

    "fail when topic creation fails" in new Setup {
      when(mockPPNSConnector.ensureTopicIsCreated(*, any[ClientId])(*)).thenReturn(failed(new RuntimeException))

      intercept[RuntimeException] {
        await(service.subscribeToPPNS(clientId, apiContext, apiVersion, subscriptionFieldsId, fieldDefns, fields))
      }
    }

    "fail when subscribe to topic fails" in new Setup {
      when(mockPPNSConnector.ensureTopicIsCreated((*), any[ClientId])(*)).thenReturn(successful(topicId))
      when(mockPPNSConnector.subscribe(subscriptionFieldsId,topicId,callbackUrl)(hc)).thenReturn(failed(new RuntimeException))

      intercept[RuntimeException] {
        await(service.subscribeToPPNS(clientId, apiContext, apiVersion, subscriptionFieldsId, fieldDefns, fields))
      }
    }
  }
}
