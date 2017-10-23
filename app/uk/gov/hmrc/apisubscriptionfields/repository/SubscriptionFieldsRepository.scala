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

package uk.gov.hmrc.apisubscriptionfields.repository

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.Logger
import play.api.libs.json._
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.apisubscriptionfields.model.SubscriptionIdentifier
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[SubscriptionFieldsMongoRepository])
trait SubscriptionFieldsRepository {

  def save(subscription: SubscriptionFields): Future[Boolean]

  def fetchByClientId(clientId: String): Future[List[SubscriptionFields]]
  def fetch(identifier: SubscriptionIdentifier): Future[Option[SubscriptionFields]]
  def fetchByFieldsId(fieldsId: UUID): Future[Option[SubscriptionFields]]

  def delete(identifier: SubscriptionIdentifier): Future[Boolean]
}

@Singleton
class SubscriptionFieldsMongoRepository @Inject()(mongoDbProvider: MongoDbProvider)
  extends ReactiveRepository[SubscriptionFields, BSONObjectID]("subscriptionFields", mongoDbProvider.mongo,
    MongoFormatters.SubscriptionFieldsJF, ReactiveMongoFormats.objectIdFormats)
  with SubscriptionFieldsRepository
  with MongoIndexCreator
  with MongoErrorHandler {

  private implicit val format = MongoFormatters.SubscriptionFieldsJF

  override def indexes = Seq(
    createCompoundIndex(
      indexFieldMappings = Seq(
        "clientId"   -> IndexType.Ascending,
        "apiContext" -> IndexType.Ascending,
        "apiVersion" -> IndexType.Ascending
      ),
      indexName = Some("clientId-apiContext-apiVersion_Index"),
      isUnique = true
    ),
    createSingleFieldAscendingIndex(
      indexFieldKey = "clientId",
      indexName = Some("clientIdIndex"),
      isUnique = false
    ),
    createSingleFieldAscendingIndex(
      indexFieldKey = "fieldsId",
      indexName = Some("fieldsIdIndex"),
      isUnique = true
    )
  )

  override def save(subscription: SubscriptionFields): Future[Boolean] = {
    collection.update(selector = selectorForSubscriptionFields(subscription), update = subscription, upsert = true).map {
      updateWriteResult => handleSaveError(updateWriteResult, s"Could not save subscription fields: $subscription",
        updateWriteResult.upserted.nonEmpty
      )
    }
  }

  override def fetchByClientId(clientId: String): Future[List[SubscriptionFields]] = {
    val selector = Json.obj("clientId" -> clientId)
    Logger.debug(s"[fetchByClientId] selector: $selector")
    collection.find(selector).cursor[SubscriptionFields](ReadPreference.primary).collect[List](
      Int.MaxValue, Cursor.FailOnError[List[SubscriptionFields]]()
    )
  }

  override def fetch(identifier: SubscriptionIdentifier): Future[Option[SubscriptionFields]] = {
    val selector = selectorForIdentifier(identifier)
    Logger.debug(s"[fetch] selector: $selector")
    collection.find(selector).one[SubscriptionFields]
  }

  private def selectorForIdentifier(clientId: String, apiContext: String, apiVersion: String): JsObject = {
    Json.obj(
      "clientId"   -> clientId,
      "apiContext" -> apiContext,
      "apiVersion" -> apiVersion
    )
  }

  private def selectorForIdentifier(identifier: SubscriptionIdentifier): JsObject = {
    selectorForIdentifier(identifier.clientId.value, identifier.apiContext.value, identifier.apiVersion.value)
  }

  private def selectorForSubscriptionFields(subscription: SubscriptionFields): JsObject = {
    selectorForIdentifier(subscription.clientId, subscription.apiContext, subscription.apiVersion)
  }

  override def fetchByFieldsId(fieldsId: UUID): Future[Option[SubscriptionFields]] = {
    val selector = Json.obj("fieldsId" -> fieldsId)
    Logger.debug(s"[fetchByFieldsId] selector: $selector")
    collection.find(selector).one[SubscriptionFields]
  }

  override def delete(identifier: SubscriptionIdentifier): Future[Boolean] = {
    val selector = selectorForIdentifier(identifier)
    Logger.debug(s"[delete] selector: $selector")
    collection.remove(selector).map {
      writeResult => handleDeleteError(writeResult, s"Could not delete subscription fields for id: $identifier")
    }
  }
}
