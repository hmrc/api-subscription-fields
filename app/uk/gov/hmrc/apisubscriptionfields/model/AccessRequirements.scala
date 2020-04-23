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

package uk.gov.hmrc.apisubscriptionfields.model

sealed trait DevhubAccessRequirement
object DevhubAccessRequirement {
   final val Default: DevhubAccessRequirement = DevhubAccessLevel.Developer

   case object NoOne extends DevhubAccessRequirement
}

case class DevhubAccessRequirements(
    readOnly: DevhubAccessRequirement = DevhubAccessRequirement.Default,
    readWrite: DevhubAccessRequirement = DevhubAccessRequirement.Default) {
  def satisfiesRead(dal: DevhubAccessLevel): Boolean = dal.satisfiesRequirement(readOnly) || dal.satisfiesRequirement(readWrite)
  def satisfiesWrite(dal: DevhubAccessLevel): Boolean = dal.satisfiesRequirement(readWrite)
}

object DevhubAccessRequirements {
  final val Default = DevhubAccessRequirements()
}

case class AccessRequirements(devhub: DevhubAccessRequirements)
object AccessRequirements {
  final val Default = AccessRequirements(devhub = DevhubAccessRequirements.Default)
}


sealed trait DevhubAccessLevel extends DevhubAccessRequirement {
  def satisfiesRequirement(requirement: DevhubAccessRequirement): Boolean = DevhubAccessLevel.satisfies(requirement)(this)
}
object DevhubAccessLevel {
  case object Developer extends DevhubAccessLevel
  case object Admininstator extends DevhubAccessLevel

  private def score(dl: DevhubAccessRequirement): Int = dl match {
    case DevhubAccessRequirement.NoOne => 3
    case Admininstator => 2
    case Developer => 1
  }

  def satisfies(requirement: DevhubAccessRequirement)(actual: DevhubAccessLevel): Boolean = score(requirement) <= score(actual)
}
