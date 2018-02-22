/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.springframework.stereotype.Repository;

/**
 * Implementation UserRepository.
 *
 * @author Sokolov@ivc.org
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

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
    @Override
    public List<User> findAll() {
        return ldapTemplate.findAll(User.class);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws IllegalArgumentException - on failure to determine the distinguished name.
     * <p/>
     * throws ConstraintViolationException - on invalid or null user.
     */
    @Override
    public User create(@Valid @NotNull User user) {
        ldapTemplate.create(user);
        return user;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws ConstraintViolationException - on empty or null uid argument.
     */
    @Override
    public Optional<User> findByUid(@NotBlank(message = "{empty.argument}") String uid) {
        List<User> users = ldapTemplate.find(query().where(User.UID_ATTRIBUTE).is(uid), User.class);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws IllegalArgumentException - on failure to determine the distinguished name.
     * <p/>
     * throws ConstraintViolationException - on invalid or null user.
     * <p/>
     * throws NameNotFoundException - on absent updating user in directory.
     */
    @Override
    public void update(@Valid @NotNull User user) {
        ldapTemplate.update(user);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws ConstraintViolationException - on null user.
     */
    @Override
    public void delete(@NotNull User user) {
        ldapTemplate.delete(user);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws ConstraintViolationException - on invalid or null user.
     */
    @Override
    public List<User> findByOrganization(
            @NotBlank(message = "{empty.argument}") String organization) {
        return ldapTemplate.find(query().where(User.O_ATTRIBUTE).is(organization), User.class);
    }
}
