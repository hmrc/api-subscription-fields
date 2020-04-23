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

import org.scalatest.WordSpec
import org.scalatest.Matchers
import uk.gov.hmrc.apisubscriptionfields.model.DevhubAccessLevel.{Developer,Admininstator}

class AccessRequirementsSpec extends WordSpec with Matchers {

  private val allLevels: List[DevhubAccessLevel] = List(Developer, Admininstator)

  "DevhubLevelRequirement" should {

    "DevhubLevelRequirement Developer" should {
      val requirement: DevhubAccessRequirement = Developer

      "allow all devhub levels to satisfy the requirement" in {
        allLevels.foreach(at => at.satisfiesRequirement(requirement) shouldBe true)
      }
    }

    "DevhubLevelRequirement Admin" should {
      val requirement: DevhubAccessRequirement = Admininstator

      "allow only admin to satisfy the requirement" in {
        Admininstator.satisfiesRequirement(requirement) shouldBe true
      }

      "disallow developer to satisfy the requirement" in {
        Developer.satisfiesRequirement(requirement) shouldBe false
      }
    }

    "DevhubLevelRequirement NoOne" should {
      val requirement: DevhubAccessRequirement = DevhubAccessRequirement.NoOne

      "not allow any devhub level to satisfy the requirement" in {
        allLevels.foreach(at => at.satisfiesRequirement(requirement) shouldBe false)
      }
    }
  }
}
