package com.naisa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Represents the database configuration.
 *
 * <p>This configuration component lets Spring read properties from database.properties in the
 * resources directory (i.e., src/main/resources relative to the Spring Boot project's root
 * directory). Because the properties file contains database credentials, it should not be added to
 * version control (i.e., Git). Therefore, it has been added to .gitignore.
 *
 * <p>The properties in database.properties are used by Hibernate and other frameworks. Therefore,
 * this class has no fields.
 *
 * @author James Chen
 */
@Configuration
@PropertySource("classpath:database.properties")
class DatabaseConfiguration {}
