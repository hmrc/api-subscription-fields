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

sealed trait GatekeeperLevel
object GatekeeperLevel {
  case object NoOne extends GatekeeperLevel
  case object User extends GatekeeperLevel
  case object SuperUser extends GatekeeperLevel
  case object Admin extends GatekeeperLevel

  final val Default = User
}
case class GatekeeperAccessLevels(readOnly: GatekeeperLevel = GatekeeperLevel.Default, readWrite: GatekeeperLevel= GatekeeperLevel.Default)
object GatekeeperAccessLevels {
  final val Default = GatekeeperAccessLevels()
}

sealed trait DevhubLevel
object DevhubLevel {
  case object NoOne extends DevhubLevel
  case object Developer extends DevhubLevel
  case object Admininstator extends DevhubLevel

 final val Default = Developer
}
case class DevhubAccessLevels(readOnly: DevhubLevel = DevhubLevel.Default, readWrite: DevhubLevel = DevhubLevel.Default)
object DevhubAccessLevels {
  final val Default = DevhubAccessLevels()
}

case class AccessLevels(devhub: Option[DevhubAccessLevels] = None, gatekeeper: Option[GatekeeperAccessLevels] = None)
