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

package uk.gov.hmrc.apisubscriptionfields.service

import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.apisubscriptionfields.FieldDefinitionTestData
import uk.gov.hmrc.apisubscriptionfields.model.{ApiContext, ApiVersion, ApiFieldDefinitions, BulkApiFieldDefinitionsResponse, ApiFieldDefinitionsResponse}
import uk.gov.hmrc.apisubscriptionfields.repository.ApiFieldDefinitionsRepository
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import cats.data.NonEmptyList
import scala.concurrent.ExecutionContext.Implicits.global

class ApiFieldDefinitionsServiceSpec extends UnitSpec with FieldDefinitionTestData with MockFactory {

  private val mockApiFieldDefinitionsRepository = mock[ApiFieldDefinitionsRepository]
  private val service = new ApiFieldDefinitionsService(mockApiFieldDefinitionsRepository)

  "getAll" should {
    "return an empty list when there are no records in the database collection" in {
      (mockApiFieldDefinitionsRepository.fetchAll _).expects().returns(List())

      val result = await(service.getAll)

      result shouldBe BulkApiFieldDefinitionsResponse(List())
    }

    "return a list of all entries" in {
      val fd1 = createApiFieldDefinitions(apiContext = "api-1", fieldDefinitions = NonEmptyList.one(FakeFieldDefinitionUrl))
      val fd2 = createApiFieldDefinitions(apiContext = "api-2", fieldDefinitions = NonEmptyList.one(FakeFieldDefinitionString))

      (mockApiFieldDefinitionsRepository.fetchAll _).expects().returns(List(fd1, fd2))

      val result = await(service.getAll)

      val expectedResponse = BulkApiFieldDefinitionsResponse(apis = Seq(
        ApiFieldDefinitionsResponse("api-1", fakeRawVersion, NonEmptyList.one(FakeFieldDefinitionUrl)),
        ApiFieldDefinitionsResponse("api-2", fakeRawVersion, NonEmptyList.one(FakeFieldDefinitionString))))
      result shouldBe expectedResponse
    }
  }

  "get" should {
    "return None when no definition exists in the database collection" in {
      (mockApiFieldDefinitionsRepository fetch(_: ApiContext, _: ApiVersion)).expects(FakeContext, FakeVersion)
        .returns(None)

      val result = await(service.get(FakeContext, FakeVersion))

      result shouldBe None
    }

    "return the expected definition" in {
      (mockApiFieldDefinitionsRepository fetch(_: ApiContext, _: ApiVersion)).expects(FakeContext, FakeVersion)
        .returns(Some(FakeApiFieldDefinitions))

      val result = await(service.get(FakeContext, FakeVersion))

      result shouldBe Some(FakeApiFieldDefinitionsResponse)
    }
  }

  "upsert" should {
    "return false when updating an existing fields definition" in {
      (mockApiFieldDefinitionsRepository save _) expects FakeApiFieldDefinitions returns ((FakeApiFieldDefinitions, false))

      val result = await(service.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions))

      result shouldBe ((ApiFieldDefinitionsResponse(fakeRawContext, fakeRawVersion, NelOfFieldDefinitions), false))
    }

    "return true when creating a new fields definition" in {
      (mockApiFieldDefinitionsRepository save _) expects FakeApiFieldDefinitions returns ((FakeApiFieldDefinitions, true))

      val result = await(service.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions))

      result shouldBe ((ApiFieldDefinitionsResponse(fakeRawContext, fakeRawVersion, NelOfFieldDefinitions), true))
    }

    "propagate the error" in {
      (mockApiFieldDefinitionsRepository save(_: ApiFieldDefinitions)) expects * returns Future.failed(emulatedFailure)

      val caught = intercept[EmulatedFailure] {
        await(service.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions))
      }

      caught shouldBe emulatedFailure
    }
  }

  "delete" should {
    "return true when the record is removed from the database collection" in {
      (mockApiFieldDefinitionsRepository delete (_:ApiContext, _:ApiVersion)).expects(FakeContext, FakeVersion)
        .returns(true)

      await(service.delete(FakeContext, FakeVersion)) shouldBe true
    }

    "return false when the record is not found in the database collection" in {
      (mockApiFieldDefinitionsRepository delete (_:ApiContext, _:ApiVersion)).expects(FakeContext, FakeVersion)
        .returns(false)

      await(service.delete(FakeContext, FakeVersion)) shouldBe false
    }
  }

}
