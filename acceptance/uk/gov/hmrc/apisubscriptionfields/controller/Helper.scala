package uk.gov.hmrc.apisubscriptionfields.controller

import uk.gov.hmrc.apisubscriptionfields.model.{Types, FieldDefinition}
import cats.data.{NonEmptyList => NEL}

trait Helper {
  def makeSubscriptionFieldsRequest(fields: Types.Fields): SubscriptionFieldsRequest = SubscriptionFieldsRequest(fields)
  def makeFieldDefinitionsRequest(definitions: NEL[FieldDefinition]): FieldDefinitionsRequest = FieldDefinitionsRequest(definitions)
}

object Helper extends Helper
