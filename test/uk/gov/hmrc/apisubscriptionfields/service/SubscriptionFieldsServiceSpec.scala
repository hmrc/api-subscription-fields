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
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apisubscriptionfields.mocks.{SubscriptionFieldsRepositoryMockModule, _}
import uk.gov.hmrc.apisubscriptionfields.model.Types._
import uk.gov.hmrc.apisubscriptionfields.model._
import uk.gov.hmrc.apisubscriptionfields.{FieldDefinitionTestData, SubscriptionFieldsTestData}

class SubscriptionFieldsServiceSpec extends AnyWordSpec with DefaultAwaitTimeout with FutureAwaits with Matchers with SubscriptionFieldsTestData with FieldDefinitionTestData
    with IdiomaticMockito {

  trait Setup extends ApiFieldDefinitionsServiceMockModule with SubscriptionFieldsRepositoryMockModule with PushPullNotificationServiceMockModule {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val service =
      new SubscriptionFieldsService(SubscriptionFieldsRepositoryMock.aMock, ApiFieldDefinitionsServiceMock.aMock, PushPullNotificationServiceMock.aMock)

    val validSubsFields: SubscriptionFields                  = subsFieldsFor(SubscriptionFieldsMatchRegexValidation)
    val otherValidSubsFields                                 = subsFieldsFor(SubscriptionFieldsMatchRegexValidation + (AlphanumericFieldName -> "CBA321"))
    def validSubsFieldsWithPpnsValue(fieldValue: FieldValue) = subsFieldsFor(SubscriptionFieldsMatchRegexValidation + (PPNSFieldFieldName -> fieldValue))
    val validPpnsSubsFields: SubscriptionFields              = validSubsFieldsWithPpnsValue(PPNSFieldFieldValue)

    val subsFieldsFailingValidation: SubscriptionFields = subsFieldsFor(Map(AlphanumericFieldName -> "ABC 123", PasswordFieldName -> "Qw12@er"))
    val noSubsFields                                    = validSubsFields.copy(fields = Map.empty)

    def thereAreNoFieldDefinitions()   = ApiFieldDefinitionsServiceMock.Get.returnsNothing(FakeContext, FakeVersion)
    def thereAreFieldDefinitions()     = ApiFieldDefinitionsServiceMock.Get.returns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponseWithRegex)
    def thereArePpnsFieldDefinitions() = ApiFieldDefinitionsServiceMock.Get.returns(FakeContext, FakeVersion, FakeApiFieldDefinitionsResponsePPNSWithRegex)

    def thereAreNoExistingFieldValues()                         = SubscriptionFieldsRepositoryMock.Fetch.returnsNone(FakeClientId, FakeContext, FakeVersion)
    def thereAreExistingFieldValues(fields: SubscriptionFields) = SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, fields)
    def thereAreExistingFieldsWithoutValues()                   = thereAreExistingFieldValues(noSubsFields)
    def thereAreExistingPpnsFieldValues()                       = thereAreExistingFieldValues(validPpnsSubsFields)


    def boxIsCreatedIfNeeded() = PushPullNotificationServiceMock.EnsureBoxIsCreated.succeeds(FakeClientId, FakeContext, FakeVersion, PPNSFieldFieldName, FakeBoxId)
    def boxCreationFails()     = PushPullNotificationServiceMock.EnsureBoxIsCreated.fails(FakeClientId, FakeContext, FakeVersion, PPNSFieldFieldName)

    def ppnsFieldGetsUpdated(fieldValue: FieldValue = PPNSFieldFieldValue) =
      PushPullNotificationServiceMock.UpdateCallbackUrl.succeeds(FakeClientId, FakeBoxId, fieldValue)
    def ppnsFieldUpdateFails(error: String) =
      PushPullNotificationServiceMock.UpdateCallbackUrl.fails(FakeClientId, FakeBoxId, PPNSFieldFieldValue, error)

    def fieldsAreCreatedInDB(whichFields: SubscriptionFields) = SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, whichFields, true)
    def fieldsAreUpdatedInDB(whichFields: SubscriptionFields) = SubscriptionFieldsRepositoryMock.SaveAtomic.returns(FakeClientId, FakeContext, FakeVersion, whichFields, false)
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
      thereAreNoExistingFieldValues()
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
    "succeed in creating a new api subscription fields" in new Setup {
      thereAreFieldDefinitions()
      thereAreExistingFieldsWithoutValues()
      fieldsAreCreatedInDB(validSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, validSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, validSubsFields.fields), isInsert = true)
    }

    "succeed without updating because all fields match (no PPNSField definition)" in new Setup {
      thereAreFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, validSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, validSubsFields.fields), isInsert = false)
    }

    "not fail when identical subs fields presented but no definitions exist (anymore)" in new Setup {
      thereAreNoFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, validSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, validSubsFields.fields), isInsert = false)
    }

    "not fail when changed subs fields presented but no definitions exist (anymore)" in new Setup {
      thereAreNoFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, otherValidSubsFields.fields))

      result shouldBe NotFoundSubsFieldsUpsertResponse
    }

    "succeed updating an existing api subscription field because fields don't all match (no PPNSField definition)" in new Setup {
      thereAreFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)
      fieldsAreUpdatedInDB(otherValidSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, otherValidSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, otherValidSubsFields.fields), isInsert = false)
    }

    "fail validation when upserting with an invalid field value (no PPNSField definition)" in new Setup {
      thereAreFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, subsFieldsFailingValidation.fields))

      result shouldBe FailedValidationSubsFieldsUpsertResponse(Map(
        AlphanumericFieldName -> FakeFieldDefinitionAlphnumericField.validation.get.errorMessage,
        PasswordFieldName     -> FakeFieldDefinitionPassword.validation.get.errorMessage
      ))
    }

    "succeed creating a new api subscription fields with PPNS field value" in new Setup {
      thereArePpnsFieldDefinitions()
      thereAreExistingFieldsWithoutValues()
      boxIsCreatedIfNeeded()
      ppnsFieldGetsUpdated()
      fieldsAreCreatedInDB(validPpnsSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, validPpnsSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, validPpnsSubsFields.fields), isInsert = true)
    }

    "succeed without updating because all fields match but PPNS box creation is required" in new Setup {  // APSR-1788
      thereArePpnsFieldDefinitions()
      thereAreExistingPpnsFieldValues()
      boxIsCreatedIfNeeded()

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, validPpnsSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, validPpnsSubsFields.fields), isInsert = false)
    }

    "handle having no PPNS field value in the new fields but saves other fields due to changed values" in new Setup {
      thereArePpnsFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)
      fieldsAreUpdatedInDB(otherValidSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, otherValidSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, otherValidSubsFields.fields), isInsert = false)
    }

    "handle PPNS subscription returning validation errors" in new Setup {
      thereArePpnsFieldDefinitions()
      thereAreNoExistingFieldValues()
      boxIsCreatedIfNeeded()
      ppnsFieldUpdateFails("Bobbins")

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, validPpnsSubsFields.fields))

      result shouldBe FailedValidationSubsFieldsUpsertResponse(Map(PPNSFieldFieldName -> "Bobbins"))
    }

    "succeed updating an existing api subscription field because fields dont all match and PPNS subscribe is required" in new Setup {
      thereArePpnsFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)
      boxIsCreatedIfNeeded()
      ppnsFieldGetsUpdated()
      fieldsAreUpdatedInDB(validPpnsSubsFields)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, validPpnsSubsFields.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, validPpnsSubsFields.fields), isInsert = false)
    }

    "fail validation when upserting with a bad PPNS field value and don't subscribe PPNS" in new Setup {
      thereArePpnsFieldDefinitions()
      SubscriptionFieldsRepositoryMock.Fetch.returns(FakeClientId, FakeContext, FakeVersion, subsFieldsFailingValidation)

      final val BadPpnsValue               = "xxx"
      val baseFields                       = SubscriptionFieldsMatchRegexValidationPPNS
      val fieldsWithInvalidPpnsValue       = baseFields + (PPNSFieldFieldName -> BadPpnsValue)
      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, fieldsWithInvalidPpnsValue))

      result shouldBe FailedValidationSubsFieldsUpsertResponse(Map(PPNSFieldFieldName -> FakeFieldDefinitionPPNSFields.validation.get.errorMessage))
    }

    "propagate the error" in new Setup {
      thereAreFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)
      SubscriptionFieldsRepositoryMock.SaveAtomic.fails(FakeClientId, FakeContext, FakeVersion, emulatedFailure)

      val caught: EmulatedFailure = intercept[EmulatedFailure] {
        await(service.upsert(FakeClientId, FakeContext, FakeVersion, otherValidSubsFields.fields))
      }

      caught shouldBe emulatedFailure
    }

    "APSR-1788" in new Setup {
      val fieldsWithPpnsEmptyValue = validSubsFieldsWithPpnsValue("")
      thereArePpnsFieldDefinitions()
      thereAreExistingFieldValues(validSubsFields)
      boxIsCreatedIfNeeded()
      ppnsFieldGetsUpdated("")
      fieldsAreUpdatedInDB(fieldsWithPpnsEmptyValue)

      val result: SubsFieldsUpsertResponse = await(service.upsert(FakeClientId, FakeContext, FakeVersion, fieldsWithPpnsEmptyValue.fields))

      result shouldBe SuccessfulSubsFieldsUpsertResponse(SubscriptionFields(FakeClientId, FakeContext, FakeVersion, FakeFieldsId, fieldsWithPpnsEmptyValue.fields), isInsert = false)
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

  "validate value against field definition" should {
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
