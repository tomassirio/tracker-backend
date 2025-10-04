package com.tomassirio.wanderer.commons.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan("com.tomassirio.wanderer.commons.domain")
public class CommonsJpaConfiguration {}
