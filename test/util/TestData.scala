/*
 * Copyright 2017 HM Revenue & Customs
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

package util

import java.util.UUID

import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.SubscriptionFields

import scala.concurrent.Future

trait TestData {

  final val fakeAppId = UUID.randomUUID()
  final val fakeContext = "acontext"
  final val fakeVersion = "1.0.2"
  final val FakeSubscriptionIdentifier = SubscriptionIdentifier(AppId(fakeAppId),ApiContext(fakeContext),ApiVersion(fakeVersion))
  final val FakeRawIdentifier: String = FakeSubscriptionIdentifier.encode()

  final val FakeRawFieldsId = UUID.randomUUID()
  final val FakeFieldsId = SubscriptionFieldsId(FakeRawFieldsId)

  final val EmptyResponse: Future[Option[SubscriptionFieldsResponse]] = Future.successful(None)
  final val CustomFields = Map("A" -> "X", "B" -> "Y")

  final val FakeApiSubscription = SubscriptionFields(FakeRawIdentifier, FakeRawFieldsId, CustomFields)
  final val FakeSubscriptionFieldsId = SubscriptionFieldsId(FakeRawFieldsId)
  final val ValidResponse: SubscriptionFieldsResponse = SubscriptionFieldsResponse(FakeSubscriptionFieldsId, CustomFields)

}

object TestData extends TestData


object RequestHeaders {

  val CONTENT_TYPE_HEADER: (String, String) = CONTENT_TYPE -> MimeTypes.JSON

  val CONTENT_TYPE_HEADER_INVALID: (String, String) = CONTENT_TYPE -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HMRC_JSON_HEADER: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"

  val ACCEPT_HEADER_INVALID: (String, String) = ACCEPT -> MimeTypes.JSON

  val ValidHeaders = Map(
  CONTENT_TYPE_HEADER,
  ACCEPT_HMRC_JSON_HEADER)
}
