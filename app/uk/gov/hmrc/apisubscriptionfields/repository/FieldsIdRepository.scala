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

import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.apisubscriptionfields.model.{ApiSubscription, MongoFormat}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FieldsIdRepository extends Repository[ApiSubscription, BSONObjectID] {

  def save(subscription: ApiSubscription)

  def fetch(id: String)

  def delete(id: String)
}

class FieldsIdMongoRepository(mongo: () => DB)
  extends ReactiveRepository[ApiSubscription, BSONObjectID]("fieldsId", mongo, MongoFormat.formatApiSubscription, ReactiveMongoFormats.objectIdFormats)
    with FieldsIdRepository {

  ensureIndex("fieldsId", "fieldsIdIndex")

  private def ensureIndex(field: String, indexName: String, isUnique: Boolean = true): Future[Boolean] = {
    collection.indexesManager.ensure(Index(Seq(field -> IndexType.Ascending),
      name = Some(indexName), unique = isUnique, background = true))
  }

  override def save(subscription: ApiSubscription): Unit = ???

  override def fetch(id: String): Unit = ???

  override def delete(id: String): Unit = ???
}
