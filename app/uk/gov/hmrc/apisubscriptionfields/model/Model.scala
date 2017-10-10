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

package uk.gov.hmrc.apisubscriptionfields.model

import java.util.UUID

import scala.annotation.tailrec

case class AppId(value: String) extends AnyVal

case class ApiContext(value: String) extends AnyVal

case class ApiVersion(value: String) extends AnyVal

case class SubscriptionFieldsId(value: UUID) extends AnyVal

object SubscriptionIdentifier {
  private val Separator = "##"
  private type SeparatorType = String

  def decode(text:String): Option[SubscriptionIdentifier] = {
    def findSeparator(separatorToFind: SeparatorType) : SeparatorType = {
      if (text.split(separatorToFind).length > 3)
        findSeparator(separatorToFind+Separator)
      else
        separatorToFind
    }

    val parts = text.split(findSeparator(Separator))
    Some(SubscriptionIdentifier(AppId(parts(0)), ApiContext(parts(1)), ApiVersion(parts(2))))
  }
}

case class SubscriptionIdentifier(applicationId: AppId, apiContext: ApiContext, apiVersion: ApiVersion) {
  import SubscriptionIdentifier._

  def encode(): String = {
    @tailrec
    def findSeparator(text: String)(separatorToFind: SeparatorType): SeparatorType =
      if (!text.contains(separatorToFind))
        separatorToFind
      else
        findSeparator(text)(separatorToFind+Separator)

    val longestSeparator: SeparatorType = findSeparator(apiVersion.value)(findSeparator(apiContext.value)(Separator))
    s"${applicationId.value}$longestSeparator${apiContext.value}$longestSeparator${apiVersion.value}"
  }
}
