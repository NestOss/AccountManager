/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ivc.accountmanager.service;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.UserRepository;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author Администратор
 */
@Validated
public interface GroupAdministratorService {
    
    //-------------------Constants------------------------------------------------
    String EMPTY_GRUOP_ERROR_MASSAGE = "name group can't be an empty string";
    //-------------------Methods--------------------------------------------------
    /**
     * Returns the list of user from  group.
     *
     * @return
     */
    List<User> UserList(@NotBlank(message = EMPTY_GRUOP_ERROR_MASSAGE) String group);
     /**
     * Deleted user from system.
     *
     */
    void deleteUser(@NotNull @Validated(User.IdGroup.class) User user);
     /**
     * Create sensor user in  group.
     *
     */
    User createUser(@NotNull @Valid  User user,
            @NotBlank(message = EMPTY_GRUOP_ERROR_MASSAGE) String group);
    
      /**
     * Updates the user. Before checks presence of the user in lists of the organisation and in
     * group rocketuser.
     *
     */
    User updateUser(@Valid @NotNull User user,
            @NotBlank(message = EMPTY_GRUOP_ERROR_MASSAGE)String group);
     /**
     * Changes the password of the user.
     *
     * @param user - the object contains the information on login of the user and its new password.
     * @return
     */
    User changePassword(@NotNull @Validated(User.PairLoginPassword.class) User user);
}
