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

import com.google.inject.ImplementedBy
import play.api.Logger
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.{SubscriptionFields, SubscriptionFieldsRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//TODO: look at flattening this into just the class
@ImplementedBy(classOf[RepositoryFedSubscriptionFieldsService])
trait SubscriptionFieldsService {
  def get(identifier: SubscriptionIdentifier): Future[Option[SubscriptionFieldsResponse]]

  def get(subscriptionFieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFieldsResponse]]

  def upsert(identifier: SubscriptionIdentifier, subscriptionFields: Fields): Future[(SubscriptionFieldsResponse, Boolean)]

  def delete(identifier: SubscriptionIdentifier): Future[Boolean]
}

@Singleton
class UUIDCreator {
  def uuid(): UUID = UUID.randomUUID()
}

@Singleton
class RepositoryFedSubscriptionFieldsService @Inject()(repository: SubscriptionFieldsRepository,
                                                       uuidCreator: UUIDCreator) extends SubscriptionFieldsService {

  def upsert(identifier: SubscriptionIdentifier, subscriptionFields: Fields): Future[(SubscriptionFieldsResponse, Boolean)] = {
    def update(existingFieldsId: UUID): Future[SubscriptionFieldsResponse] =
      save(SubscriptionFields(identifier, existingFieldsId, subscriptionFields))

    def create(): Future[SubscriptionFieldsResponse] =
      save(SubscriptionFields(identifier, uuidCreator.uuid(), subscriptionFields))

    Logger.debug(s"[upsert] SubscriptionIdentifier: $identifier")

    repository.fetchById(identifier.encode()) flatMap {
      o =>
        o.fold(
          create() map { x => (x, true) }
        )(
          existing => update(existing.fieldsId) map { x => (x, false) }
        )
    }
  }

  def delete(identifier: SubscriptionIdentifier): Future[Boolean] = {
    val id = identifier.encode()
    Logger.debug(s"[delete] SubscriptionIdentifier: $identifier")
    repository.delete(id)
  }

  def get(identifier: SubscriptionIdentifier): Future[Option[SubscriptionFieldsResponse]] = {
    Logger.debug(s"[get] SubscriptionIdentifier: $identifier")
    for {
      fetch <- repository.fetchById(identifier.encode())
    } yield fetch.map(asResponse)
  }

  def get(subscriptionFieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFieldsResponse]] = {
    Logger.debug(s"[get] SubscriptionFieldsId: $subscriptionFieldsId")
    for {
      fetch <- repository.fetchByFieldsId(subscriptionFieldsId.value)
    } yield fetch.map(asResponse)
  }

  private def save(apiSubscription: SubscriptionFields): Future[SubscriptionFieldsResponse] = {
    Logger.debug(s"[save] SubscriptionFields: $apiSubscription")
    repository.save(apiSubscription) map {
      _ => SubscriptionFieldsResponse(SubscriptionFieldsId(apiSubscription.fieldsId), apiSubscription.customFields)
    }
  }

  private def asResponse(apiSubscription: SubscriptionFields): SubscriptionFieldsResponse = {
    SubscriptionFieldsResponse(fieldsId = SubscriptionFieldsId(apiSubscription.fieldsId), fields = apiSubscription.customFields)
  }
}
