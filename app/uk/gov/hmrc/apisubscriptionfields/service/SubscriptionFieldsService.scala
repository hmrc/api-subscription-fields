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

import javax.inject._
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

import cats.data.NonEmptyList
import cats.data.{NonEmptyList => NEL}
import cats.implicits._

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApiContext, ApiVersionNbr, ClientId}
import uk.gov.hmrc.apiplatform.modules.common.services.EitherTHelper
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.SubscriptionFieldsRepository

@Singleton
class SubscriptionFieldsService @Inject() (
    subscriptionFieldsRepository: SubscriptionFieldsRepository,
    apiFieldDefinitionsService: ApiFieldDefinitionsService,
    pushPullNotificationService: PushPullNotificationService
  )(implicit ec: ExecutionContext
  ) {

  def upsert(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, newFields: Fields)(implicit hc: HeaderCarrier): Future[SubsFieldsUpsertResponse] = {
    def findPpnsField(fieldDefinitions: NEL[FieldDefinition]): Option[FieldDefinition] = fieldDefinitions.find(_.`type` == FieldDefinitionType.PPNS_FIELD)

    def handleAnyPpnsSubscriptionRequired(
        clientId: ClientId,
        apiContext: ApiContext,
        apiVersion: ApiVersionNbr,
        fieldDefinitions: NEL[FieldDefinition],
        fields: Fields
      )(implicit
        hc: HeaderCarrier
      ): Future[Either[String, Unit]] = {

      findPpnsField(fieldDefinitions) match {
        case Some(fieldDefinition) =>
          val fieldName = fieldDefinition.name
          fields.get(fieldName) match {
            case Some(fieldValue) =>
              println(s"Calling subscribe '$fieldName' '$fieldValue'"); pushPullNotificationService.subscribeToPPNS(clientId, apiContext, apiVersion, fieldName, fieldValue)
            case None             => successful(Right(()))
          }
        case None                  => successful(Right(()))
      }
    }

    def validateFields(fields: Fields, fieldDefinitions: NonEmptyList[FieldDefinition]): Either[FieldErrorMap, Unit] = {
      SubscriptionFieldsService.validateAgainstValidationRules(fieldDefinitions, fields) ++ SubscriptionFieldsService.validateFieldNamesAreDefined(fieldDefinitions, fields) match {
        case FieldErrorMap.empty => Right(())
        case errs: FieldErrorMap => Left(errs)
      }
    }

    def translateValidateError(fieldErrorMessages: FieldErrorMap) = FailedValidationSubsFieldsUpsertResponse(fieldErrorMessages)

    def translatePpnsError(fieldDefinitions: NEL[FieldDefinition])(error: String) = {
      val fieldName          = findPpnsField(fieldDefinitions).get.name
      val fieldErrorMessages = Map(fieldName -> error)
      FailedValidationSubsFieldsUpsertResponse(fieldErrorMessages)
    }

    def upsertIfFieldsHaveChanged(anyExistingFields: Option[SubscriptionFields]) = {
      def upsertSubscriptionFields(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr, fields: Fields): Future[SuccessfulSubsFieldsUpsertResponse] = {
        subscriptionFieldsRepository
          .saveAtomic(clientId, apiContext, apiVersion, fields)
          .map(result => SuccessfulSubsFieldsUpsertResponse(result._1, result._2))
      }

      anyExistingFields match {
        case Some(existingFields) if (existingFields.fields == newFields) =>
          successful(SuccessfulSubsFieldsUpsertResponse(existingFields, false))
        case _                                                            =>
          println("Upsert fields")
          upsertSubscriptionFields(clientId, apiContext, apiVersion, newFields)
      }
    }

    (for {
      anyFieldDefinitions <- apiFieldDefinitionsService.get(apiContext, apiVersion).map(_.map(_.fieldDefinitions))
      _                    = println(anyFieldDefinitions)
      anyExistingFields   <- subscriptionFieldsRepository.fetch(clientId, apiContext, apiVersion)
      _                    = println(anyExistingFields)
    } yield (anyFieldDefinitions, anyExistingFields))
      .flatMap(_ match {
        case (None, Some(existingFields)) if (existingFields.fields == newFields) => successful(SuccessfulSubsFieldsUpsertResponse(existingFields, false))
        case (None, _)                                                            => successful(NotFoundSubsFieldsUpsertResponse)
        case (Some(fieldDefinitions), anyExistingFields)                          =>
          val E = EitherTHelper.make[FailedValidationSubsFieldsUpsertResponse]
          (
            for {
              _        <- E.fromEither(validateFields(newFields, fieldDefinitions).leftMap(translateValidateError))
              _         = println("validated")
              _        <- E.fromEitherF(handleAnyPpnsSubscriptionRequired(clientId, apiContext, apiVersion, fieldDefinitions, newFields)
                            .map(_.leftMap(translatePpnsError(fieldDefinitions))))
              _         = println("handle")
              response <- E.liftF(upsertIfFieldsHaveChanged(anyExistingFields))
            } yield response
          )
            .merge
      })
  }

  def delete(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr): Future[Boolean] = {
    subscriptionFieldsRepository.delete(clientId, apiContext, apiVersion)
  }

  def delete(clientId: ClientId): Future[Boolean] = {
    subscriptionFieldsRepository.delete(clientId)
  }

  def get(clientId: ClientId, apiContext: ApiContext, apiVersion: ApiVersionNbr): Future[Option[SubscriptionFields]] = {
    for {
      fetch <- subscriptionFieldsRepository.fetch(clientId, apiContext, apiVersion)
    } yield fetch
  }

  def getBySubscriptionFieldId(subscriptionFieldsId: SubscriptionFieldsId): Future[Option[SubscriptionFields]] = {
    for {
      fetch <- subscriptionFieldsRepository.fetchByFieldsId(subscriptionFieldsId)
    } yield fetch
  }

  def getByClientId(clientId: ClientId): Future[Option[BulkSubscriptionFieldsResponse]] = {
    (for {
      fields <- subscriptionFieldsRepository.fetchByClientId(clientId)
    } yield fields)
      .map {
        case Nil => None
        case fs  => Some(BulkSubscriptionFieldsResponse(subscriptions = fs))
      }
  }

  def getAll: Future[BulkSubscriptionFieldsResponse] = {
    (for {
      fields <- subscriptionFieldsRepository.fetchAll
    } yield fields)
      .map(BulkSubscriptionFieldsResponse(_))
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
    fieldDefinition.validation.flatMap(group => if (validateAgainstGroup(group, value)) None else Some((fieldDefinition.name, group.errorMessage)))
  }

  def validateAgainstValidationRules(fieldDefinitions: NonEmptyList[FieldDefinition], fields: Fields): FieldErrorMap =
    fieldDefinitions
      .map(fd => validateAgainstDefinition(fd, fields.getOrElse(fd.name, "")))
      .foldLeft(FieldErrorMap.empty) {
        case (acc, None)              => acc
        case (acc, Some((name, msg))) => acc + (name -> msg)
      }

  def validateFieldNamesAreDefined(fieldDefinitions: NonEmptyList[FieldDefinition], fields: Fields): FieldErrorMap = {
    val illegalNames = fields.keySet -- (fieldDefinitions.map(_.name).toList)
    illegalNames.foldLeft(FieldErrorMap.empty)((acc, name) => acc + (name -> "No Field Definition found for this Field"))
  }
}
