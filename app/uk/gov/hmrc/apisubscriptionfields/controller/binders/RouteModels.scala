/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields.controller.binders

import uk.gov.hmrc.apiplatform.modules.common.domain.models.*

// N.B. This is a temporary work around until play supports opaque types in route files.

object RouteModels {
  type SimpleApiContext    = String
  type SimpleApiVersionNbr = String
  type SimpleClientId      = String

  object Conversions {

    given Conversion[SimpleApiContext, ApiContext] with
      def apply(x: SimpleApiContext): ApiContext = ApiContext(x)

    given Conversion[SimpleApiVersionNbr, ApiVersionNbr] with
      def apply(x: SimpleApiVersionNbr): ApiVersionNbr = ApiVersionNbr(x)

    given Conversion[SimpleClientId, ClientId] with
      def apply(x: SimpleClientId): ClientId = ClientId(x)
  }
}
