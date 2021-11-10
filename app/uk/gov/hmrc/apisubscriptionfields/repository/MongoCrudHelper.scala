/*
 * Copyright 2021 HM Revenue & Customs
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
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import reactivemongo.api.Cursor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.apisubscriptionfields.model.Types.IsInsert

trait MongoCrudHelper[T] extends MongoIndexCreator with MongoErrorHandler {

  protected val mongoCollection: JSONCollection

  def saveAtomic(selector: JsObject, updateOperations: JsObject)(implicit w: OFormat[T]): Future[(T, IsInsert)] = {
    val updateOp = mongoCollection.updateModifier(
      update = updateOperations,
      fetchNewObject = true,
      upsert = true
    )

    mongoCollection.findAndModify(selector, updateOp).map { findAndModifyResult =>
      val maybeTuple: Option[(T, IsInsert)] = for {
        value <- findAndModifyResult.value
        updateLastError <- findAndModifyResult.lastError
      } yield (value.as[T], !updateLastError.updatedExisting)

      maybeTuple.fold[(T, IsInsert)] {
        handleError(selector, findAndModifyResult)
      }(tuple => tuple)
    }
  }

  private def handleError(selector: JsObject, findAndModifyResult: mongoCollection.BatchCommands.FindAndModifyCommand.FindAndModifyResult) = {
    val error = s"Error upserting database for $selector."
    appLogger.error(s"$error lastError: ${findAndModifyResult.lastError}")
    throw new RuntimeException(error)
  }

  def save(entity: T, selector: JsObject)(implicit w: OWrites[T]): Future[(T, IsInsert)] = {
    mongoCollection.update(ordered=false).one(selector, entity, upsert = true).map { updateWriteResult => (entity, handleSaveError(updateWriteResult, s"Could not save entity: $entity")) }
  }

  def getMany(selector: JsObject)(implicit r: Reads[T]): Future[List[T]] = {
    mongoCollection.find[JsObject, JsObject](selector, None).cursor[T]().collect[List](Int.MaxValue, Cursor.FailOnError[List[T]]())
  }

  def getOne(selector: JsObject)(implicit r: Reads[T]): Future[Option[T]] = {
    mongoCollection.find[JsObject, JsObject](selector, None).one[T]
  }

  def deleteOne(selector: JsObject): Future[Boolean] = {
    mongoCollection.delete(ordered=false).one(selector, limit = Some(1)).map(handleDeleteError(_, s"Could not delete entity for selector: $selector"))
  }

  def deleteMany(selector: JsObject): Future[Boolean] = {
    mongoCollection.delete().one(selector).map(handleDeleteError(_, s"Could not delete entity for selector: $selector"))
  }
}
