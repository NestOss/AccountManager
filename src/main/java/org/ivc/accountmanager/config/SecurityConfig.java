/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.config;

import java.nio.charset.StandardCharsets;
import org.ivc.accountmanager.web.OrganizationRestController;
import org.ivc.accountmanager.web.UserRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Security Configuration.
 *
 * @author Roman Osipov
 */
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //------------------------Constants----------------------------------------
    public static final String ORGANIZATIONS_PAGE = "/organizations_page";
    public static final String USERS_PAGE = "/users_page";

    //------------------------Fields-------------------------------------------
    @Value("${security.ldap.host}")
    private String ldapHost;

    @Value("${security.ldap.port}")
    private Integer ldapPort;

    @Value("${security.ldap.password}")
    private String ldapPassword;

    @Value("${security.ldap.manager}")
    private String ldapManager;

    //------------------------Methods------------------------------------------
    @Override
    public void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding(StandardCharsets.UTF_8.name());
        encodingFilter.setForceEncoding(true);
        http
                .addFilterAfter(new CsrfTokenGeneratorFilter(), CsrfFilter.class)
                .addFilterBefore(encodingFilter, CsrfFilter.class)
                .authorizeRequests()
                .antMatchers(OrganizationRestController.ORGANIZATIONS_PATH + "/**").hasRole(Role.ADMIN)
                .antMatchers(UserRestController.USERS_PATH + "/**").hasRole(Role.ADMIN)
                .antMatchers(ORGANIZATIONS_PAGE).hasRole(Role.ADMIN)
                .antMatchers(USERS_PAGE).hasRole(Role.ADMIN)
                .anyRequest().authenticated()
                .and()
                .formLogin();
//                .and()
//                .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        ShaPasswordEncoder spe = new PrefixShaPasswordEncoder(256);
        spe.setEncodeHashAsBase64(true);
        auth
                .ldapAuthentication()
                .userSearchBase("ou=users")
                .userSearchFilter("(&(title=TRUE)(uid={0}))")
                .groupSearchBase("ou=groups")
                .groupSearchFilter("member={0}")
                .passwordCompare()
                .passwordEncoder(spe)
                .passwordAttribute("userPassword").and()
                .contextSource()
                .url("ldap://" + ldapHost + ":" + ldapPort + "/" + LdapConfig.LDAP_BASE)
                .managerDn(ldapManager)
                .managerPassword(ldapPassword);
    }
}
