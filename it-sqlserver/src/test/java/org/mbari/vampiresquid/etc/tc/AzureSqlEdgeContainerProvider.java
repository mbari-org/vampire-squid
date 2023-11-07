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