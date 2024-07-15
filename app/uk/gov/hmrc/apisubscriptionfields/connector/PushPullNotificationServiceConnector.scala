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

package uk.gov.hmrc.apisubscriptionfields.connector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import play.api.libs.json.Json
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ClientId
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.http.metrics.common._

import uk.gov.hmrc.apisubscriptionfields.config.ApplicationConfig
import uk.gov.hmrc.apisubscriptionfields.model.Types.FieldValue
import uk.gov.hmrc.apisubscriptionfields.model._

@Singleton
class PushPullNotificationServiceConnector @Inject() (http: HttpClientV2, appConfig: ApplicationConfig, val apiMetrics: ApiMetrics)(implicit ec: ExecutionContext)
    extends RecordMetrics {
  import uk.gov.hmrc.apisubscriptionfields.connector.JsonFormatters._

  val api: API = API("api-subscription-fields")

  private lazy val externalServiceUri = appConfig.pushPullNotificationServiceURL

  def ensureBoxIsCreated(boxName: String, clientId: ClientId)(implicit hc: HeaderCarrier): Future[BoxId] = {
    val payload = CreateBoxRequest(boxName, clientId)

    http
      .put(url"$externalServiceUri/box")
      .withBody(Json.toJson(payload))
      .execute[CreateBoxResponse]
      .map(_.boxId)
      .recover { case NonFatal(e) =>
        throw new RuntimeException(s"Unexpected response from $externalServiceUri: ${e.getMessage}")
      }
  }

  def subscribe(boxId: BoxId, callbackUrl: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    val payload = UpdateSubscriberRequest(SubscriberRequest(callbackUrl, "API_PUSH_SUBSCRIBER"))

    http
      .put(url"$externalServiceUri/box/${boxId.value.toString}/subscriber")
      .withBody(Json.toJson(payload))
      .execute[UpdateSubscriberResponse]
      .map(_ => ())
      .recover { case NonFatal(e) =>
        throw new RuntimeException(s"Unexpected response from $externalServiceUri: ${e.getMessage}")
      }
  }

  def updateCallBackUrl(clientId: ClientId, boxId: BoxId, callbackUrl: FieldValue)(implicit hc: HeaderCarrier): Future[Either[String, Unit]] = {
    val payload = UpdateCallBackUrlRequest(clientId, callbackUrl)
    http
      .put(url"$externalServiceUri/box/${boxId.value.toString}/callback")
      .withBody(Json.toJson(payload))
      .execute[UpdateCallBackUrlResponse]
      .map(response =>
        if (response.successful)
          Right(())
        else
          Left(response.errorMessage.getOrElse("Unknown Error"))
      )
      .recover { case NonFatal(e) =>
        throw new RuntimeException(s"Unexpected response from $externalServiceUri: ${e.getMessage}")
      }
  }
}
