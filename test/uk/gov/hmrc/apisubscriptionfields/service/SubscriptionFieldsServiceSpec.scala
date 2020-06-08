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

import java.{util=>ju}
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.repository._
import uk.gov.hmrc.apisubscriptionfields.{FieldDefinitionTestData, SubscriptionFieldsTestData}
import uk.gov.hmrc.apisubscriptionfields.AsyncHmrcSpec
import cats.data.NonEmptyList
import scala.concurrent.Future.{successful,failed}
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class SubscriptionFieldsServiceSpec extends AsyncHmrcSpec with SubscriptionFieldsTestData with FieldDefinitionTestData {

  trait Setup {
    val mockSubscriptionFieldsRepository = mock[SubscriptionFieldsMongoRepository]
    val mockApiFieldDefinitionsService = mock[ApiFieldDefinitionsService]
    val mockPushPullNotificationService = mock[PushPullNotificationService](org.mockito.Mockito.withSettings().verboseLogging())

    val mockUuidCreator = new UUIDCreator {
      override def uuid(): ju.UUID = FakeRawFieldsId
    }

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val service = new SubscriptionFieldsService(mockSubscriptionFieldsRepository, mockUuidCreator, mockApiFieldDefinitionsService, mockPushPullNotificationService)

  }

  "getAll" should {
    "return an empty list when no entry exists in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.fetchAll).thenReturn(successful(List.empty))

      await(service.getAll) shouldBe BulkSubscriptionFieldsResponse(subscriptions = List())
    }

    "return a list containing all subscription fields" in new Setup {
      val sf1: SubscriptionFields = createSubscriptionFieldsWithApiContext(FakeClientId)
      val sf2: SubscriptionFields = createSubscriptionFieldsWithApiContext(FakeClientId2)

      when(mockSubscriptionFieldsRepository.fetchAll).thenReturn(successful(List(sf1, sf2)))

      val expectedResponse = BulkSubscriptionFieldsResponse(subscriptions =
        List(
          SubscriptionFieldsResponse(sf1.clientId, sf1.apiContext, sf1.apiVersion, SubscriptionFieldsId(sf1.fieldsId), sf1.fields),
          SubscriptionFieldsResponse(sf2.clientId, sf2.apiContext, sf2.apiVersion, SubscriptionFieldsId(sf2.fieldsId), sf2.fields)
        )
      )

      await(service.getAll) shouldBe expectedResponse
    }
  }

  "get by clientId" should {
    "return None when the expected record does not exist in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.fetchByClientId(FakeClientId)).thenReturn(successful(List()))

      await(service.getByClientId(FakeClientId)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in new Setup {
      val sf1 = createSubscriptionFieldsWithApiContext()
      val sf2 = createSubscriptionFieldsWithApiContext(rawContext = fakeRawContext2)
      when(mockSubscriptionFieldsRepository.fetchByClientId(FakeClientId)).thenReturn(successful(List(sf1, sf2)))

      val result = await(service.getByClientId(FakeClientId))

      result shouldBe Some(
        BulkSubscriptionFieldsResponse(subscriptions =
          Seq(
            SubscriptionFieldsResponse(sf1.clientId, sf1.apiContext, sf1.apiVersion, SubscriptionFieldsId(sf1.fieldsId), sf1.fields),
            SubscriptionFieldsResponse(sf2.clientId, sf2.apiContext, sf2.apiVersion, SubscriptionFieldsId(sf2.fieldsId), sf2.fields)
          )
        )
      )
    }
  }

  "get" should {
    "return None when no entry exists in the repo" in new Setup {
      when(mockSubscriptionFieldsRepository.fetch(FakeClientId, FakeContext, FakeVersion)).thenReturn(successful(None))

      await(service.get(FakeClientId, FakeContext, FakeVersion)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.fetch(FakeClientId, FakeContext, FakeVersion)).thenReturn(successful(Some(FakeApiSubscription)))

      val result = await(service.get(FakeClientId, FakeContext, FakeVersion))

      result shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "get by fieldsId" should {
    "return None when no entry exists in the repo" in new Setup {
      when(mockSubscriptionFieldsRepository.fetchByFieldsId(SubscriptionFieldsId(FakeRawFieldsId))).thenReturn(successful(None))

      await(service.getBySubscriptionFieldId(FakeFieldsId)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.fetchByFieldsId(SubscriptionFieldsId(FakeRawFieldsId))).thenReturn(successful(Some(FakeApiSubscription)))

      await(service.getBySubscriptionFieldId(FakeFieldsId)) shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "upsert" should {
    val fields: Types.Fields = SubscriptionFieldsMatchRegexValidation
    val subscriptionFields: SubscriptionFields = subsFieldsFor(fields)
      val ppnsResponse: Future[Unit] = successful(())

    "return false when updating an existing `api subscription fields" in new Setup {
      when(mockApiFieldDefinitionsService.get(FakeContext, FakeVersion)).thenReturn(successful(Some(FakeApiFieldDefinitionsResponseWithRegex)))
      when(mockPushPullNotificationService.subscribeToPPNS(eqTo(FakeClientId), eqTo(FakeContext), eqTo(FakeVersion), any, eqTo(fields))(any)).thenReturn(ppnsResponse)
      when(mockSubscriptionFieldsRepository.saveAtomic(*)).thenReturn(successful((subscriptionFields, false)))

      val result = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))

      result shouldBe (SuccessfulSubsFieldsUpsertResponse(SubscriptionFieldsResponse(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fields), false))
    }

    "return true when creating a new api subscription fields" in new Setup {
      when(mockApiFieldDefinitionsService.get(FakeContext, FakeVersion)).thenReturn(successful(Some(FakeApiFieldDefinitionsResponseWithRegex)))
      when(mockPushPullNotificationService.subscribeToPPNS(eqTo(FakeClientId), eqTo(FakeContext), eqTo(FakeVersion), any, eqTo(fields))(any)).thenReturn(ppnsResponse)
      when(mockSubscriptionFieldsRepository.saveAtomic(*)).thenReturn(successful((subscriptionFields, true)))

      val result = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))

      result shouldBe (SuccessfulSubsFieldsUpsertResponse(SubscriptionFieldsResponse(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fields), true))
    }

    "propagate the error" in new Setup {
      when(mockApiFieldDefinitionsService.get(FakeContext, FakeVersion)).thenReturn(successful(Some(FakeApiFieldDefinitionsResponseWithRegex)))
      when(mockSubscriptionFieldsRepository.saveAtomic(*)).thenReturn(failed(emulatedFailure))

      val caught = intercept[EmulatedFailure] {
        await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))
      }

      caught shouldBe emulatedFailure
    }
  }

  "delete" should {
    "return true when the entry exists in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.delete(FakeClientId, FakeContext,FakeVersion)).thenReturn(successful(true))

      await(service.delete(FakeClientId, FakeContext, FakeVersion)) shouldBe true
    }

    "return false when the entry does not exist in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.delete(FakeClientId, FakeContext,FakeVersion)).thenReturn(successful(false))

      await(service.delete(FakeClientId, FakeContext, FakeVersion)) shouldBe false
    }

    "return true when the client ID exists in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.delete(FakeClientId)).thenReturn(successful(true))

      await(service.delete(FakeClientId)) shouldBe true
    }

    "return false when the client ID does not exist in the database collection" in new Setup {
      when(mockSubscriptionFieldsRepository.delete(FakeClientId)).thenReturn(successful(false))

      await(service.delete(FakeClientId)) shouldBe false
    }
  }

  def theErrorMessage(i: Int) = s"error message $i"
  val validationGroup1: ValidationGroup = ValidationGroup(theErrorMessage(1), NonEmptyList(mixedCaseRule, List(atLeastThreeLongRule)))

  "validate value against group" should {
    "return true when the value is both mixed case and at least 3 long" in new Setup {
      SubscriptionFieldsService.validateAgainstGroup(validationGroup1, mixedCaseValue) shouldBe true
    }

    "return false when the value is not mixed case or not at least 3 long" in new Setup {
      val hasNumeralsValue = "A345"
      val veryShortMixedCase = "Ab"
      SubscriptionFieldsService.validateAgainstGroup(validationGroup1, hasNumeralsValue) shouldBe false
      SubscriptionFieldsService.validateAgainstGroup(validationGroup1, veryShortMixedCase) shouldBe false
    }
  }

  "validate value against field defintion" should {
    val fieldDefintionWithoutValidation = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", None)
    val fieldDefinitionWithValidation = fieldDefintionWithoutValidation.copy(validation = Some(validationGroup1))

    "succeed when no validation is present on the field defintion" in new Setup {
      SubscriptionFieldsService.validateAgainstDefinition(fieldDefintionWithoutValidation, lowerCaseValue) shouldBe None
    }

    "return FieldError when validation on the field defintion does not match the value" in new Setup {
      val hasNumeralsValue = "A345"
      SubscriptionFieldsService.validateAgainstDefinition(fieldDefinitionWithValidation, hasNumeralsValue) shouldBe
        Some((fieldDefinitionWithValidation.name, validationGroup1.errorMessage))
    }
  }

  "validate Field Names Are Defined" should {
    val fieldDefintionWithoutValidation = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", None)
    val fields = Map(fieldN(1) -> "Emily")

    "succeed when Fields match Field Definitions" in new Setup {
      SubscriptionFieldsService.validateFieldNamesAreDefined(NonEmptyList.one(fieldDefintionWithoutValidation), fields) shouldBe empty
    }

    "fail when when Fields are not present in the Field Definitions" in new Setup {
      val errs = SubscriptionFieldsService.validateFieldNamesAreDefined(NonEmptyList.one(fieldDefintionWithoutValidation), Map(fieldN(5) -> "Bob", fieldN(1) -> "Fred"))
      errs should not be empty
      errs.head match {
        case (name, msg) if name == fieldN(5)=> succeed
        case _ => fail("Not the field we expected")
      }
    }
  }
}
