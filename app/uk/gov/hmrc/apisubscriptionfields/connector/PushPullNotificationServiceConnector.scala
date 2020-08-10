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
import uk.gov.hmrc.apisubscriptionfields.config.ApplicationConfig
import uk.gov.hmrc.apisubscriptionfields.model.{BoxId, ClientId, PPNSCallBackUrlValidationResponse, PPNSCallBackUrlSuccessResponse, PPNSCallBackUrlFailedResponse}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.metrics._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import uk.gov.hmrc.apisubscriptionfields.model.PPNSCallBackUrlSuccessResponse
import uk.gov.hmrc.apisubscriptionfields.model.PPNSCallBackUrlFailedResponse

@Singleton
class PushPullNotificationServiceConnector @Inject()(http: HttpClient, appConfig: ApplicationConfig, val apiMetrics: ApiMetrics)
                                                    (implicit ec: ExecutionContext) extends RecordMetrics {
  import uk.gov.hmrc.apisubscriptionfields.connector.JsonFormatters._

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

  def subscribe(boxId: BoxId, callbackUrl: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    val payload = UpdateSubscriberRequest(SubscriberRequest(callbackUrl, "API_PUSH_SUBSCRIBER"))

    http.PUT[UpdateSubscriberRequest, UpdateSubscriberResponse](s"$externalServiceUri/box/${boxId.value.toString}/subscriber", payload)
    .map(_ => ())
    .recover {
      case NonFatal(e) => throw new RuntimeException(s"Unexpected response from $externalServiceUri: ${e.getMessage}")
    }
  }

  def updateCallBackUrl(boxId: BoxId, callbackUrl: String)(implicit hc: HeaderCarrier): Future[PPNSCallBackUrlValidationResponse] = {
    val payload = UpdateCallBackUrlRequest(callbackUrl)

    http.PUT[UpdateCallBackUrlRequest, UpdateCallBackUrlResponse](s"$externalServiceUri/box/${boxId.value.toString}/callback", payload)
    .map(response =>
      if(response.successful) PPNSCallBackUrlSuccessResponse
      else response.errorMessage.fold(PPNSCallBackUrlFailedResponse("Unknown Error"))(PPNSCallBackUrlFailedResponse)
    ).recover {
      case NonFatal(e) => throw new RuntimeException(s"Unexpected response from $externalServiceUri: ${e.getMessage}")
    }
  }
}
