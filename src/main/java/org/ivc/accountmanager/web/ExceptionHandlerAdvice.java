/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.web;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.ivc.accountmanager.domain.RestError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Rest services global exception handler.
 *
 * @author Roman Osipov
 */
@ControllerAdvice
public class ExceptionHandlerAdvice {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    private Validator validator;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    //-------------------Methods--------------------------------------------------
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<RestError> exception(Exception exception) {
        String message = null;
        if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) exception;
            FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
            message = fieldError.getField() + " " + fieldError.getDefaultMessage();
        } else if (exception instanceof ConstraintViolationException) {
            // revalidate, because ConstraintViolation not show invalid field name
            ConstraintViolationException ex = (ConstraintViolationException) exception;
            ConstraintViolation<?>[] constraintViolations
                    = ex.getConstraintViolations().toArray(new ConstraintViolation<?>[0]);
            ConstraintViolation<?> constraintViolation = constraintViolations[0];
            Object bean = constraintViolation.getLeafBean();
            Errors errors = new BeanPropertyBindingResult(bean, "");
            validator.validate(bean, errors);
            if (errors.hasErrors()) {
                FieldError error = errors.getFieldError();
                message = String.format("%s %s %s",
                        error.getObjectName(), error.getField(), error.getDefaultMessage());
            }
        } else {
            message = exception.getMessage();
        }
        RestError restError = new RestError(exception.getClass().getCanonicalName(),
                message);
        return new ResponseEntity<>(restError, HttpStatus.BAD_REQUEST);
    }
}
