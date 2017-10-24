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
import play.api.Logger
import play.api.libs.json._
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.apisubscriptionfields.model.{ApiContext, ApiVersion}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[FieldsDefinitionMongoRepository])
trait FieldsDefinitionRepository {

  def save(fieldsDefinition: FieldsDefinition): Future[Boolean]

  def fetch(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[FieldsDefinition]]

  def fetchAll(): Future[List[FieldsDefinition]]

}

@Singleton
class FieldsDefinitionMongoRepository @Inject()(mongoDbProvider: MongoDbProvider)
  extends ReactiveRepository[FieldsDefinition, BSONObjectID]("fieldsDefinitions", mongoDbProvider.mongo,
    MongoFormatters.FieldsDefinitionJF, ReactiveMongoFormats.objectIdFormats)
  with FieldsDefinitionRepository
  with MongoIndexCreator
  with MongoErrorHandler {

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

  override def fetchAll(): Future[List[FieldsDefinition]] = {
    Logger.debug(s"[fetchAll]")
    collection.find(Json.obj()).cursor[FieldsDefinition](ReadPreference.primary).collect[List](
      Int.MaxValue, Cursor.FailOnError[List[FieldsDefinition]]()
    )
  }

  override def fetch(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[FieldsDefinition]] = {
    val selector = selectorForFieldsDefinition(apiContext, apiVersion)
    Logger.debug(s"[fetch] selector: $selector")
    collection.find(selector).one[FieldsDefinition]
  }

  override def save(fieldsDefinition: FieldsDefinition): Future[Boolean] = {
    collection.update(selector = selectorForFieldsDefinition(fieldsDefinition), update = fieldsDefinition, upsert = true).map {
      updateWriteResult =>
        handleSaveError(updateWriteResult, s"Could not save fields definition fields: $fieldsDefinition",
          updateWriteResult.upserted.nonEmpty
      )
    }
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
