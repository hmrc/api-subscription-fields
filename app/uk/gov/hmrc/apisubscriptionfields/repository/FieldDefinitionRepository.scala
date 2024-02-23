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

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.ImplementedBy
import org.apache.pekko.stream.Materializer
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}

import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.utils.ApplicationLogger

@ImplementedBy(classOf[ApiFieldDefinitionsMongoRepository])
trait ApiFieldDefinitionsRepository {

  def save(definitions: ApiFieldDefinitions): Future[(ApiFieldDefinitions, IsInsert)]

  def fetch(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Future[Option[ApiFieldDefinitions]]

  def fetchAll(): Future[List[ApiFieldDefinitions]]

  def delete(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Future[Boolean]
}

@Singleton
class ApiFieldDefinitionsMongoRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext, val mat: Materializer)
    extends PlayMongoRepository[ApiFieldDefinitions](
      collectionName = "fieldsDefinitions",
      mongoComponent = mongo,
      domainFormat = JsonFormatters.ApiFieldDefinitionsJF,
      extraCodecs = Seq(
        Codecs.playFormatCodec(JsonFormatters.ApiFieldDefinitionsJF),
        Codecs.playFormatCodec(JsonFormatters.ValidationJF)
      ),
      indexes = Seq(
        IndexModel(
          ascending(List("apiContext", "apiVersionNbr"): _*),
          IndexOptions()
            .name("apiContext-apiVersion_index")
            .background(true)
            .unique(true)
        )
      )
    )
    with ApiFieldDefinitionsRepository
    with ApplicationLogger {

  def save(definitions: ApiFieldDefinitions): Future[(ApiFieldDefinitions, IsInsert)] = {
    val query = and(equal("apiContext", Codecs.toBson(definitions.apiContext.value)), equal("apiVersionNbr", Codecs.toBson(definitions.apiVersionNbr.value)))

    collection.find(query).headOption().flatMap {
      case Some(_: ApiFieldDefinitions) =>
        for {
          updatedDefinitions <- collection
                                  .replaceOne(
                                    filter = query,
                                    replacement = definitions
                                  )
                                  .toFuture()
                                  .map(_ => definitions)
        } yield (updatedDefinitions, false)

      case None =>
        for {
          newDefinitions <- collection.insertOne(definitions).toFuture().map(_ => definitions)
        } yield (newDefinitions, true)
    }
  }

  override def fetch(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Future[Option[ApiFieldDefinitions]] = {
    collection.find(Filters.and(equal("apiContext", Codecs.toBson(apiContext.value)), equal("apiVersionNbr", Codecs.toBson(apiVersionNbr.value)))).headOption()
  }

  override def fetchAll(): Future[List[ApiFieldDefinitions]] = {
    collection.find().toFuture().map(_.toList)
  }

  override def delete(apiContext: ApiContext, apiVersionNbr: ApiVersionNbr): Future[Boolean] = {
    collection
      .deleteOne(Filters.and(equal("apiContext", Codecs.toBson(apiContext.value)), equal("apiVersionNbr", Codecs.toBson(apiVersionNbr.value))))
      .toFuture()
      .map(_.getDeletedCount > 0)
  }
}
