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
import uk.gov.hmrc.apisubscriptionfields.model.{ApiContext, ApiVersion, BulkFieldsDefinitionsResponse, FieldsDefinitionResponse}
import uk.gov.hmrc.apisubscriptionfields.repository.FieldsDefinitionRepository
import uk.gov.hmrc.apisubscriptionfields.service.FieldsDefinitionService
import uk.gov.hmrc.play.test.UnitSpec
import util.FieldsDefinitionTestData

import scala.concurrent.Future

class FieldsDefinitionServiceSpec extends UnitSpec with FieldsDefinitionTestData with MockFactory {

  private val mockFieldsDefinitionRepository = mock[FieldsDefinitionRepository]
  private val service = new FieldsDefinitionService(mockFieldsDefinitionRepository)

  "getAll" should {
    "return an empty list when there are no records in the database collection" in {
      (mockFieldsDefinitionRepository.fetchAll _).expects().returns(List())

      val result = await(service.getAll)

      result shouldBe BulkFieldsDefinitionsResponse(List())
    }

    "return a list of all entries" in {
      val fd1 = createFieldsDefinition(apiContext = "api-1", fieldDefinitions = Seq(FakeFieldDefinitionUrl))
      val fd2 = createFieldsDefinition(apiContext = "api-2", fieldDefinitions = Seq(FakeFieldDefinitionString))

      (mockFieldsDefinitionRepository.fetchAll _).expects().returns(List(fd1, fd2))

      val result = await(service.getAll)

      val expectedResponse = BulkFieldsDefinitionsResponse(apis = Seq(
        FieldsDefinitionResponse("api-1", fakeRawVersion, Seq(FakeFieldDefinitionUrl)),
        FieldsDefinitionResponse("api-2", fakeRawVersion, Seq(FakeFieldDefinitionString))))
      result shouldBe expectedResponse
    }
  }

  "get" should {
    "return None when no definition exists in the database collection" in {
      (mockFieldsDefinitionRepository fetch(_: ApiContext, _: ApiVersion)) expects(FakeContext, FakeVersion) returns None

      val result = await(service.get(FakeContext, FakeVersion))

      result shouldBe None
    }

    "return the expected definition" in {
      (mockFieldsDefinitionRepository fetch(_: ApiContext, _: ApiVersion)) expects(FakeContext, FakeVersion) returns Some(FakeFieldsDefinition)

      val result = await(service.get(FakeContext, FakeVersion))

      result shouldBe Some(FakeFieldsDefinitionResponse)
    }
  }

  "upsert" should {
    "return false when updating an existing fields definition" in {
      (mockFieldsDefinitionRepository save _) expects FakeFieldsDefinition returns false

      val result = await(service.upsert(FakeContext, FakeVersion, FakeFieldsDefinitions))

      result shouldBe false
    }

    "return true when creating a new fields definition" in {
      (mockFieldsDefinitionRepository save _) expects FakeFieldsDefinition returns true

      val result = await(service.upsert(FakeContext, FakeVersion, FakeFieldsDefinitions))

      result shouldBe true
    }

    "propagate the error" in {
      (mockFieldsDefinitionRepository save _) expects * returns Future.failed(emulatedFailure)

      val caught = intercept[EmulatedFailure] {
        await(service.upsert(FakeContext, FakeVersion, FakeFieldsDefinitions))
      }

      caught shouldBe emulatedFailure
    }
  }

  "delete" should {
    "return true when the record is removed from the database collection" in {
      (mockFieldsDefinitionRepository delete (_:ApiContext, _:ApiVersion)) expects (FakeContext, FakeVersion) returns true

      await(service.delete(FakeContext, FakeVersion)) shouldBe true
    }

    "return false when the record is not found in the database collection" in {
      (mockFieldsDefinitionRepository delete (_:ApiContext, _:ApiVersion)) expects (FakeContext, FakeVersion) returns false

      await(service.delete(FakeContext, FakeVersion)) shouldBe false
    }
  }
}
