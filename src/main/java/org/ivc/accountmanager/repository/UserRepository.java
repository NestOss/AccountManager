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
import org.springframework.validation.annotation.Validated;

/**
 * User Repository.
 *
 * @author Sokolov@ivc.org
 */
@Validated
public interface UserRepository {

    /**
     * Returns the list of the users.
     *
     * @return the list of the users.
     */
    List<User> findAll();

    /**
     * Create the user.
     *
     * @param user the user to create.
     * @return the created user.
     */
    User create(@Valid @NotNull User user);

    /**
     * Find the optional user by it's uid.
     *
     * @param uid the user uid.
     * @return the optional user with found user or empty optional, if user was not found.
     */
    Optional<User> findByUid(@NotBlank(message = "{empty.argument}") String uid);

    /**
     * Update the user.
     *
     * @param user the user update.
     */
    void update(@Valid @NotNull User user);

    /**
     * Delete the user.
     *
     * @param user the user to delete.
     */
    void delete(@NotNull User user);

    /**
     * Search users who are members of the organization.
     * @param organization the organization.
     * @return the user list, who are members of the organization.
     */
    List<User> findByOrganization(@NotBlank(message = "{empty.argument}") String organization);
}
