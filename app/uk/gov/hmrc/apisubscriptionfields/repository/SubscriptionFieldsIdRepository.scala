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

import play.api.libs.json._
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.apisubscriptionfields.model.{ApiSubscription, MongoFormatters}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SubscriptionFieldsIdRepository extends Repository[ApiSubscription, BSONObjectID] {

  def save(subscription: ApiSubscription): Future[ApiSubscription]

  def fetch(id: String): Future[Option[ApiSubscription]]

  def delete(id: String): Future[Int]
}

class SubscriptionFieldsIdMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[ApiSubscription, BSONObjectID]("subscriptionFieldsId", mongo,
    MongoFormatters.formatApiSubscription, ReactiveMongoFormats.objectIdFormats)
  with SubscriptionFieldsIdRepository {

  private implicit val format = new OFormat[ApiSubscription] {
    override def reads(json: JsValue): JsResult[ApiSubscription] = MongoFormatters.formatApiSubscription.reads(json)
    override def writes(s: ApiSubscription): JsObject = MongoFormatters.formatApiSubscription.writes(s)
  }

  ensureIndex("subscriptionFieldsId", "subscriptionFieldsIdIndex")

  private def ensureIndex(field: String, indexName: String, isUnique: Boolean = true): Future[Boolean] = {
    collection.indexesManager.ensure(Index(Seq(field -> IndexType.Ascending),
      name = Some(indexName), unique = isUnique, background = true))
  }

  override def save(subscription: ApiSubscription): Future[ApiSubscription] = {
    collection.find(Json.obj("id" -> subscription.id)).one[BSONDocument].flatMap {
      case Some(document) => collection.update(selector = BSONDocument("_id" -> document.get("_id")), update = subscription)
      case None => collection.insert(subscription)
    }.map {
      // TODO: extract code for handling the errors
      error => error.errmsg match {
        case Some(errorMsg) => throw new RuntimeException(s"""Could not save subscription fields: $subscription. $errorMsg""")
        case None => subscription
      }
    }
  }

  override def fetch(id: String): Future[Option[ApiSubscription]] = {
    collection.find(Json.obj("id" -> id)).one[ApiSubscription]
  }

  override def delete(id: String): Future[Int] = ???
}

object SubscriptionFieldsIdRepository extends MongoDbConnection {

  private lazy val repository = new SubscriptionFieldsIdMongoRepository

  def apply(): SubscriptionFieldsIdRepository = repository
}
