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

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apisubscriptionfields.SubscriptionFieldsTestData
import uk.gov.hmrc.apisubscriptionfields.FieldsDefinitionTestData


class ModelSpec extends UnitSpec with SubscriptionFieldsTestData with FieldsDefinitionTestData with ValidationRuleTestData {
  "RegexValidationRule" should {

    "return true when the value is valid - correct case" in {
      lowerCaseRule.validate(lowerCaseValue) shouldBe true
    }
    "return true when the value is valid - long enough" in {
      atLeastThreeLongRule.validate(lowerCaseValue) shouldBe true
    }
    "return false when the value is invalid - wrong case" in {
      lowerCaseRule.validate(mixedCaseValue) shouldBe false
    }
    "return false when the value is invalid - too short" in {
      atLeastTenLongRule.validate(mixedCaseValue) shouldBe false
    }
  }

  "UrlValidationRule" should {
    "pass for a matching value" in {
      UrlValidationRule.validate(validUrl) shouldBe true
      }
      "fail for a value that does not match" in {
        UrlValidationRule.validate(invalidUrl) shouldBe false
    }
  }
}