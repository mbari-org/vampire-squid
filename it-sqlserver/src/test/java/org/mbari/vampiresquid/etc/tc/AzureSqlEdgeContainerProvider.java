/*
 * Copyright 2021 MBARI
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

package org.mbari.vampiresquid.etc.tc;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Uses a {@link MSSQLServerContainer} with a `mcr.microsoft.com/azure-sql-edge` image (default: "latest") in place of
 * the standard ` mcr.microsoft.com/mssql/server` image
 */
public class AzureSqlEdgeContainerProvider extends JdbcDatabaseContainerProvider {

  private static final String NAME = "azuresqledge";

  @Override public boolean supports(String databaseType) {
    return databaseType.equals(NAME);
  }

  @Override public JdbcDatabaseContainer newInstance() {
    return newInstance("latest");
  }

  @Override public JdbcDatabaseContainer newInstance(String tag) {
    var taggedImageName = DockerImageName.parse("mcr.microsoft.com/azure-sql-edge")
      .withTag(tag)
      .asCompatibleSubstituteFor(MSSQLServerContainer.IMAGE);
    return new MSSQLServerContainer(taggedImageName).withUrlParam("trustServerCertificate", "true");
  }
}