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

package uk.gov.hmrc.apisubscriptionfields.controller

import cats.data.{NonEmptyList => NEL}

import uk.gov.hmrc.apiplatform.modules.subscriptionfields.domain.models.{FieldDefinition, Fields}
import uk.gov.hmrc.apiplatform.modules.subscriptionfields.interface.models.{FieldDefinitionsRequest, UpsertFieldValuesRequest}

trait Helper {
  def makeUpsertFieldValuesRequest(fields: Fields): UpsertFieldValuesRequest                  = UpsertFieldValuesRequest(fields)
  def makeFieldDefinitionsRequest(definitions: NEL[FieldDefinition]): FieldDefinitionsRequest = FieldDefinitionsRequest(definitions)
}

object Helper extends Helper
