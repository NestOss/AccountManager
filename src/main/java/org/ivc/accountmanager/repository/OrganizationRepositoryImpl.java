/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.repository;

import java.util.List;
import javax.naming.Name;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.annotation.LogToJournal;
import org.ivc.accountmanager.domain.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;

/**
 * Implementation of OrganizationRepository.
 *
 * @author Roman Osipov
 */
@Repository
public class OrganizationRepositoryImpl implements OrganizationRepository {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    private LdapTemplate ldapTemplate;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Autowired
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    //-------------------Methods--------------------------------------------------
      /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     */
    @Override
    public List<Organization> findAll() {
        return ldapTemplate.findAll(Organization.class);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws IllegalArgumentException - on failure to determine the distinguished name.
     * <p/>
     * throws ConstraintViolationException - on invalid or null organization.
     */
    @Override
    @LogToJournal
    public Organization create(@Valid @NotNull Organization organization) {
        ldapTemplate.create(organization);
        return organization;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws ConstraintViolationException - on empty or null argument.
     */
    @Override
    public boolean isExistsByName(@NotBlank(message = "{emtpy.argument}") String name) {
        return !ldapTemplate.find(query().
                where(Organization.O_ATTRIBUTE).is(name), Organization.class)
                .isEmpty();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws IllegalArgumentException - on failure to determine the distinguished name.
     * <p/>
     * throws ConstraintViolationException - on empty or null argument.
     */
    @Override
    @LogToJournal
    public void delete(@Valid @NotNull Organization organization) {
        ldapTemplate.delete(organization);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws IllegalArgumentException - on failure to determine the distinguished name.
     * <p/>
     * throws ConstraintViolationException - on empty or null arguments.
     */
    @Override
    @LogToJournal
    public void replace(@Valid @NotNull Organization oldOrganization,
            @Valid @NotNull Organization newOrganization) {
        Name oldDn = buildDn(oldOrganization);
        Name newDn = buildDn(newOrganization);
        ldapTemplate.rename(oldDn, newDn);
    }

    protected Name buildDn(Organization p) {
        return LdapNameBuilder.newInstance(Organization.BASE_DN)
                .add(Organization.O_ATTRIBUTE, p.getName())
                .build();
    }
}
