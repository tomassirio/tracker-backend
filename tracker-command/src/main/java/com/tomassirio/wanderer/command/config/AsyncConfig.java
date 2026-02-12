package com.tomassirio.wanderer.command.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for asynchronous event processing.
 *
 * <p>Enables @Async annotation support for event listeners, allowing domain events to be processed
 * asynchronously without blocking the main transaction.
 */
@Configuration
@EnableAsync
public class AsyncConfig {}
