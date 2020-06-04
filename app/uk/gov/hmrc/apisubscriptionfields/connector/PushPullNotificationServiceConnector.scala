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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.http.metrics._

import uk.gov.hmrc.apisubscriptionfields.config.ApplicationConfig
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.{successful}
import uk.gov.hmrc.apisubscriptionfields.model.ClientId
import java.{util => ju}
import uk.gov.hmrc.apisubscriptionfields.model.TopicId

// TODO - formatters
// TODO - private[connector]

case class CreateTopicRequest(topicName: String, clientId: ClientId)

private[connector] case class CreateTopicResponse(uuid: ju.UUID)

@Singleton
class PushPullNotificationServiceConnector @Inject()(appConfig: ApplicationConfig, val apiMetrics: ApiMetrics)(implicit ec: ExecutionContext) extends RecordMetrics {
  val api = API("api-subscription-fields")

  private lazy val externalServiceUri = appConfig.pushPullNotificationServiceURL

  def notifyOfTopic(topicName: String, clientId: ClientId): Future[TopicId] = {
    val payload = CreateTopicRequest(topicName, clientId)
    println(s"Yeah - PUT $externalServiceUri with payload $payload")

    val response = CreateTopicResponse(ju.UUID.randomUUID)    // TODO really a call

    successful(TopicId(response.uuid)) // TODO - not random
  }
}
