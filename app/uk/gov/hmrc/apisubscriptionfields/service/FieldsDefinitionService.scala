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

package uk.gov.hmrc.apisubscriptionfields.service

import javax.inject.{Inject, Singleton}

import play.api.Logger
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.{FieldsDefinition, FieldsDefinitionRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FieldsDefinitionService @Inject() (repository: FieldsDefinitionRepository) {

  def upsert(apiContext: ApiContext, apiVersion: ApiVersion, fields: Seq[FieldDefinition]): Future[Boolean] = {
    Logger.debug(s"[upsert] ApiContext: $apiContext, ApiVersion: $apiVersion")
    repository.save(FieldsDefinition(apiContext.value, apiVersion.value, fields))
  }

  def get(apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[FieldsDefinitionResponse]] = {
    Logger.debug(s"[get] ApiContext: $apiContext, ApiVersion: $apiVersion")
    for {
      fetch <- repository.fetch(apiContext, apiVersion)
    } yield fetch.map(asResponse)
  }

  def getAll: Future[FieldsDefinitionResponse] = {
    Logger.debug(s"[getAllFieldDefinitions]")
    repository.fetchAll() map { defs => defs.flatMap { _.fieldDefinitions } } map (FieldsDefinitionResponse(_))
  }

  private def asResponse(fieldsDefinition: FieldsDefinition): FieldsDefinitionResponse = {
    FieldsDefinitionResponse(fields = fieldsDefinition.fieldDefinitions)
  }
}
