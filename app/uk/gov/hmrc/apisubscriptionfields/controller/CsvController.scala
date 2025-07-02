/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.apisubscriptionfields.controller

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.http.HeaderNames
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.apiplatform.modules.common.domain.models._

import uk.gov.hmrc.apisubscriptionfields.model.SubscriptionFields
import uk.gov.hmrc.apisubscriptionfields.model.Types.{FieldName, FieldValue}
import uk.gov.hmrc.apisubscriptionfields.service.SubscriptionFieldsService
import uk.gov.hmrc.apisubscriptionfields.utils.CsvHelper.ColumnDefinition
import uk.gov.hmrc.apisubscriptionfields.utils.{ApplicationLogger, CsvHelper}

@Singleton
class CsvController @Inject() (val controllerComponents: ControllerComponents, service: SubscriptionFieldsService)(implicit ec: ExecutionContext)
    extends CommonController
    with ApplicationLogger {

  def csv() = Action.async {
    case class FlattenedSubscriptionFieldValue(clientId: ClientId, context: ApiContext, versionNbr: ApiVersionNbr, name: FieldName)

    val columnDefinitions: Seq[ColumnDefinition[FlattenedSubscriptionFieldValue]] = Seq(
      ColumnDefinition("Environment", (_ => Environment.PRODUCTION.toString())),
      ColumnDefinition("ClientId", (data => data.clientId.value)),
      ColumnDefinition("ApiContext", (data => data.context.value)),
      ColumnDefinition("ApiVersionNbr", (data => data.versionNbr.value)),
      ColumnDefinition("FieldName", (data => data.name.value))
    )

    def flattendFieldValues(subscriptionFieldValues: Seq[SubscriptionFields]): Seq[FlattenedSubscriptionFieldValue] = {
      subscriptionFieldValues.flatMap(allsubscriptionFieldValues => {
        allsubscriptionFieldValues.fields.map { fieldValue: (FieldName, FieldValue) =>
          {
            val fieldName = fieldValue._1
            FlattenedSubscriptionFieldValue(allsubscriptionFieldValues.clientId, allsubscriptionFieldValues.apiContext, allsubscriptionFieldValues.apiVersion, fieldName)
          }
        }
      })
    }

    service.getAll().map(allFieldsValues => {

      val sortedAndFlattenedFields = flattendFieldValues(allFieldsValues.subscriptions)
        .sortBy(x => (x.clientId.value, x.context, x.versionNbr, x.name.value))

      Ok(CsvHelper.toCsvString(columnDefinitions, sortedAndFlattenedFields)).withHeaders(HeaderNames.CONTENT_TYPE -> "text/csv")
    })
  }
}
