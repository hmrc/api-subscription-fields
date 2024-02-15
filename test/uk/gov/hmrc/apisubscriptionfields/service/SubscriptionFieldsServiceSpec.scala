/*
 * Copyright 2023 HM Revenue & Customs
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

import scala.concurrent.ExecutionContext.Implicits.global

import cats.data.NonEmptyList

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.mocks.{SubscriptionFieldsRepositoryMockModule, _}
import uk.gov.hmrc.apisubscriptionfields.model.Types.FieldErrorMap
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.{AsyncHmrcSpec, FieldDefinitionTestData, SubscriptionFieldsTestData}

class SubscriptionFieldsServiceSpec extends AsyncHmrcSpec with SubscriptionFieldsTestData with FieldDefinitionTestData {

  trait Setup extends ApiFieldDefinitionsServiceMockModule with SubscriptionFieldsRepositoryMockModule with PushPullNotificationServiceMockModule {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val service =
      new SubscriptionFieldsService(SubscriptionFieldsRepositoryMock.aMock, ApiFieldDefinitionsServiceMock.aMock, PushPullNotificationServiceMock.aMock)

  }

  "getAll" should {
    "return an empty list when no entry exists in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.FetchAll.returns(List.empty)

      await(service.getAll) shouldBe BulkSubscriptionFieldsResponse(subscriptions = List())
    }

    "return a list containing all subscription fields" in new Setup {
      val sf1: SubscriptionFields = createSubscriptionFieldsWithApiContext(FakeClientId)
      val sf2: SubscriptionFields = createSubscriptionFieldsWithApiContext(FakeClientId2)

      SubscriptionFieldsRepositoryMock.FetchAll.returns(List(sf1, sf2))

      val expectedResponse: BulkSubscriptionFieldsResponse = BulkSubscriptionFieldsResponse(subscriptions =
        List(
          SubscriptionFields(sf1.clientId, sf1.apiContext, sf1.apiVersion, sf1.fieldsId, sf1.fields),
          SubscriptionFields(sf2.clientId, sf2.apiContext, sf2.apiVersion, sf2.fieldsId, sf2.fields)
        )
      )

      await(service.getAll) shouldBe expectedResponse
    }
  }

  "get by clientId" should {
    "return None when the expected record does not exist in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.FetchByClientId.returns(FakeClientId, List())

      await(service.getByClientId(FakeClientId)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in new Setup {
      val sf1: SubscriptionFields = createSubscriptionFieldsWithApiContext()
      val sf2: SubscriptionFields = createSubscriptionFieldsWithApiContext(rawContext = fakeRawContext2)
      SubscriptionFieldsRepositoryMock.FetchByClientId.returns(FakeClientId, List(sf1, sf2))

      val result: Option[BulkSubscriptionFieldsResponse] = await(service.getByClientId(FakeClientId))

      result shouldBe Some(
        BulkSubscriptionFieldsResponse(subscriptions =
          Seq(
            SubscriptionFields(sf1.clientId, sf1.apiContext, sf1.apiVersion, sf1.fieldsId, sf1.fields),
            SubscriptionFields(sf2.clientId, sf2.apiContext, sf2.apiVersion, sf2.fieldsId, sf2.fields)
          )
        )
      )
    }
  }

  "get" should {
    "return None when no entry exists in the repo" in new Setup {
      SubscriptionFieldsRepositoryMock.Fetch.returnsNone(FakeClientId, FakeContext, FakeVersion)
      await(service.get(FakeClientId, FakeContext, FakeVersion)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, FakeApiSubscription)

      val result: Option[SubscriptionFields] = await(service.get(FakeClientId, FakeContext, FakeVersion))

      result shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "get by fieldsId" should {
    "return None when no entry exists in the repo" in new Setup {
      SubscriptionFieldsRepositoryMock.FetchByFieldsId.returnsNone(SubscriptionFieldsId(FakeRawFieldsId))

      await(service.getBySubscriptionFieldId(FakeFieldsId)) shouldBe None
    }

    "return the expected response when the entry exists in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.FetchByFieldsId.returns(SubscriptionFieldsId(FakeRawFieldsId), FakeApiSubscription)

      await(service.getBySubscriptionFieldId(FakeFieldsId)) shouldBe Some(FakeSubscriptionFieldsResponse)
    }
  }

  "upsert" should {
    val fieldsNonMatch: Types.Fields                   = SubscriptionFieldsNonMatchRegexValidation
    val fields: Types.Fields                           = SubscriptionFieldsMatchRegexValidation
    val subscriptionFieldsNonMatch: SubscriptionFields = subsFieldsFor(fieldsNonMatch)
    val subscriptionFieldsMatching: SubscriptionFields = subsFieldsFor(fields)

    "return false when updating an existing api subscription fields (no PPNS)" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponseWithRegex)
      SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch, false)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsMatching)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fields), isInsert = false)
      PushPullNotificationServiceMock.verifyZeroInteractions()
    }

    "return false when updating an existing api subscription fields where all fields match (no PPNS)" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponseWithRegex)
      SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsMatching, false)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsMatching)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fields), isInsert = false)
      PushPullNotificationServiceMock.verifyZeroInteractions()
    }

    "return false when updating an existing api subscription fields (has PPNS)" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponsePPNSWithRegex)
      PushPullNotificationServiceMock.SubscribeToPPNS.returns(FakeClientId, FakeContext, FakeVersion, Some("https://www.mycallbackurl.com"), PPNSCallBackUrlSuccessResponse)
      SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch, false)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidationPPNS))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fieldsNonMatch), isInsert = false)

      PushPullNotificationServiceMock.SubscribeToPPNS.verifyCalled()
      SubscriptionFieldsRepositoryMock.SaveAtomic.verifyCalled()
    }

    "return false when updating an existing api subscription field with empty string callback URL for PPNS" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponsePPNSWithRegex)
      PushPullNotificationServiceMock.SubscribeToPPNS.returns(FakeClientId, FakeContext, FakeVersion, Some(""), PPNSCallBackUrlSuccessResponse)
      SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch, false)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsEmptyValueRegexValidationPPNS))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fieldsNonMatch), isInsert = false)

      PushPullNotificationServiceMock.SubscribeToPPNS.verifyCalled()
      SubscriptionFieldsRepositoryMock.SaveAtomic.verifyCalled()
    }

    "return PPNSCallBackUrlSuccessResponse when updating an existing api subscription field for PPNS is not included" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponsePPNSWithRegex)
      PushPullNotificationServiceMock.SubscribeToPPNS.returns(FakeClientId, FakeContext, FakeVersion, None, PPNSCallBackUrlSuccessResponse)
      SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch, false)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fieldsNonMatch), isInsert = false)

      PushPullNotificationServiceMock.SubscribeToPPNS.verifyCalled()
      SubscriptionFieldsRepositoryMock.SaveAtomic.verifyCalled()

    }

    "return FailedValidationSubsFieldsUpsertResponse when updating an existing api subscription fields and PPNS service returns failure" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponsePPNSWithRegex)
      PushPullNotificationServiceMock.SubscribeToPPNS.returns(
        FakeClientId,
        FakeContext,
        FakeVersion,
        Some("https://www.mycallbackurl.com"),
        PPNSCallBackUrlFailedResponse("An Error Occurred")
      )

      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidationPPNS))

      result shouldBe FailedValidationSubsFieldsUpsertResponse(Map(PPNSFieldFieldName -> "An Error Occurred"))

      PushPullNotificationServiceMock.SubscribeToPPNS.verifyCalled()
    }

    "return FailedValidationSubsFieldsUpsertResponse when updating an existing api subscription fields and PPNS field fails validation" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponsePPNSWithRegex)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsDoNotMatchRegexValidationPPNS))

      result shouldBe FailedValidationSubsFieldsUpsertResponse(Map(PPNSFieldFieldName -> "CallBackUrl Validation"))

      PushPullNotificationServiceMock.verifyZeroInteractions()
    }

    "return true when creating a new api subscription fields" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponseWithRegex)

      SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch, true)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fieldsNonMatch), isInsert = true)
      PushPullNotificationServiceMock.verifyZeroInteractions()
    }

    "propagate the error" in new Setup {
      ApiFieldDefinitionsServiceMock.Get.thenReturns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponseWithRegex)
      SubscriptionFieldsRepositoryMock.SaveAtomic.fails(FakeClientId, FakeContext, FakeVersion, emulatedFailure)
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subscriptionFieldsNonMatch)

      val caught: EmulatedFailure = intercept[EmulatedFailure] {
        await(service.upsert(FakeClientId, FakeContext, FakeVersion, SubscriptionFieldsMatchRegexValidation))
      }

      caught shouldBe emulatedFailure
    }
  }

  "delete" should {
    "return true when the entry exists in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.Delete.existsFor(FakeClientId, FakeContext, FakeVersion)

      await(service.delete(FakeClientId, FakeContext, FakeVersion)) shouldBe true
    }

    "return false when the entry does not exist in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.Delete.notExistingFor(FakeClientId, FakeContext, FakeVersion)

      await(service.delete(FakeClientId, FakeContext, FakeVersion)) shouldBe false
    }

    "return true when the client ID exists in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.Delete.existsFor(FakeClientId)

      await(service.delete(FakeClientId)) shouldBe true
    }

    "return false when the client ID does not exist in the database collection" in new Setup {
      SubscriptionFieldsRepositoryMock.Delete.notExistingFor(FakeClientId)

      await(service.delete(FakeClientId)) shouldBe false
    }
  }

  def theErrorMessage(i: Int)           = s"error message $i"
  val validationGroup1: ValidationGroup = ValidationGroup(theErrorMessage(1), NonEmptyList(mixedCaseRule, List(atLeastThreeLongRule)))

  "validate value against group" should {
    "return true when the value is both mixed case and at least 3 long" in new Setup {
      SubscriptionFieldsService.validateAgainstGroup(validationGroup1, mixedCaseValue) shouldBe true
    }

    "return false when the value is not mixed case or not at least 3 long" in new Setup {
      val hasNumeralsValue   = "A345"
      val veryShortMixedCase = "Ab"
      SubscriptionFieldsService.validateAgainstGroup(validationGroup1, hasNumeralsValue) shouldBe false
      SubscriptionFieldsService.validateAgainstGroup(validationGroup1, veryShortMixedCase) shouldBe false
    }
  }

  "validate value against field defintion" should {
    val fieldDefintionWithoutValidation = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", None)
    val fieldDefinitionWithValidation   = fieldDefintionWithoutValidation.copy(validation = Some(validationGroup1))

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
    val fieldDefinitionWithoutValidation = FieldDefinition(fieldN(1), "desc1", "hint1", FieldDefinitionType.URL, "short description", None)
    val fields                           = Map(fieldN(1) -> "Emily")

    "succeed when Fields match Field Definitions" in new Setup {
      SubscriptionFieldsService.validateFieldNamesAreDefined(NonEmptyList.one(fieldDefinitionWithoutValidation), fields) shouldBe empty
    }

    "fail when when Fields are not present in the Field Definitions" in new Setup {
      val errs: FieldErrorMap =
        SubscriptionFieldsService.validateFieldNamesAreDefined(NonEmptyList.one(fieldDefinitionWithoutValidation), Map(fieldN(5) -> "Bob", fieldN(1) -> "Fred"))
      errs should not be empty
      errs.head match {
        case (name, _) if name == fieldN(5) => succeed
        case _                              => fail("Not the field we expected")
      }
    }
  }
}
