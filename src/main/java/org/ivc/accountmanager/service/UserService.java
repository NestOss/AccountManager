/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.service;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.domain.User;
import org.springframework.validation.annotation.Validated;

/**
 * User service.
 *
 * @author Sokolov@ivc.org
 */
@Validated
public interface UserService {

    /**
     * Returns the list of users with its roles. If user hasn't have a role, then assign null to its
     * role.
     *
     * @return the list of users with its roles.
     */
    List<User> findAll();

    /**
     * Create a new user. Its organization should exists.
     *
     * @param user a new user.
     * @return created user.
     */
    User create(@Valid @NotNull User user);

    /**
     * Removes the user and delete reference from his group.
     *
     * @param user the user to remove.
     */
    void delete(@NotNull @Validated(User.IdGroup.class) User user);

    /**
     * Update user.
     *
     * @param user updating user(with changed fields).
     * @return updated user.
     */
    User update(@Valid @NotNull User user);

    /**
     * Returns all users who are members of the group.
     *
     * @param groupName the group name.
     * @return the user list, who are members of the group.
     */
    List<User> findByGroupName(@NotBlank(message = "{empty.argument}") String groupName);

    /**
     * Change user's password.
     *
     * @param user user with updated password.
     * @return user with changed password.
     */
    User changePassword(@NotNull User user);
}
