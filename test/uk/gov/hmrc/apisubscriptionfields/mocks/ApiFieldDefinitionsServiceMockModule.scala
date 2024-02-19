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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

import uk.gov.hmrc.apisubscriptionfields.model.ApiFieldDefinitions
import uk.gov.hmrc.apisubscriptionfields.service.ApiFieldDefinitionsService

trait ApiFieldDefinitionsServiceMockModule extends MockitoSugar with ArgumentMatchersSugar with FixedClock {

  protected trait BaseApiFieldDefinitionsServiceMock {
    def aMock: ApiFieldDefinitionsService

    def verifyZeroInteractions(): Unit = MockitoSugar.verifyZeroInteractions(aMock)

    object Get {

      def returns(context: ApiContext, version: ApiVersionNbr, response: ApiFieldDefinitions) =
        when(aMock.get(eqTo(context), eqTo(version))).thenReturn(successful(Some(response)))

      def returnsNothing(context: ApiContext, version: ApiVersionNbr) =
        when(aMock.get(eqTo(context), eqTo(version))).thenReturn(successful(None))
    }
  }

  object ApiFieldDefinitionsServiceMock extends BaseApiFieldDefinitionsServiceMock {
    val aMock = mock[ApiFieldDefinitionsService]
  }
}
