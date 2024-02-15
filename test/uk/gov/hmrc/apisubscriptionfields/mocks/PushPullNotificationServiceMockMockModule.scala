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

package uk.gov.hmrc.apisubscriptionfields.mocks

import scala.concurrent.Future.successful

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

import uk.gov.hmrc.apisubscriptionfields.model.{FieldDefinition, PPNSCallBackUrlValidationResponse, Types}
import uk.gov.hmrc.apisubscriptionfields.service.PushPullNotificationService

trait PushPullNotificationServiceMockModule extends MockitoSugar with ArgumentMatchersSugar with FixedClock {

  protected trait BasePushPullNotificationServiceMock {
    def aMock: PushPullNotificationService

    def verifyZeroInteractions(): Unit = MockitoSugar.verifyZeroInteractions(aMock)

    object SubscribeToPPNS {

      def returns(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fieldValue: Option[Types.FieldValue], response: PPNSCallBackUrlValidationResponse) =
        when(aMock.subscribeToPPNS(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), eqTo(fieldValue), *[FieldDefinition])(*)).thenReturn(successful(response))

      def verifyCalled() =
        verify(aMock, times(1)).subscribeToPPNS(*[ClientId], *[ApiContext], *[ApiVersionNbr], *, *)(*)
    }
  }

  object PushPullNotificationServiceMock extends BasePushPullNotificationServiceMock {
    val aMock = mock[PushPullNotificationService]
  }
}
