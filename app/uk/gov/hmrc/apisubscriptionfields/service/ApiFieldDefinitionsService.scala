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

package uk.gov.hmrc.apisubscriptionfields.service

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import cats.data.NonEmptyList

import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.ApiFieldDefinitionsRepository

@Singleton
class ApiFieldDefinitionsService @Inject() (repository: ApiFieldDefinitionsRepository)(implicit ec: ExecutionContext) {

  def upsert(apiContext: ApiContext, apiVersion: ApiVersion, fieldDefinitions: NonEmptyList[FieldDefinition]): Future[(ApiFieldDefinitions, IsInsert)] = {
    val definitions = ApiFieldDefinitions(apiContext, apiVersion, fieldDefinitions)
    repository.save(definitions)
  }

  def delete(apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    repository.delete(apiContext, apiVersion)
  }

  def get(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[ApiFieldDefinitions]] = {
    for {
      fetch <- repository.fetch(apiContext, apiVersion)
    } yield fetch
  }

  def getAll: Future[BulkApiFieldDefinitionsResponse] = {
    (for {
      defs <- repository.fetchAll()
    } yield defs) map (BulkApiFieldDefinitionsResponse(_))
  }
}
