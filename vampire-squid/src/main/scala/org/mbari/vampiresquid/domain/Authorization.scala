/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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

package org.mbari.vampiresquid.domain

final case class Authorization(token_type: String, access_token: String):

    def tokenType: String   = token_type
    def accessToken: String = access_token

object Authorization:
    val TokenTypeBearer: String = "Bearer"
    val TokenTypeApiKey: String = "APIKey"

    def bearer(accessToken: String): Authorization = Authorization(TokenTypeBearer, accessToken)
