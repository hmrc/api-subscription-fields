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

import play.api.Logger
import play.api.libs.json.{JsObject, OWrites, Reads}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



trait MongoCrudHelper[T] extends MongoIndexCreator with MongoErrorHandler {

  val col: JSONCollection

  def save(entity: T, selector: JsObject)(implicit w: OWrites[T]): Future[Boolean] = {
    Logger.debug(s"[save] entity: $entity selector: $selector")
    col.update(selector = selector, update = entity, upsert = true).map {
      updateWriteResult => handleSaveError(updateWriteResult, s"Could not save entity: $entity")
    }
  }

  def getMany(selector: JsObject)(implicit r: Reads[T]):Future[List[T]] = {
    Logger.debug(s"[getMany] selector: $selector")
    col.find(selector).cursor[T](ReadPreference.primary).collect[List](
      Int.MaxValue, Cursor.FailOnError[List[T]]()
    )
  }

  def getOne(selector: JsObject)(implicit r: Reads[T]):Future[Option[T]] = {
    Logger.debug(s"[getOne] selector: $selector")
    col.find(selector).one[T]
  }

  def deleteOne(selector: JsObject): Future[Boolean] = {
    Logger.debug(s"[deleteOne] selector: $selector")
    col.remove(selector).map {
      writeResult => handleDeleteError(writeResult, s"Could not delete entity for selector: $selector")
    }
  }

}
