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
import uk.gov.hmrc.apisubscriptionfields.repository.{FieldsDefinition, FieldsDefinitionRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.data.NonEmptyList
import Types._

@Singleton
class FieldsDefinitionService @Inject() (repository: FieldsDefinitionRepository) {

  def upsert(apiContext: ApiContext, apiVersion: ApiVersion, fieldDefinitions: NonEmptyList[FieldDefinition]): Future[(FieldsDefinitionResponse, IsInsert)] = {
    val fieldsDefinition = FieldsDefinition(apiContext.value, apiVersion.value, fieldDefinitions)
    repository.save(fieldsDefinition).map {
      case (fd: FieldsDefinition, inserted: IsInsert) => (asResponse(fd), inserted)
    }
  }

  def delete(apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    repository.delete(apiContext, apiVersion)
  }

  def get(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[FieldsDefinitionResponse]] = {
    for {
      fetch <- repository.fetch(apiContext, apiVersion)
    } yield fetch.map(asResponse)
  }

  def getAll: Future[BulkFieldsDefinitionsResponse] = {
    (for {
      defs <- repository.fetchAll()
    } yield defs.map(asResponse)) map (BulkFieldsDefinitionsResponse(_))
  }

  private def asResponse(fieldsDefinition: FieldsDefinition): FieldsDefinitionResponse = {
    FieldsDefinitionResponse(fieldsDefinition.apiContext, fieldsDefinition.apiVersion, fieldsDefinition.fieldDefinitions)
  }
}
