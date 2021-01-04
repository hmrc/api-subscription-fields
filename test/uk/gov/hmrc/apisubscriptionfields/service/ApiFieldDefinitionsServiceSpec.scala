/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.apisubscriptionfields.FieldDefinitionTestData
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository.ApiFieldDefinitionsRepository
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec

import scala.concurrent.Future.{successful,failed}
import cats.data.NonEmptyList
import scala.concurrent.ExecutionContext.Implicits.global

class ApiFieldDefinitionsServiceSpec extends AsyncHmrcSpec with FieldDefinitionTestData {

  private val mockApiFieldDefinitionsRepository = mock[ApiFieldDefinitionsRepository]
  private val service = new ApiFieldDefinitionsService(mockApiFieldDefinitionsRepository)

  "getAll" should {
    "return an empty list when there are no records in the database collection" in {
      when(mockApiFieldDefinitionsRepository.fetchAll()).thenReturn(successful(List()))

      val result = await(service.getAll)

      result shouldBe BulkApiFieldDefinitionsResponse(List())
    }

    "return a list of all entries" in {
      val fd1 = createApiFieldDefinitions(apiContext = FakeContext, fieldDefinitions = NonEmptyList.one(FakeFieldDefinitionUrl))
      val fd2 = createApiFieldDefinitions(apiContext = FakeContext2, fieldDefinitions = NonEmptyList.one(FakeFieldDefinitionString))

      when(mockApiFieldDefinitionsRepository.fetchAll()).thenReturn(successful(List(fd1, fd2)))

      val result = await(service.getAll)

      val expectedResponse = BulkApiFieldDefinitionsResponse(apis = Seq(
        ApiFieldDefinitions(FakeContext, FakeVersion, NonEmptyList.one(FakeFieldDefinitionUrl)),
        ApiFieldDefinitions(FakeContext2, FakeVersion, NonEmptyList.one(FakeFieldDefinitionString))))
      result shouldBe expectedResponse
    }
  }

  "get" should {
    "return None when no definition exists in the database collection" in {
      when(mockApiFieldDefinitionsRepository.fetch(FakeContext, FakeVersion)).thenReturn(successful(None))

      val result = await(service.get(FakeContext, FakeVersion))

      result shouldBe None
    }

    "return the expected definition" in {
      when(mockApiFieldDefinitionsRepository.fetch(FakeContext, FakeVersion)).thenReturn(successful(Some(FakeApiFieldDefinitions)))

      val result = await(service.get(FakeContext, FakeVersion))

      result shouldBe Some(FakeApiFieldDefinitionsResponse)
    }
  }

  "upsert" should {
    "return false when updating an existing fields definition" in {
      when(mockApiFieldDefinitionsRepository.save(FakeApiFieldDefinitions)).thenReturn(successful((FakeApiFieldDefinitions, false)))

      val result = await(service.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions))

      result shouldBe ((ApiFieldDefinitions(FakeContext, FakeVersion, NelOfFieldDefinitions), false))
    }

    "return true when creating a new fields definition" in {
      when(mockApiFieldDefinitionsRepository.save(FakeApiFieldDefinitions)).thenReturn(successful((FakeApiFieldDefinitions, true)))

      val result = await(service.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions))

      result shouldBe ((ApiFieldDefinitions(FakeContext, FakeVersion, NelOfFieldDefinitions), true))
    }

    "propagate the error" in {
      when(mockApiFieldDefinitionsRepository.save(*)).thenReturn(failed(emulatedFailure))

      val caught = intercept[EmulatedFailure] {
        await(service.upsert(FakeContext, FakeVersion, NelOfFieldDefinitions))
      }

      caught shouldBe emulatedFailure
    }
  }

  "delete" should {
    "return true when the record is removed from the database collection" in {
      when(mockApiFieldDefinitionsRepository.delete(FakeContext, FakeVersion)).thenReturn(successful(true))

      await(service.delete(FakeContext, FakeVersion)) shouldBe true
    }

    "return false when the record is not found in the database collection" in {
      when(mockApiFieldDefinitionsRepository.delete(FakeContext, FakeVersion)).thenReturn(successful(false))

      await(service.delete(FakeContext, FakeVersion)) shouldBe false
    }
  }

}
