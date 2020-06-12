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

package uk.gov.hmrc.apisubscriptionfields.service

import uk.gov.hmrc.apisubscriptionfields.connector.PushPullNotificationServiceConnector
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.apisubscriptionfields.model.FieldDefinition
import scala.concurrent.Future
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.model.Types._
import cats.data.{NonEmptyList => NEL}
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class PushPullNotificationService @Inject() (ppnsConnector: PushPullNotificationServiceConnector)(implicit ec: ExecutionContext) {
  def makeTopicName(apiContext: ApiContext, apiVersion: ApiVersion, fieldDefinition: FieldDefinition) : String = {
    val separator = "/"
    s"${apiContext.value}${separator}${apiVersion.value}${separator}${fieldDefinition.name.value}"
  }

  private def subscribeToPPNS(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, subscriptionFieldsId: SubscriptionFieldsId, fieldDefinition: FieldDefinition, oFieldValue: Option[FieldValue])(implicit hc: HeaderCarrier) = {
    for {
      topicId <- ppnsConnector.ensureTopicIsCreated(makeTopicName(apiContext, apiVersion, fieldDefinition), clientId)
      _ <- oFieldValue.fold(Future.successful(()))(fieldValue => ppnsConnector.subscribe(subscriptionFieldsId, topicId, fieldValue))
    } yield ()
  }

  def subscribeToPPNS(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, subscriptionFieldsId: SubscriptionFieldsId, fieldDefinitions: NEL[FieldDefinition], fields: Fields)(implicit hc: HeaderCarrier): Future[Unit] = {
    val subscriptionResponses : List[Future[Unit]] =
      fieldDefinitions
      .filter(_.`type` == FieldDefinitionType.PPNS_TOPIC )
      .map { fieldDefn =>
        subscribeToPPNS(clientId, apiContext, apiVersion, subscriptionFieldsId, fieldDefn, fields.get(fieldDefn.name))
      }

    Future.sequence(subscriptionResponses).map(_ => ())
  }
}
