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
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.metrics._
import uk.gov.hmrc.apisubscriptionfields.config.ApplicationConfig
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.apisubscriptionfields.model.ClientId
import uk.gov.hmrc.apisubscriptionfields.model.BoxId
import uk.gov.hmrc.http.HeaderCarrier
import scala.util.control.NonFatal
import uk.gov.hmrc.apisubscriptionfields.model.SubscriptionFieldsId

@Singleton
class PushPullNotificationServiceConnector @Inject()(http: HttpClient, appConfig: ApplicationConfig, val apiMetrics: ApiMetrics)(implicit ec: ExecutionContext) extends RecordMetrics {
  import  uk.gov.hmrc.apisubscriptionfields.connector.JsonFormatters._

  val api = API("api-subscription-fields")

  private lazy val externalServiceUri = appConfig.pushPullNotificationServiceURL

  def ensureBoxIsCreated(boxName: String, clientId: ClientId)(implicit hc: HeaderCarrier): Future[BoxId] = {
    val payload = CreateBoxRequest(boxName, clientId)

    http.PUT[CreateBoxRequest, CreateBoxResponse](s"$externalServiceUri/box", payload)
    .map(_.boxId)
    .recover {
      case NonFatal(e) => throw new RuntimeException(s"Unexpected response from $externalServiceUri: ${e.getMessage}")
    }
  }

  def subscribe(subscriberFieldsId: SubscriptionFieldsId, boxId: BoxId, callbackUrl: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    val payload = UpdateSubscribersRequest(List(SubscribersRequest(callbackUrl, "API_PUSH_SUBSCRIBER", Some(subscriberFieldsId))))

    http.PUT[UpdateSubscribersRequest, UpdateSubscribersResponse](s"$externalServiceUri/box/${boxId.value.toString}/subscribers", payload)
    .map(_ => ())
    .recover {
      case NonFatal(e) => throw new RuntimeException(s"Unexpected response from $externalServiceUri: ${e.getMessage}")
    }
  }
}
