/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.apisubscriptionfields.model.Types.IsInsert
import uk.gov.hmrc.apisubscriptionfields.repository.ApiFieldDefinitionsRepository

trait FieldDefinitionRepositoryMockModule extends MockitoSugar with ArgumentMatchersSugar with FixedClock {

  protected trait BaseFieldDefinitionRepositoryMock {
    def aMock: ApiFieldDefinitionsRepository

    object Save {

      def returns(definitions: ApiFieldDefinitions, ret: (ApiFieldDefinitions, IsInsert)) =
        when(aMock.save(eqTo(definitions))).thenReturn(successful(ret))
    }

    object Fetch {

      def returns(apiContext: ApiContext, apiVersion: ApiVersionNbr, ret: Option[ApiFieldDefinitions]) =
        when(aMock.fetch(eqTo(apiContext), eqTo(apiVersion))).thenReturn(successful(ret))
    }
  }

  object FieldDefinitionRepositoryMock extends BaseFieldDefinitionRepositoryMock {
    val aMock = mock[ApiFieldDefinitionsRepository]
  }
}
