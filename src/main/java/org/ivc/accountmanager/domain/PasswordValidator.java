/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.domain;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import static org.springframework.util.Assert.notNull;

/**
 *
 * @author Администратор
 */
public class PasswordValidator implements ConstraintValidator<Password, byte[]> {
    
    //-------------------Constants------------------------------------------------
    //-------------------Methods--------------------------------------------------
    @Override
    public void initialize(Password password) {
    }

    @Override
    public boolean isValid(byte[] t, ConstraintValidatorContext cvc) {
        if(t==null){
            return false;
        }
        if(t.length == 0){
            return false;
        }
       return true;
    }

}
