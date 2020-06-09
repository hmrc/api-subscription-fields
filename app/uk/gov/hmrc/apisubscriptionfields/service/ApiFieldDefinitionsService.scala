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

package uk.gov.hmrc.apisubscriptionfields.service

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.apisubscriptionfields.model._
import Types._
import scala.concurrent.Future
import cats.data.NonEmptyList
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.apisubscriptionfields.repository.ApiFieldDefinitionsRepository

@Singleton
class ApiFieldDefinitionsService @Inject() (repository: ApiFieldDefinitionsRepository)(implicit ec: ExecutionContext) {

  def upsert(apiContext: ApiContext, apiVersion: ApiVersion, fieldDefinitions: NonEmptyList[FieldDefinition]): Future[(ApiFieldDefinitionsResponse, IsInsert)] = {
    val definitions = ApiFieldDefinitions(apiContext, apiVersion, fieldDefinitions)
    repository.save(definitions).map {
      case (fd: ApiFieldDefinitions, inserted: IsInsert) => (asResponse(fd), inserted)
    }
  }

  def delete(apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    repository.delete(apiContext, apiVersion)
  }

  def get(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[ApiFieldDefinitionsResponse]] = {
    for {
      fetch <- repository.fetch(apiContext, apiVersion)
    } yield fetch.map(asResponse)
  }

  def getAll: Future[BulkApiFieldDefinitionsResponse] = {
    (for {
      defs <- repository.fetchAll()
    } yield defs.map(asResponse)) map (BulkApiFieldDefinitionsResponse(_))
  }

  private def asResponse(definitions: ApiFieldDefinitions): ApiFieldDefinitionsResponse = {
    ApiFieldDefinitionsResponse(definitions.apiContext, definitions.apiVersion, definitions.fieldDefinitions)
  }
}
