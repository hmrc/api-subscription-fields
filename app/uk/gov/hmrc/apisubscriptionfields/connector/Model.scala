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

import uk.gov.hmrc.apisubscriptionfields.model.{TopicId, ClientId, SubscriptionFieldsId}

private[connector] case class CreateTopicRequest(topicName: String, clientId: ClientId)

private[connector] case class CreateTopicResponse(topicId: TopicId)

private[connector] case class SubscribersRequest(callBackUrl: String, subscriberType: String, subscriberId: Option[SubscriptionFieldsId] = None)

private[connector] case class UpdateSubscribersRequest(subscribers: List[SubscribersRequest])

private[connector] case class UpdateSubscribersResponse(topicId: TopicId)
