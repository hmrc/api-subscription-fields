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

import java.util.UUID

import javax.inject._
import uk.gov.hmrc.apisubscriptionfields.model._
import Types._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful
import cats.data.NonEmptyList
import uk.gov.hmrc.apisubscriptionfields.repository.SubscriptionFieldsRepository
import uk.gov.hmrc.http.HeaderCarrier
import cats.data.{NonEmptyList => NEL}

@Singleton
class UUIDCreator {
  def uuid(): UUID = UUID.randomUUID()
}

@Singleton
class SubscriptionFieldsService @Inject() (
                                          repository: SubscriptionFieldsRepository,
                                          uuidCreator: UUIDCreator,
                                          apiFieldDefinitionsService: ApiFieldDefinitionsService,
                                          pushPullNotificationService: PushPullNotificationService) {


  private def validate(fields: Fields, fieldDefinitions: NonEmptyList[FieldDefinition]): SubsFieldValidationResponse = {
    SubscriptionFieldsService.validateAgainstValidationRules(fieldDefinitions, fields) ++ SubscriptionFieldsService.validateFieldNamesAreDefined(fieldDefinitions,fields) match {
      case FieldErrorMap.empty => ValidSubsFieldValidationResponse
      case errs: FieldErrorMap => InvalidSubsFieldValidationResponse(errorResponses = errs)
    }
  }

  private def upsertSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, fields: Fields)
                                      (implicit hc: HeaderCarrier): Future[SuccessfulSubsFieldsUpsertResponse] = {
    val subscriptionFieldsId = SubscriptionFieldsId(uuidCreator.uuid())
    val subscriptionFields = SubscriptionFields(clientId, apiContext, apiVersion, subscriptionFieldsId, fields)

    repository.saveAtomic(subscriptionFields)
      .map(result => SuccessfulSubsFieldsUpsertResponse(result._1, result._2))
  }

  def handlePPNS(clientId: ClientId,
                 apiContext: ApiContext,
                 apiVersion: ApiVersion,
                 fieldDefinitions: NEL[FieldDefinition],
                 fields: Fields)(implicit hc: HeaderCarrier): Future[SubsFieldsUpsertResponse] = {
    val ppnsFieldDefinition: Option[FieldDefinition] = fieldDefinitions.find(_.`type` == FieldDefinitionType.PPNS_FIELD)

    ppnsFieldDefinition match {
      case Some(fieldDefinition)   =>
        val callBackUrl: Option[FieldValue] = fields.get(fieldDefinition.name).filterNot(_.isEmpty)
        val callBackResponse: Future[PPNSCallBackUrlValidationResponse] = callBackUrl match {
            case Some(fieldValue) => pushPullNotificationService.subscribeToPPNS(clientId, apiContext, apiVersion, fieldValue, fieldDefinition)
            case None => Future.successful(PPNSCallBackUrlSuccessResponse)
          }
        callBackResponse.flatMap {
          case PPNSCallBackUrlSuccessResponse =>  upsertSubscriptionFields(clientId, apiContext, apiVersion, fields)
          case PPNSCallBackUrlFailedResponse(error) => Future.successful(FailedPPNSSubsFieldsUpsertResponse(Map(fieldDefinition.name -> error)))
        }
      case None =>  upsertSubscriptionFields(clientId, apiContext, apiVersion, fields)
    }

  }

  def upsert(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion, fields: Fields)(implicit hc: HeaderCarrier): Future[SubsFieldsUpsertResponse] = {
    val foFieldDefinitions: Future[Option[NonEmptyList[FieldDefinition]]] = apiFieldDefinitionsService.get(apiContext, apiVersion).map(_.map(_.fieldDefinitions))

    foFieldDefinitions.flatMap( _ match {
      case None                   => successful(NotFoundSubsFieldsUpsertResponse)
      case Some(fieldDefinitions) => {
        validate(fields, fieldDefinitions) match {
          case ValidSubsFieldValidationResponse                       => handlePPNS(clientId, apiContext, apiVersion, fieldDefinitions, fields)
          case InvalidSubsFieldValidationResponse(fieldErrorMessages) => successful(FailedValidationSubsFieldsUpsertResponse(fieldErrorMessages))
        }
      }
    })
  }

  def delete(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Boolean] = {
    repository.delete(clientId, apiContext, apiVersion)
  }

  def delete(clientId: ClientId): Future[Boolean] = {
    repository.delete(clientId)
  }

  def get(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersion): Future[Option[SubscriptionFields]] = {
    for {
      fetch <- repository.fetch(clientId, apiContext, apiVersion)
    } yield fetch
  }

  def getBySubscriptionFieldId(subscriptionFieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFields]] = {
    for {
      fetch <- repository.fetchByFieldsId(subscriptionFieldsId)
    } yield fetch
  }

  def getByClientId(clientId: ClientId): Future[Option[BulkSubscriptionFieldsResponse]] = {
    (for {
      fields <- repository.fetchByClientId(clientId)
    } yield fields)
    .map {
      case Nil => None
      case fs  => Some(BulkSubscriptionFieldsResponse(subscriptions = fs))
    }
  }

  def getAll: Future[BulkSubscriptionFieldsResponse] = {
    (for {
      fields <- repository.fetchAll
    } yield fields)
    .map (BulkSubscriptionFieldsResponse(_))
  }

}

object SubscriptionFieldsService {
  import Types._

  // True - passed
  def validateAgainstGroup(group: ValidationGroup, value: FieldValue): Boolean = {
    group.rules.foldLeft(true)((acc, rule) => (acc && rule.validate(value)))
  }

  // Some is Some(error)
  def validateAgainstDefinition(fieldDefinition: FieldDefinition, value: FieldValue): Option[FieldError] = {
    fieldDefinition.validation .flatMap(group => if (validateAgainstGroup(group, value)) None else Some((fieldDefinition.name, group.errorMessage)))
  }

  def validateAgainstValidationRules(fieldDefinitions: NonEmptyList[FieldDefinition], fields: Fields): FieldErrorMap =
    fieldDefinitions
      .map(fd => validateAgainstDefinition(fd, fields.getOrElse(fd.name,"")))
      .foldLeft(FieldErrorMap.empty) {
        case (acc, None)     => acc
        case (acc, Some((name,msg))) => acc + (name -> msg)
      }

  def validateFieldNamesAreDefined(fieldDefinitions: NonEmptyList[FieldDefinition], fields: Fields): FieldErrorMap = {
    val illegalNames = fields.keySet -- (fieldDefinitions.map(_.name).toList)
    illegalNames.foldLeft(FieldErrorMap.empty)( (acc, name) => acc + (name -> "No Field Definition found for this Field"))
  }
}
