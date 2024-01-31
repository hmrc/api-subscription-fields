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

package uk.gov.hmrc.apisubscriptionfields.repository

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import akka.stream.Materializer
import com.google.inject.ImplementedBy
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}

import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import uk.gov.hmrc.apisubscriptionfields.model.Types.{Fields, IsInsert}
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.utils.ApplicationLogger

@ImplementedBy(classOf[SubscriptionFieldsMongoRepository])
trait SubscriptionFieldsRepository {

  def saveAtomic(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, fields: Fields): Future[(SubscriptionFields, IsInsert)]

  def fetch(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[SubscriptionFields]]
  def fetchByFieldsId(fieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFields]]
  def fetchByClientId(clientId: ClientId): Future[List[SubscriptionFields]]
  def fetchAll: Future[List[SubscriptionFields]]

  def delete(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean]

  def delete(clientId: ClientId): Future[Boolean]
}

@Singleton
class UUIDCreator {
  def uuid(): UUID = UUID.randomUUID()
}

@Singleton
class SubscriptionFieldsMongoRepository @Inject() (mongo: MongoComponent, uuidCreator: UUIDCreator)(implicit ec: ExecutionContext, val mat: Materializer)
    extends PlayMongoRepository[SubscriptionFields](
      collectionName = "subscriptionFields",
      mongoComponent = mongo,
      domainFormat = JsonFormatters.SubscriptionFieldsJF,
      extraCodecs = Seq(
        Codecs.playFormatCodec(JsonFormatters.ApiContextJF),
        Codecs.playFormatCodec(JsonFormatters.ApiVersionJF),
        Codecs.playFormatCodec(JsonFormatters.SubscriptionFieldsIdjsonFormat),
        Codecs.playFormatCodec(JsonFormatters.ValidationJF)
      ),
      indexes = Seq(
        IndexModel(
          ascending(List("clientId", "apiContext", "apiVersion"): _*),
          IndexOptions()
            .name("clientId-apiContext-apiVersion_Index")
            .unique(true)
        ),
        IndexModel(
          ascending("clientId"),
          IndexOptions()
            .name("clientIdIndex")
            .unique(false)
        ),
        IndexModel(
          ascending("fieldsId"),
          IndexOptions()
            .name("fieldsIdIndex")
            .unique(true)
        )
      )
    )
    with SubscriptionFieldsRepository
    with ApplicationLogger
    with JsonFormatters {

  override def saveAtomic(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, fields: Fields): Future[(SubscriptionFields, IsInsert)] = {
    val query = and(
      equal("clientId", Codecs.toBson(clientId.value)),
      equal("apiContext", Codecs.toBson(apiContext.value)),
      equal("apiVersion", Codecs.toBson(apiVersion.value))
    )

    collection.find(query).headOption().flatMap {
      case Some(subscription: SubscriptionFields) =>
        val updatedSubscription = subscription.copy(fields = fields)
        for {
          updatedDefinitions <- collection
                                  .replaceOne(
                                    filter = query,
                                    replacement = updatedSubscription
                                  )
                                  .toFuture()
                                  .map(_ => updatedSubscription)
        } yield (updatedDefinitions, false)

      case None =>
        val subscriptionFieldsId = SubscriptionFieldsId(uuidCreator.uuid())
        val subscription         = SubscriptionFields(clientId, apiContext, apiVersion, subscriptionFieldsId, fields)
        for {
          newSubscriptionFields <- collection.insertOne(subscription).toFuture().map(_ => subscription)
        } yield (newSubscriptionFields, true)
    }
  }

  override def fetch(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[SubscriptionFields]] = {
    val query = and(
      equal("clientId", Codecs.toBson(clientId.value)),
      equal("apiContext", Codecs.toBson(apiContext.value)),
      equal("apiVersion", Codecs.toBson(apiVersion.value))
    )

    collection.find(query).headOption()
  }

  override def fetchByFieldsId(fieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFields]] = {
    val query = and(equal("fieldsId", Codecs.toBson(fieldsId.value)))
    collection.find(query).headOption()
  }

  override def fetchByClientId(clientId: ClientId): Future[List[SubscriptionFields]] = {
    val query = equal("clientId", Codecs.toBson(clientId.value))
    collection.find(query).toFuture().map(_.toList)
  }

  override def fetchAll: Future[List[SubscriptionFields]] = {
    collection.find().toFuture().map(_.toList)
  }

  override def delete(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    val query = and(
      equal("clientId", Codecs.toBson(clientId.value)),
      equal("apiContext", Codecs.toBson(apiContext.value)),
      equal("apiVersion", Codecs.toBson(apiVersion.value))
    )
    collection
      .deleteOne(query)
      .toFuture()
      .map(_.getDeletedCount > 0)
  }

  override def delete(clientId: ClientId): Future[Boolean] = {
    val query = equal("clientId", Codecs.toBson(clientId.value))
    collection
      .deleteMany(query)
      .toFuture()
      .map(_.getDeletedCount > 0)
  }

}
