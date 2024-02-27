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

import scala.concurrent.Future.{failed, successful}

import org.mockito.{ArgumentMatchersSugar, MockitoSugar, Strictness}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

import uk.gov.hmrc.apisubscriptionfields.model.BoxId
import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.service.PushPullNotificationService

trait PushPullNotificationServiceMockModule extends MockitoSugar with ArgumentMatchersSugar with FixedClock {

  protected trait BasePushPullNotificationServiceMock {
    def aMock: PushPullNotificationService

    def verifyZeroInteractions(): Unit = MockitoSugar.verifyZeroInteractions(aMock)

    object EnsureBoxIsCreated {

      def succeeds(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fieldName: FieldName, boxId: BoxId) =
        when(aMock.ensureBoxIsCreated(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), eqTo(fieldName))(*)).thenReturn(successful(boxId))

      def fails(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fieldName: FieldName) =
        when(aMock.ensureBoxIsCreated(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), eqTo(fieldName))(*)).thenReturn(failed(new Exception("bang!")))
    }

    object UpdateCallbackUrl {

      def succeeds(clientId: ClientId, boxId: BoxId, fieldValue: FieldValue) = {
        when(aMock.updateCallbackUrl(eqTo(clientId), eqTo(boxId), eqTo(fieldValue))(*)).thenReturn(successful(Right(())))
      }

      def fails(clientId: ClientId, boxId: BoxId, fieldValue: FieldValue, error: String) =
        when(aMock.updateCallbackUrl(eqTo(clientId), eqTo(boxId), eqTo(fieldValue))(*)).thenReturn(successful(Left(error)))
    }
  }

  object PushPullNotificationServiceMock extends BasePushPullNotificationServiceMock {
    val aMock = mock[PushPullNotificationService](withSettings.strictness(Strictness.Warn))
  }
}
