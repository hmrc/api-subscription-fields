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

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.model.{SubscriptionFields, SubscriptionFieldsId}
import uk.gov.hmrc.apisubscriptionfields.repository.SubscriptionFieldsRepository

trait SubscriptionFieldsRepositoryMockModule extends MockitoSugar with ArgumentMatchersSugar with FixedClock {

  protected trait BaseSubscriptionFieldsRepositoryMock {
    def aMock: SubscriptionFieldsRepository

    def verifyZeroInteractions(): Unit = MockitoSugar.verifyZeroInteractions(aMock)

    object FetchAll {

      def returns(list: List[SubscriptionFields]) =
        when(aMock.fetchAll).thenReturn(successful(list))
    }

    object FetchByClientId {

      def returns(clientId: ClientId, list: List[SubscriptionFields]) =
        when(aMock.fetchByClientId(eqTo(clientId))).thenReturn(successful(list))
    }

    object Fetch {

      def returns(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fields: SubscriptionFields) =
        when(aMock.fetch(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion))).thenReturn(successful(Some(fields)))

      def returnsNone(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr) =
        when(aMock.fetch(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion))).thenReturn(successful(None))
    }

    object FetchByFieldsId {

      def returns(id: SubscriptionFieldsId, fields: SubscriptionFields) =
        when(aMock.fetchByFieldsId(eqTo(id))).thenReturn(successful(Some(fields)))

      def returnsNone(id: SubscriptionFieldsId) =
        when(aMock.fetchByFieldsId(eqTo(id))).thenReturn(successful(None))
    }

    object SaveAtomic {

      def returns(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fields: SubscriptionFields, isInsert: IsInsert) =
        when(aMock.saveAtomic(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), *)).thenReturn(successful((fields, isInsert)))

      def fails(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, failure: Exception) =
        when(aMock.saveAtomic(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion), *)).thenReturn(failed(failure))

      def verifyCalled() =
        verify(aMock, times(1)).saveAtomic(*[ClientId], *[ApiContext], *[ApiVersionNbr], *)
    }

    object Delete {

      def existsFor(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr) =
        when(aMock.delete(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion))).thenReturn(successful(true))

      def existsFor(clientId: ClientId) =
        when(aMock.delete(eqTo(clientId))).thenReturn(successful(true))

      def notExistingFor(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr) =
        when(aMock.delete(eqTo(clientId), eqTo(apiContext), eqTo(apiVersion))).thenReturn(successful(false))

      def notExistingFor(clientId: ClientId) =
        when(aMock.delete(eqTo(clientId))).thenReturn(successful(false))
    }
  }

  object SubscriptionFieldsRepositoryMock extends BaseSubscriptionFieldsRepositoryMock {
    val aMock = mock[SubscriptionFieldsRepository]
  }
}
