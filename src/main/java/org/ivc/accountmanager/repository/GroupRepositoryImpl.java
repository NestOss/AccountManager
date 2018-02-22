/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.repository;

import java.util.List;
import java.util.Optional;
import javax.naming.Name;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.domain.Group;
import org.ivc.accountmanager.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the GroupRepository.
 *
 * @author Sokolov@ivc.org
 */
@Repository
public class GroupRepositoryImpl implements GroupRepository {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    private LdapTemplate ldapTemplate;
    //-------------------Constructors---------------------------------------------

    //-------------------Getters and setters--------------------------------------
    @Autowired(required = false) // for tests purpose
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
    public List<Group> findAll() {
        return ldapTemplate.findAll(Group.class);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws ConstraintViolationException - on empty or null argument.
     * <p/>
     * NamingException - on LDAP error.
     */
    @Override
    public Optional<Group> findByCn(@NotBlank(message = "{emtpy.argument}") String cn) {
        List<Group> groups
                = ldapTemplate.find(query().where(Group.CN_ATTRIBUTE).is(cn), Group.class);
        return (groups.isEmpty()) ? Optional.empty() : Optional.of(groups.get(0));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws ConstraintViolationException - on null argument.
     */
    @Override
    public Optional<Group> findByMember(
            @NotBlank(message = "{empty.argument}") String userUID) {
        Name userDn = LdapNameBuilder.newInstance(LdapConfig.LDAP_BASE).add(User.BASE_DN)
                .add(User.UID_ATTRIBUTE, userUID).build();
        List<Group> groups = ldapTemplate.find(query()
                .where(Group.MEMBER_ATTRIBUTE).is(userDn.toString()), Group.class);
        return (groups.isEmpty()) ? Optional.empty() : Optional.of(groups.get(0));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws ConstraintViolationException - on invalid or null argument.
     * <p/>
     * throws NameNotFoundException - on invalid or null argument.
     */
    @Override
    public void update(@Valid @NotNull Group group) {
        ldapTemplate.update(group);
    }
}
