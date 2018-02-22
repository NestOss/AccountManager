/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

/**
 *
 * @author Roman Osipov
 */
public class ValidUtils {
    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    private Validator validator;
    
    //-------------------Constructors---------------------------------------------
    @Autowired
    public ValidUtils(Validator validator) {    
        this.validator = validator;
    }

    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------
    /**
     * Validate bean.
     *
     * @param bean the bean.
     * @param beanName the bean name.
     * @throws IllegalArgumentException if bean fields values is not valid.
     */
    public void validateBean(Object bean, String beanName) {
        Errors errors = new BeanPropertyBindingResult(bean, beanName);
        validator.validate(bean, errors);
        if (errors.hasErrors()) {
            FieldError error = errors.getFieldError();
            String message = String.format("%s %s %s",
                    error.getObjectName(), error.getField(), error.getDefaultMessage());
            throw new IllegalArgumentException(message);
        }
    }
}
