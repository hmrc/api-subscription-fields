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

import uk.gov.hmrc.apisubscriptionfields.SubscriptionFieldsTestData
import uk.gov.hmrc.apisubscriptionfields.FieldDefinitionTestData
import uk.gov.hmrc.apisubscriptionfields.HmrcSpec
import org.scalatest.Matchers

class ModelSpec extends HmrcSpec with SubscriptionFieldsTestData with FieldDefinitionTestData with ValidationRuleTestData {
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
    "return true when the value is blank" in {
      atLeastTenLongRule.validate("") shouldBe true
    }
  }
}

class UrlValidationRuleSpec extends HmrcSpec with ValidationRuleTestData with Matchers {
  "url validation rule" should {
    "pass for a matching value" in {
      UrlValidationRule.validate(validUrl) shouldBe true
    }

    "pass for localhost" in {
      UrlValidationRule.validate(localValidUrl) shouldBe true
    }

    "return true when the value is blank" in {
      UrlValidationRule.validate("") shouldBe true
    }

    "invalid urls" in {
      invalidUrls.map(invalidUrl =>  {
        UrlValidationRule.validate(invalidUrl) shouldBe false
      })
    }

    "handles internal mdtp domains in url" in {
     UrlValidationRule.validate("https://who-cares.mdtp/pathy/mcpathface") shouldBe true
    }
  }
}