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

package uk.gov.hmrc.apisubscriptionfields.service

import java.util.UUID
import javax.inject._

import play.api.Logger
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.{SubscriptionFields, SubscriptionFieldsRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UUIDCreator {
  def uuid(): UUID = UUID.randomUUID()
}

@Singleton
class SubscriptionFieldsService @Inject()(repository: SubscriptionFieldsRepository, uuidCreator: UUIDCreator) {

  def upsert(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, subscriptionFields: Fields): Future[(SubscriptionFieldsResponse, Boolean)] = {

    def update(existingFieldsId: UUID): Future[SubscriptionFieldsResponse] =
      save(SubscriptionFields(clientId.value, apiContext.value, apiVersion.value, existingFieldsId, subscriptionFields))

    def create(): Future[SubscriptionFieldsResponse] =
      save(SubscriptionFields(clientId.value, apiContext.value, apiVersion.value, uuidCreator.uuid(), subscriptionFields))

    Logger.debug(s"[upsert subscription fields] clientId: $clientId, apiContext: $apiVersion, apiVersion: $apiVersion")
    repository.fetch(clientId, apiContext, apiVersion) flatMap { o =>
      o.fold(
        create().map((_, true))
      )(
        existing => update(existing.fieldsId).map((_, false))
      )
    }

  }

  def delete(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    Logger.debug(s"[delete subscription fields] clientId: $clientId, apiContext: $apiVersion, apiVersion: $apiVersion")
    repository.delete(clientId, apiContext, apiVersion)
  }

  def get(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[SubscriptionFieldsResponse]] = {
    Logger.debug(s"[get subscription fields] clientId: $clientId, apiContext: $apiVersion, apiVersion: $apiVersion")
    for {
      fetch <- repository.fetch(clientId, apiContext, apiVersion)
    } yield fetch.map(asResponse)
  }

  def get(subscriptionFieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFieldsResponse]] = {
    Logger.debug(s"[get subscription fields] fieldsId: $subscriptionFieldsId")
    for {
      fetch <- repository.fetchByFieldsId(subscriptionFieldsId)
    } yield fetch.map(asResponse)
  }

  def get(clientId: ClientId): Future[Option[BulkSubscriptionFieldsResponse]] = {
    Logger.debug(s"[get subscription fields] clientId: $clientId")
    (for {
      fields <- repository.fetchByClientId(clientId)
    } yield fields.map(asResponse)) map {
      case Nil => None
      case fs => Some(BulkSubscriptionFieldsResponse(subscriptions = fs))
    }
  }

  def getAll: Future[BulkSubscriptionFieldsResponse] = {
    Logger.debug(s"[get all subscription fields]")
    (for {
      fields <- repository.fetchAll()
    } yield fields.map(asResponse)) map (BulkSubscriptionFieldsResponse(_))
  }

  private def save(apiSubscriptionFields: SubscriptionFields): Future[SubscriptionFieldsResponse] = {
    Logger.debug(s"[save subscription fields] subscriptionFields: $apiSubscriptionFields")
    repository.save(apiSubscriptionFields).map(_ => asResponse(apiSubscriptionFields))
  }

  private def asResponse(apiSubscription: SubscriptionFields): SubscriptionFieldsResponse = {
    SubscriptionFieldsResponse(
      clientId = apiSubscription.clientId,
      apiContext = apiSubscription.apiContext,
      apiVersion = apiSubscription.apiVersion,
      fieldsId = SubscriptionFieldsId(apiSubscription.fieldsId),
      fields = apiSubscription.fields)
  }

}
