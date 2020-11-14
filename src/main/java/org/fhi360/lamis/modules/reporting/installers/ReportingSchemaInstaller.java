package org.fhi360.lamis.modules.reporting.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

@Order(1)
@Installer(name = "reporting-schema-installer", description = "Installs the required database tables", version = 4)
public class ReportingSchemaInstaller extends AcrossLiquibaseInstaller {
    public ReportingSchemaInstaller() {
        super("classpath:installers/reporting/schema/schema.xml");
    }
}
