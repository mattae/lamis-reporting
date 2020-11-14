package org.fhi360.lamis.modules.reporting.service.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RunningPortService {
    private final Environment environment;

    public String getPort() {
        return environment.getProperty("local.server.port");
    }
}
