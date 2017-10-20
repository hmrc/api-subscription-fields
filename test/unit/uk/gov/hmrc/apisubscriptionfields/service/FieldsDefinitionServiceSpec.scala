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

package unit.uk.gov.hmrc.apisubscriptionfields.service

import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.apisubscriptionfields.model.FieldsDefinitionIdentifier
import uk.gov.hmrc.apisubscriptionfields.repository.FieldsDefinitionRepository
import uk.gov.hmrc.apisubscriptionfields.service.FieldsDefinitionService
import uk.gov.hmrc.play.test.UnitSpec
import util.FieldsDefinitionTestData

import scala.concurrent.Future

class FieldsDefinitionServiceSpec extends UnitSpec with FieldsDefinitionTestData with MockFactory {

  private val mockFieldsDefinitionRepository = mock[FieldsDefinitionRepository]
  private val service = new FieldsDefinitionService(mockFieldsDefinitionRepository)

  "A FieldsDefinitionService" should {
    "return None when no entry exist in the repo when get by identifier is called" in {
      (mockFieldsDefinitionRepository fetchById (_: FieldsDefinitionIdentifier)) expects FakeFieldsDefinitionIdentifier returns None

      val result = await(service.get(FakeFieldsDefinitionIdentifier))

      result shouldBe None
    }

    "return Some when entry exists in the repo when get by identifier is called" in {
      (mockFieldsDefinitionRepository fetchById (_: FieldsDefinitionIdentifier)) expects FakeFieldsDefinitionIdentifier returns Some(FakeFieldsDefinition)

      val result = await(service.get(FakeFieldsDefinitionIdentifier))

      result shouldBe Some(FakeFieldsDefinitionResponse)
    }

    "return false if save is for an existing fields definition" in {
      (mockFieldsDefinitionRepository save _) expects FakeFieldsDefinition returns false

      val result = await(service.upsert(FakeFieldsDefinitionIdentifier, FakeFieldsDefinitions))

      result shouldBe false
    }

    "return true if save is for a new fields definition" in {
      (mockFieldsDefinitionRepository save _) expects FakeFieldsDefinition returns true

      val result = await(service.upsert(FakeFieldsDefinitionIdentifier, FakeFieldsDefinitions))

      result shouldBe true
    }

    "propagate Failure in repository for upsert" in {
      (mockFieldsDefinitionRepository save _) expects * returns Future.failed(emulatedFailure)

      val caught = intercept[EmulatedFailure] {
        await(service.upsert(FakeFieldsDefinitionIdentifier, FakeFieldsDefinitions))
      }

      caught shouldBe emulatedFailure
    }

  }

}
