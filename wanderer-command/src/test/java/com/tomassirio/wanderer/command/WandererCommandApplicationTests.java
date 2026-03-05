package com.tomassirio.wanderer.command;

import com.tomassirio.wanderer.commons.BaseIntegrationTest;
import com.tomassirio.wanderer.commons.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(TestConfig.class)
class WandererCommandApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {}
}
