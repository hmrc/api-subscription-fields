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

package unit.uk.gov.hmrc.apisubscriptionfields.model

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.apisubscriptionfields.model._
import util.SubscriptionFieldsTestData

class ModelSpec extends WordSpec with Matchers with SubscriptionFieldsTestData {

  private val Separator: String = "##"

  "SubscriptionIdentifier" should {
    "encode" should {
      "Handle simple case" in {
        SubscriptionIdentifier(AppId(fakeAppId), ApiContext("XYZ"), ApiVersion("123")).encode() shouldBe s"$fakeAppId${Separator}XYZ${Separator}123"
      }
      "Handle components with separators in them" in {
        SubscriptionIdentifier(AppId(fakeAppId), ApiContext("X##Z"), ApiVersion("123")).encode() shouldBe s"$fakeAppId$Separator${Separator}X##Z$Separator${Separator}123"
      }
      "Handle components with multiple separators in them" in {
        val sep = "######"
        SubscriptionIdentifier(AppId(fakeAppId), ApiContext("X##Z"), ApiVersion("12####3")).encode() shouldBe s"$fakeAppId${sep}X##Z${sep}12####3"
      }
    }

    "decode" should {
      "Handle simple case" in {
        SubscriptionIdentifier.decode(s"$fakeAppId${Separator}XYZ${Separator}123") shouldBe
          Some(SubscriptionIdentifier(AppId(fakeAppId), ApiContext("XYZ"), ApiVersion("123")))
      }
      "Handle components with separators in them" in {
        SubscriptionIdentifier.decode(s"$fakeAppId$Separator${Separator}X##Z$Separator${Separator}123") shouldBe
          Some(SubscriptionIdentifier(AppId(fakeAppId), ApiContext("X##Z"), ApiVersion("123")))
      }
      "Handle components with multiple separators in them" in {
        SubscriptionIdentifier.decode(s"$fakeAppId$Separator$Separator${Separator}X##Z$Separator$Separator${Separator}12####3") shouldBe
          Some(SubscriptionIdentifier(AppId(fakeAppId), ApiContext("X##Z"), ApiVersion("12####3")))
      }
      "Handle invalid number of parts" in {
        SubscriptionIdentifier.decode(s"$fakeAppId") shouldBe None
      }
    }
  }


  "FieldsDefinitionIdentifier" should {
    "encode" should {
      "Handle simple case" in {
        FieldsDefinitionIdentifier(ApiContext("XYZ"), ApiVersion("123")).encode() shouldBe s"XYZ${Separator}123"
      }
      "Handle components with separators in them" in {
        FieldsDefinitionIdentifier(ApiContext("X##Z"), ApiVersion("123")).encode() shouldBe s"X##Z$Separator${Separator}123"
      }
      "Handle components with multiple separators in them" in {
        val sep = "######"
        FieldsDefinitionIdentifier(ApiContext("X##Z"), ApiVersion("12####3")).encode() shouldBe s"X##Z${sep}12####3"
      }
    }

    "decode" should {
      "Handle simple case" in {
        FieldsDefinitionIdentifier.decode(s"XYZ${Separator}123") shouldBe
          Some(FieldsDefinitionIdentifier(ApiContext("XYZ"), ApiVersion("123")))
      }
      "Handle components with separators in them" in {
        FieldsDefinitionIdentifier.decode(s"X##Z$Separator${Separator}123") shouldBe
          Some(FieldsDefinitionIdentifier(ApiContext("X##Z"), ApiVersion("123")))
      }
      "Handle components with multiple separators in them" in {
        FieldsDefinitionIdentifier.decode(s"X##Z$Separator$Separator${Separator}12####3") shouldBe
          Some(FieldsDefinitionIdentifier(ApiContext("X##Z"), ApiVersion("12####3")))
      }
      "Handle invalid number of parts" in {
        SubscriptionIdentifier.decode(s"""$fakeAppId""") shouldBe None
      }
    }
  }

}
