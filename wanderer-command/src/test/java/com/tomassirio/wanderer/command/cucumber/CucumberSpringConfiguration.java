package com.tomassirio.wanderer.command.cucumber;

import com.tomassirio.wanderer.command.WandererCommandApplication;
import com.tomassirio.wanderer.command.client.WandererAuthClient;
import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(
        classes = WandererCommandApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration extends BaseCucumberSpringConfiguration {

    @MockitoBean private WandererAuthClient wandererAuthClient;
}
