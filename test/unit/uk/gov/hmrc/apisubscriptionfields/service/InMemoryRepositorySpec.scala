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

import java.util.UUID
import java.util.concurrent.TimeUnit

import uk.gov.hmrc.apisubscriptionfields.repository.{InMemoryRepository, MongoFormatters, SubscriptionFields}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

class InMemoryRepositorySpec extends org.scalatest.WordSpec with org.scalatest.Matchers with org.scalatest.OptionValues with MongoFormatters {
  private val FakeApplicaionIdentifier: String = "12345"
  private val AltFakeApplicaionIdentifier: String = "98765"

  private val FakeFieldsId = UUID.randomUUID()
  private val AltFakeFieldsId = UUID.randomUUID()

  private val FakeApiSubscription = SubscriptionFields(FakeApplicaionIdentifier, FakeFieldsId, Map("f1" -> "v1", "f2" -> "v2"))
  private val UpdatedFakeApiSubscription = SubscriptionFields(FakeApplicaionIdentifier, FakeFieldsId, Map("f2" -> "xyz", "f3" -> "v3"))
  private val AltFakeApiSubscription = SubscriptionFields(AltFakeApplicaionIdentifier, AltFakeFieldsId, Map("f1" -> "v1", "f2" -> "v2"))


  "InMemoryRepository" should {
    val repo = new InMemoryRepository
    implicit val timeout = FiniteDuration(1,TimeUnit.SECONDS)

    def validateIsPresent(key: String): SubscriptionFields = {
      val find = Await.result(repo.fetchById(key), timeout)
      find shouldBe defined
      Await.result(repo.fetchByFieldsId(find.get.fieldsId), timeout) shouldBe defined
      find.get
    }

    def validateIsAbsent(key: String, fieldId: UUID): Unit = {
      val find = Await.result(repo.fetchById(key), timeout)
      find shouldBe None
      Await.result(repo.fetchByFieldsId(fieldId), timeout) shouldBe None
    }

    def validateFakeIsAbsent() = validateIsAbsent(FakeApplicaionIdentifier, FakeFieldsId)
    def validateFakeIsPresent(sub: SubscriptionFields = FakeApiSubscription) = validateIsPresent(FakeApplicaionIdentifier) shouldBe sub

    def validateAltFakeIsAbsent() = validateIsAbsent(AltFakeApplicaionIdentifier, AltFakeFieldsId)
    def validateAltFakeIsPresent(sub: SubscriptionFields = AltFakeApiSubscription) = validateIsPresent(AltFakeApplicaionIdentifier) shouldBe sub

    "Save correctly" in {
      validateFakeIsAbsent()
      validateAltFakeIsAbsent()

      repo.save(FakeApiSubscription)
      validateFakeIsPresent()
      validateAltFakeIsAbsent()
    }

    "Delete correctly" in {
      repo.delete(AltFakeApplicaionIdentifier)
      validateFakeIsPresent()
      validateAltFakeIsAbsent()
    }

    "Save Alt" in {
      repo.save(AltFakeApiSubscription)
      validateFakeIsPresent()
      validateAltFakeIsPresent()
    }

    "Delete original" in {
      repo.delete(FakeApplicaionIdentifier)
      validateFakeIsAbsent()
      validateAltFakeIsPresent()
    }

    "Then save original again" in {
      repo.save(FakeApiSubscription)
      validateFakeIsPresent()
    }

    "Then update original" in {
      repo.save(UpdatedFakeApiSubscription)
      validateFakeIsPresent(UpdatedFakeApiSubscription)
    }
  }

}
