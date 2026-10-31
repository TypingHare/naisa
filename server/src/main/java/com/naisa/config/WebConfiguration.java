package com.naisa.config;

import com.naisa.interceptor.AuthorizationInterceptor;
import com.naisa.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Represents general web configuration.
 *
 * <p>
 * This configuration class implements {@code WebMvcConfigurer} and adds interceptors to the
 * service.
 *
 * @author James Chen
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
	private final LoggingInterceptor loggingInterceptor;
	private final AuthorizationInterceptor authorizationInterceptor;

	public WebConfiguration(
		final LoggingInterceptor loggingInterceptor,
		final AuthorizationInterceptor authorizationInterceptor
	) {
		this.loggingInterceptor = loggingInterceptor;
		this.authorizationInterceptor = authorizationInterceptor;
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(loggingInterceptor);
		registry.addInterceptor(authorizationInterceptor);
	}
}
