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

import org.mockito.{ArgumentMatchersSugar, MockitoSugar, Strictness}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.service.PushPullNotificationService

trait PushPullNotificationServiceMockModule extends MockitoSugar with ArgumentMatchersSugar with FixedClock {

  protected trait BasePushPullNotificationServiceMock {
    def aMock: PushPullNotificationService

    def verifyZeroInteractions(): Unit = MockitoSugar.verifyZeroInteractions(aMock)

    object SubscribeToPPNS {

      def succeeds(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fieldName: FieldName, fieldValue: FieldValue) =
        when(aMock.subscribeToPPNS(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), eqTo(fieldName), eqTo(fieldValue))(*)).thenReturn(successful(Right(())))

      def fails(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fieldName: FieldName, fieldValue: FieldValue, error: String) =
        when(aMock.subscribeToPPNS(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), eqTo(fieldName), eqTo(fieldValue))(*)).thenReturn(successful(Left(error)))

      @deprecated("", "")
      def verifyCalled() =
        verify(aMock).subscribeToPPNS(*[ClientId], *[ApiContext], *[ApiVersionNbr], *, *)(*)

      @deprecated("", "")
      def verifyCalledWith(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fieldName: FieldName, fieldValue: FieldValue) =
        verify(aMock).subscribeToPPNS(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), eqTo(fieldName), eqTo(fieldValue))(*)
    }
  }

  object PushPullNotificationServiceMock extends BasePushPullNotificationServiceMock {
    val aMock = mock[PushPullNotificationService](withSettings.strictness(Strictness.Warn))
  }
}
