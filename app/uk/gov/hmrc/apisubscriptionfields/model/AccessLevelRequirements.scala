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

sealed trait DevhubAccessLevelRequirement
object DevhubAccessLevelRequirement {
   final val Default: DevhubAccessLevelRequirement = DevhubAccessLevel.Developer

   case object NoOne extends DevhubAccessLevelRequirement
}

sealed trait DevhubAccessLevel extends DevhubAccessLevelRequirement {
  def satisfiesRequirement(requirement: DevhubAccessLevelRequirement): Boolean = DevhubAccessLevel.satisfies(requirement)(this)
}
object DevhubAccessLevel {
  case object Developer extends DevhubAccessLevel
  case object Admininstator extends DevhubAccessLevel

  private def score(dl: DevhubAccessLevelRequirement): Int = dl match {
    case DevhubAccessLevelRequirement.NoOne => 3
    case Admininstator => 2
    case Developer => 1
  }

  def satisfies(requirement: DevhubAccessLevelRequirement)(actual: DevhubAccessLevel): Boolean = score(requirement) <= score(actual)
}

//
// Combinations for read only and read write
//

case class DevhubAccessLevelRequirements(
    readOnly: DevhubAccessLevelRequirement = DevhubAccessLevelRequirement.Default,
    readWrite: DevhubAccessLevelRequirement = DevhubAccessLevelRequirement.Default) {
  def satisfiesRead(dal: DevhubAccessLevel): Boolean = dal.satisfiesRequirement(readOnly) || dal.satisfiesRequirement(readWrite)
  def satisfiesWrite(dal: DevhubAccessLevel): Boolean = dal.satisfiesRequirement(readWrite)
}

object DevhubAccessLevelRequirements {
  final val Default = DevhubAccessLevelRequirements()
}

//
// Whole permission package
//

case class AccessLevelRequirements(devhub: DevhubAccessLevelRequirements)
object AccessLevelRequirements {
  final val Default = AccessLevelRequirements(devhub = DevhubAccessLevelRequirements.Default)
}