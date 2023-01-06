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

package uk.gov.hmrc.apisubscriptionfields

import java.util.UUID

import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes
import uk.gov.hmrc.apisubscriptionfields.model._

trait TestData {

  type EmulatedFailure = UnsupportedOperationException
  final val emulatedFailure = new EmulatedFailure("Emulated failure.")
  final val fakeRawClientId = UUID.randomUUID().toString
  final val fakeRawClientId2 = UUID.randomUUID().toString
  final val fakeRawContext = "acontext"
  final val fakeRawContext2 = "acontext2"
  final val fakeRawVersion = "1.0.2"

  final val FakeContext = ApiContext(fakeRawContext)

  final val FakeContext2 = ApiContext(fakeRawContext2)

  final val FakeVersion = ApiVersion(fakeRawVersion)

  final val FakeClientId = ClientId(fakeRawClientId)

  final val FakeClientId2 = ClientId(fakeRawClientId2)

}


object RequestHeaders {

  val CONTENT_TYPE_HEADER: (String, String) = CONTENT_TYPE -> MimeTypes.JSON

  val CONTENT_TYPE_HEADER_INVALID: (String, String) = CONTENT_TYPE -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HMRC_JSON_HEADER: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HEADER_INVALID: (String, String) = ACCEPT -> MimeTypes.JSON

  val ValidHeaders = Map(CONTENT_TYPE_HEADER, ACCEPT_HMRC_JSON_HEADER)
}
