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
import uk.gov.hmrc.apisubscriptionfields.model.DevhubAccessLevel._
import uk.gov.hmrc.apisubscriptionfields.model.DevhubAccessRequirement._

class AccessRequirementsSpec extends WordSpec with Matchers {

  "DevhubRequirement" should {

    "Developer" should {
      "satisfy the Anyone requirement" in {
        Developer.satisfiesRequirement(Anyone) shouldBe true
      }
      "not satisfy the AdminOnly requirement" in {
        Developer.satisfiesRequirement(AdminOnly) shouldBe false
      }
      "not satisfy the NoOne requirement" in {
        Developer.satisfiesRequirement(NoOne) shouldBe false
      }
    }

    "Admin" should {
      "satisfy the Anyone requirement" in {
        Admininstator.satisfiesRequirement(Anyone) shouldBe true
      }
      "satisfy the AdminOnly requirement" in {
        Admininstator.satisfiesRequirement(AdminOnly) shouldBe true
      }
      "not satisfy the NoOne requirement" in {
        Admininstator.satisfiesRequirement(NoOne) shouldBe false
      }
    }
  }

  "DevhubAccessRequirements" should {

    "a producer team wants to restrict collaborators to only view but not change a field" in {
      val dar = DevhubAccessRequirements(read = DevhubAccessRequirement.Default, write = NoOne)

      dar.satisfiesRead(Developer) shouldBe true
      dar.satisfiesRead(Admininstator) shouldBe true

      dar.satisfiesWrite(Developer) shouldBe false
      dar.satisfiesWrite(Admininstator) shouldBe false
    }

    "a producer team wants to restrict Developers from even viewing a field but allow administrators to read and write" in {
      val dar = DevhubAccessRequirements(read = AdminOnly, write = AdminOnly)

      dar.satisfiesRead(Developer) shouldBe false
      dar.satisfiesWrite(Developer) shouldBe false

      dar.satisfiesRead(Admininstator) shouldBe true
      dar.satisfiesWrite(Admininstator) shouldBe true
    }

    "a producer team wants to restrict Developers to viewing a field but allow administrators to read and write" in {
      val dar = DevhubAccessRequirements(read = Anyone, write = AdminOnly)

      dar.satisfiesRead(Developer) shouldBe true
      dar.satisfiesWrite(Developer) shouldBe false

      dar.satisfiesRead(Admininstator) shouldBe true
      dar.satisfiesWrite(Admininstator) shouldBe true
    }

    "a producer team wants to allow anyone to view and change a field" in {
      val dar = DevhubAccessRequirements(read = Anyone)

      dar.satisfiesRead(Developer) shouldBe true
      dar.satisfiesWrite(Developer) shouldBe true

      dar.satisfiesRead(Admininstator) shouldBe true
      dar.satisfiesWrite(Admininstator) shouldBe true
    }
  }
}
