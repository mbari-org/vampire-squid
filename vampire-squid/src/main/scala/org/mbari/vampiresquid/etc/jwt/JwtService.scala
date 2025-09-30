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

package org.mbari.vampiresquid.etc.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class JwtService(issuer: String, apiKey: String, signingSecret: String):

    private val algorithm = Algorithm.HMAC512(signingSecret)

    private val verifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    def verify(jwt: String): Boolean =
        try
            verifier.verify(jwt)
            true
        catch case e: Exception => false

    def authorize(providedApiKey: String): Option[String] =
        if providedApiKey == apiKey then
            val now      = Instant.now()
            val tomorrow = now.plus(1, ChronoUnit.DAYS)
            val iat      = Date.from(now)
            val exp      = Date.from(tomorrow)

            val jwt = JWT
                .create()
                .withIssuer(issuer)
                .withIssuedAt(iat)
                .withExpiresAt(exp)
                .sign(algorithm)
            Some(jwt)
        else None
