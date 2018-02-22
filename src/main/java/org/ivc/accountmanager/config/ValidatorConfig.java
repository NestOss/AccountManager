/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Validator configuration.
 *
 * @author Roman Osipov
 */
@Configuration
public class ValidatorConfig {

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public ValidUtils validUtils(Validator validator) {
        return new ValidUtils(validator);
    }

}
