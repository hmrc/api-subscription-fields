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

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.libs.json._
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.apisubscriptionfields.model.{ApiContext, ApiVersion}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.Future

@ImplementedBy(classOf[FieldsDefinitionMongoRepository])
trait FieldsDefinitionRepository {

  def save(fieldsDefinition: FieldsDefinition): Future[(FieldsDefinition, Boolean)]

  def fetch(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[FieldsDefinition]]
  def fetchAll(): Future[List[FieldsDefinition]]

  def delete(apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean]
}

@Singleton
class FieldsDefinitionMongoRepository @Inject()(mongoDbProvider: MongoDbProvider)
  extends ReactiveRepository[FieldsDefinition, BSONObjectID]("fieldsDefinitions", mongoDbProvider.mongo,
    MongoFormatters.FieldsDefinitionJF, ReactiveMongoFormats.objectIdFormats)
  with FieldsDefinitionRepository
  with MongoCrudHelper[FieldsDefinition] {

  override val mongoCollection: JSONCollection = mongoCollection
  private implicit val format = MongoFormatters.FieldsDefinitionJF

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

  override def save(fieldsDefinition: FieldsDefinition): Future[(FieldsDefinition, Boolean)] = {
    save(fieldsDefinition, selectorForFieldsDefinition(fieldsDefinition))
  }

  override def fetch(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[FieldsDefinition]] = {
    getOne(selectorForFieldsDefinition(apiContext, apiVersion))
  }

  override def fetchAll(): Future[List[FieldsDefinition]] = {
    getMany(Json.obj())
  }

  override def delete(apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    deleteOne(selectorForFieldsDefinition(apiContext, apiVersion))
  }

  private def selectorForFieldsDefinition(apiContext: ApiContext, apiVersion: ApiVersion): JsObject = {
    selector(apiContext.value, apiVersion.value)
  }

  private def selectorForFieldsDefinition(fd: FieldsDefinition): JsObject = {
    selector(fd.apiContext, fd.apiVersion)
  }

  private def selector(apiContext: String, apiVersion: String): JsObject = {
    Json.obj(
      "apiContext" -> apiContext,
      "apiVersion" -> apiVersion
    )
  }
}
