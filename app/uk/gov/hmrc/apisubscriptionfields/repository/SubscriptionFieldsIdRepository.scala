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

import play.api.libs.json._
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.indexes.Index
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.apisubscriptionfields.model.{ApiSubscription, MongoFormatters}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SubscriptionFieldsIdRepository extends Repository[ApiSubscription, BSONObjectID] {

  def save(subscription: ApiSubscription): Future[Unit]

  def fetchById(id: String): Future[Option[ApiSubscription]]
  def fetchByFieldsId(fieldsId: UUID): Future[Option[ApiSubscription]]

  def delete(id: String): Future[Unit]
}

class SubscriptionFieldsIdMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[ApiSubscription, BSONObjectID]("subscriptionFieldsId", mongo,
    MongoFormatters.formatApiSubscription, ReactiveMongoFormats.objectIdFormats)
  with SubscriptionFieldsIdRepository {

  private implicit val format = new OFormat[ApiSubscription] {
    override def reads(json: JsValue): JsResult[ApiSubscription] = MongoFormatters.formatApiSubscription.reads(json)
    override def writes(s: ApiSubscription): JsObject = MongoFormatters.formatApiSubscription.writes(s)
  }

  override def indexes = Seq(
    createSingleFieldAscendingIndex(
      indexFieldKey = "id",
      indexName = Some("idIndex")
    ),
    createSingleFieldAscendingIndex(
      indexFieldKey = "fieldsId",
      indexName = Some("fieldsIdIndex")
    )
  )

  private def createSingleFieldAscendingIndex(indexFieldKey: String, indexName: Option[String],
                                              isUnique: Boolean = false, isBackground: Boolean = true): Index = {
    Index(
      key = Seq(indexFieldKey -> Ascending),
      name = indexName,
      unique = isUnique,
      background = isBackground
    )
  }

  override def save(subscription: ApiSubscription): Future[Unit] = {
    collection.find(Json.obj("id" -> subscription.id)).one[BSONDocument].flatMap {
      case Some(document) => collection.update(selector = BSONDocument("_id" -> document.get("_id")), update = subscription)
      case _ => collection.insert(subscription)
    }.map {
      writeResult => handleError(writeResult, s"Could not save subscription fields: $subscription")
    }
  }

  override def fetchById(id: String): Future[Option[ApiSubscription]] = {
    collection.find(Json.obj("id" -> id)).one[ApiSubscription]
  }
  override def fetchByFieldsId(fieldsId: UUID): Future[Option[ApiSubscription]] = {
    collection.find(Json.obj("fieldsId" -> fieldsId)).one[ApiSubscription]
  }

  override def delete(id: String): Future[Unit] = {
    collection.remove(Json.obj("id" -> id)).map {
      writeResult => handleError(writeResult, s"Could not delete subscription fields for id: $id")
    }
  }

  private def handleError[T](result: WriteResult, exceptionMsg: String): Unit = {
    result.errmsg match {
      case Some(errMsg) => throw new RuntimeException(s"""$exceptionMsg. $errMsg""")
      case _ => ()
    }
  }

}

object SubscriptionFieldsIdRepository extends MongoDbConnection {

  private lazy val repository = new SubscriptionFieldsIdMongoRepository

  def apply(): SubscriptionFieldsIdRepository = repository
}
