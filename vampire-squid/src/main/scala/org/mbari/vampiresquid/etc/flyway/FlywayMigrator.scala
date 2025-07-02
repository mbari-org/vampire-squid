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

package org.mbari.vampiresquid.etc.flyway

import org.flywaydb.core.Flyway
import org.mbari.vampiresquid.DatabaseParams
import org.mbari.vampiresquid.etc.jdbc.Databases
import org.mbari.vampiresquid.etc.jdk.Logging.given 

import scala.util.Try

object FlywayMigrator:
    
    private val log = System.getLogger(getClass.getName)
    
    def migrate(databaseParams: DatabaseParams): Either[Throwable, Unit] =
        // Implementation of Flyway migration logic
        Try {
            val databaseType = Databases.typeFromUrl(databaseParams.url)
            val location     = databaseType match
                case Databases.DatabaseType.SQLServer  => "classpath:/db/migrations/sqlserver"
                case Databases.DatabaseType.PostgreSQL => "classpath:/db/migrations/postgres"
                case _                                 => throw new IllegalArgumentException(s"Unsupported database type: $databaseType")
                
            log.atInfo.log("Starting Flyway migration using SQL in " + location)
            val flyway       = Flyway
                .configure()
                .baselineOnMigrate(true)
                .dataSource(databaseParams.url, databaseParams.user, databaseParams.password)
                .locations(location)
                .load()

            val result = flyway.migrate()
            if !result.success then throw new Exception("Migration failed using SQL in " + location)
        }.toEither
