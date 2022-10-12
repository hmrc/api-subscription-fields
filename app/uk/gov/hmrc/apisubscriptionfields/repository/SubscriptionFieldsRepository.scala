/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import uk.gov.hmrc.apisubscriptionfields.model._
import Types.IsInsert
import akka.stream.Materializer
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries}
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import org.mongodb.scala.model.Indexes.ascending
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import uk.gov.hmrc.mongo.play.json.{Codecs, CollectionFactory, PlayMongoRepository}

@ImplementedBy(classOf[SubscriptionFieldsMongoRepository])
trait SubscriptionFieldsRepository {

  def saveAtomic(subscription: SubscriptionFields): Future[(SubscriptionFields, IsInsert)]

  def fetch(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[SubscriptionFields]]
  def fetchByFieldsId(fieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFields]]
  def fetchByClientId(clientId: ClientId): Future[List[SubscriptionFields]]
  def fetchAll: Future[List[SubscriptionFields]]

  def delete(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean]

  def delete(clientId: ClientId): Future[Boolean]
}


@Singleton
class SubscriptionFieldsMongoRepository @Inject()(mongo: MongoComponent)
                                                 (implicit ec: ExecutionContext, val mat: Materializer)
  extends PlayMongoRepository[SubscriptionFields](
    collectionName = "subscriptionFields",
    mongoComponent = mongo,
    domainFormat = JsonFormatters.SubscriptionFieldsJF,
    indexes = Seq(
      IndexModel(ascending(List("clientId", "apiContext", "apiVersion"): _*),
        IndexOptions()
          .name("clientId-apiContext-apiVersion_Index")
          .unique(true)),
      IndexModel(ascending("clientId"),
        IndexOptions()
          .name("clientIdIndex")
          .unique(false)),
      IndexModel(ascending("fieldsId"),
        IndexOptions()
          .name("fieldsIdIndex")
          .unique(true))
    ))
//    {

//  extends ReactiveRepository[SubscriptionFields, BSONObjectID](
//    "subscriptionFields",
//    mongoDbProvider.mongo,
//    JsonFormatters.SubscriptionFieldsJF,
//    ReactiveMongoFormats.objectIdFormats
//  )
  with SubscriptionFieldsRepository
//  with MongoCrudHelper[SubscriptionFields]
  with JsonFormatters {
//
//  override val mongoCollection: JSONCollection = collection
//
//  override def indexes = Seq(
//    createCompoundIndex(
//      indexFieldMappings = Seq(
//        "clientId"   -> IndexType.Ascending,
//        "apiContext" -> IndexType.Ascending,
//        "apiVersion" -> IndexType.Ascending
//      ),
//      indexName = Some("clientId-apiContext-apiVersion_Index"),
//      isUnique = true
//    ),
//    createSingleFieldAscendingIndex(
//      indexFieldKey = "clientId",
//      indexName = Some("clientIdIndex"),
//      isUnique = false
//    ),
//    createSingleFieldAscendingIndex(
//      indexFieldKey = "fieldsId",
//      indexName = Some("fieldsIdIndex"),
//      isUnique = true
//    )
//  )

  override lazy val collection: MongoCollection[SubscriptionFields] =
    CollectionFactory
      .collection(mongo.database, collectionName, domainFormat)
      .withCodecRegistry(
        fromRegistries(
          fromCodecs(
            Codecs.playFormatCodec(domainFormat),
            Codecs.playFormatCodec(JsonFormatters.ApiContextJF),
            Codecs.playFormatCodec(JsonFormatters.ApiVersionJF),
            Codecs.playFormatCodec(JsonFormatters.SubscriptionFieldsIdjsonFormat),
            Codecs.playFormatCodec(JsonFormatters.ValidationJF)
          ),
          MongoClient.DEFAULT_CODEC_REGISTRY
        )
      )


  override def saveAtomic(subscription: SubscriptionFields): Future[(SubscriptionFields, IsInsert)] = {
    val query = and(equal("clientId", Codecs.toBson(subscription.clientId.value)),
      equal("apiContext", Codecs.toBson(subscription.apiContext.value)),
      equal("apiVersion", Codecs.toBson(subscription.apiVersion.value)))


    collection.find(query).headOption flatMap {
      case Some(_: SubscriptionFields) =>
        for {
          updatedDefinitions <- collection.replaceOne(
            filter = query,
            replacement = subscription
          ).toFuture().map(_ => subscription)
        } yield (updatedDefinitions, false)

      case None =>
        for {
          newSubscriptionFields <- collection.insertOne(subscription).toFuture().map(_ => subscription)
        } yield (newSubscriptionFields, true)
    }
//
//
//    saveAtomic(
//      selector = subscriptionFieldsSelector(subscription),
//      updateOperations = Json.obj(
//        "$setOnInsert" -> Json.obj("fieldsId" -> subscription.fieldsId),
//        "$set" -> Json.obj("fields" -> Json.toJson(subscription.fields))
//      )
//    )
//
//    collection
//      .findOneAndReplace(Filters.and(equal("apiContext", Codecs.toBson(definitions.apiContext.value)),
//        equal("apiVersion", Codecs.toBson(definitions.apiVersion.value))),
//        definitions
//      ).map(_.asInstanceOf[ApiFieldDefinitions]).head
  }

  override def fetch(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[SubscriptionFields]] = {
    val query = and(equal("clientId", Codecs.toBson(clientId.value)),
      equal("apiContext", Codecs.toBson(apiContext.value)),
      equal("apiVersion", Codecs.toBson(apiVersion.value)))

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
    val query = and(equal("clientId", Codecs.toBson(clientId.value)),
      equal("apiContext", Codecs.toBson(apiContext.value)),
      equal("apiVersion", Codecs.toBson(apiVersion.value)))
    collection.deleteOne(query)
      .toFuture()
      .map(_.wasAcknowledged())
  }

  override def delete(clientId: ClientId): Future[Boolean] = {
    val query = equal("clientId", Codecs.toBson(clientId.value))
    collection.deleteMany(query)
      .toFuture()
      .map(_.wasAcknowledged())
  }

}
