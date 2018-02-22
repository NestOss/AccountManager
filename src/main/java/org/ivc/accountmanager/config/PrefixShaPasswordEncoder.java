/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.config;

import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

/**
 *
 * @author Roman
 */
public class PrefixShaPasswordEncoder extends ShaPasswordEncoder {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    private final int strength;

    //-------------------Constructors---------------------------------------------
    public PrefixShaPasswordEncoder(int strength) {
        super(strength);
        this.strength = strength;
    }

    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------
    @Override
    public String encodePassword(String password, Object salt) {
        return "{sha" + strength + "}" + super.encodePassword(password, salt);
    }

}
