package org.fhi360.lamis.modules.reporting;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;

@AcrossDepends(required = AcrossHibernateJpaModule.NAME)
public class ReportModule extends AcrossModule {
    public static final String NAME = "ReportModule";

    public ReportModule() {
        super();
        addApplicationContextConfigurer(
                new ComponentScanConfigurer(getClass().getPackage().getName() + ".web",
                        getClass().getPackage().getName() + ".mapper", getClass().getPackage().getName() + ".service"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
