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

package org.mbari.vampiresquid.etc.jdbc

object Databases:

    enum DatabaseType(val name: String):
        case PostgreSQL extends DatabaseType("postgresql")
        case Oracle     extends DatabaseType("oracle")
        case SQLServer  extends DatabaseType("sqlserver")

    /**
     * Infer the database type from a JDBC URL.
     * @param url
     *   The JDBC URL to infer the type from.
     * @return
     *   The inferred DatabaseType.
     */
    def typeFromUrl(url: String): DatabaseType =
        if url.contains("postgresql") then DatabaseType.PostgreSQL
        else if url.contains("oracle") then DatabaseType.Oracle
        else if url.contains("sqlserver") then DatabaseType.SQLServer
        else throw new IllegalArgumentException(s"Unknown database type for URL: $url")
