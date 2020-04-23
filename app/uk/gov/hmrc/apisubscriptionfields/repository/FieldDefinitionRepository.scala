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

package uk.gov.hmrc.apisubscriptionfields.repository

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import play.api.libs.json._
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.apisubscriptionfields.model._
import Types._

import scala.concurrent.Future

@ImplementedBy(classOf[ApiFieldDefinitionsMongoRepository])
trait ApiFieldDefinitionsRepository {

  def save(definitions: ApiFieldDefinitions): Future[(ApiFieldDefinitions, IsInsert)]

  def fetch(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[ApiFieldDefinitions]]

  def fetchAll(): Future[List[ApiFieldDefinitions]]

  def delete(apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean]
}

@Singleton
class ApiFieldDefinitionsMongoRepository @Inject() (mongoDbProvider: MongoDbProvider)
    extends ReactiveRepository[ApiFieldDefinitions, BSONObjectID]("fieldsDefinitions", mongoDbProvider.mongo, JsonFormatters.ApiFieldDefinitionsJF, ReactiveMongoFormats.objectIdFormats)
    with ApiFieldDefinitionsRepository
    with MongoCrudHelper[ApiFieldDefinitions] {

  override val mongoCollection: JSONCollection = collection

  override def indexes = Seq(
    createCompoundIndex(
      indexFieldMappings = Seq(
        "apiContext" -> IndexType.Ascending,
        "apiVersion" -> IndexType.Ascending
      ),
      indexName = Some("apiContext-apiVersion_index"),
      isUnique = true
    )
  )

  override def save(definitions: ApiFieldDefinitions): Future[(ApiFieldDefinitions, IsInsert)] = {
    import JsonFormatters.ApiFieldDefinitionsJF
    save(definitions, selectorFor(definitions))
  }

  override def fetch(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[ApiFieldDefinitions]] = {
    getOne(selectorFor(apiContext, apiVersion))
  }

  override def fetchAll(): Future[List[ApiFieldDefinitions]] = {
    getMany(Json.obj())
  }

  override def delete(apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    deleteOne(selectorFor(apiContext, apiVersion))
  }

  private def selectorFor(apiContext: ApiContext, apiVersion: ApiVersion): JsObject = {
    selector(apiContext.value, apiVersion.value)
  }

  private def selectorFor(fd: ApiFieldDefinitions): JsObject = {
    selector(fd.apiContext, fd.apiVersion)
  }

  private def selector(apiContext: String, apiVersion: String): JsObject = {
    Json.obj(
      "apiContext" -> apiContext,
      "apiVersion" -> apiVersion
    )
  }
}
