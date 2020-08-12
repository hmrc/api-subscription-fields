/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields.connector

import uk.gov.hmrc.apisubscriptionfields.model.{BoxId, ClientId}

private[connector] case class CreateBoxRequest(boxName: String, clientId: ClientId)

private[connector] case class CreateBoxResponse(boxId: BoxId)

private[connector] case class SubscriberRequest(callBackUrl: String, subscriberType: String)

private[connector] case class UpdateSubscriberRequest(subscriber: SubscriberRequest)

private[connector] case class UpdateSubscriberResponse(boxId: BoxId)

private[connector] case class UpdateCallBackUrlRequest(clientId: ClientId, callbackUrl: String)

private[connector] case class UpdateCallBackUrlResponse(successful: Boolean, errorMessage: Option[String])
