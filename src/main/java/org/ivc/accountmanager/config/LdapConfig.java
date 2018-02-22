/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Ldap configuration class.
 *
 * @author Roman Osipov
 */
@Configuration
public class LdapConfig {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    @Value("${security.ldap.host}")
    private String ldapHost;

    @Value("${security.ldap.port}")
    private Integer ldapPort;

    @Value("${security.ldap.password}")
    private String ldapPassword;

    @Value("${security.ldap.manager}")
    private String ldapManager;

    public static final String LDAP_BASE = "o=ivc";

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------
    //-------------------Beans-----------------------------------------------------
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://" + ldapHost + ":" + ldapPort);
        contextSource.setBase(LDAP_BASE);
        contextSource.setUserDn(ldapManager);
        contextSource.setPassword(ldapPassword);
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }
}
