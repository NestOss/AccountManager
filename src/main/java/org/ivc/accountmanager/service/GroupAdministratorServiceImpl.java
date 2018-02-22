/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.Validate;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import static org.springframework.util.Assert.notNull;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author Администратор
 */
@Service
public class GroupAdministratorServiceImpl implements GroupAdministratorService {
  //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    
    private static final String NULL_HASH = "{sha256}pmWkWSBCL51Bfkhn79xPuKBKHz//H6B+mY6G9/eieuM=";
    private static final String EMPTY_LOGIN_ERROR_MESSAGE = "Parameter " + User.JSON_UID_PROPERTY + " can't be an empty string.";
    private static final String EMPTY_PASSWORD_ERROR_MESSAGE
            = "Parameter " + User.PASSWORD_PROPERTY + " can't be an empty string.";
    public static final String ADMIN_EXIST_ERROR = "The admin does not exist";
    //-------------------Fields---------------------------------------------------
    UserService userService;
    UserRepository userRepository;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Autowired(required = false)
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired(required = false)
    public void setUserControlService(UserService userControlService) {
        this.userService = userControlService;
    }
    //-------------------Methods--------------------------------------------------
    /**
     * {@inheritDoc}
     * </p>
     * throws ConstraintViolationException - return 
     * throws ConstraintViolationException - on empty or null value of group.
     * throws IllegalArgumentException - on not found administrator.
     */
    @Override
    public List<User> UserList(@NotBlank(message = EMPTY_GRUOP_ERROR_MASSAGE)String group) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> o = userRepository.findByUid(userDetails.getUsername());
        User admin = (o.isPresent())? o.get():null;
        notNull(admin,ADMIN_EXIST_ERROR);
        String orgName = admin.getOrganizationName();
        List<User> resultListByOrganisation = userRepository.findByOrganization(orgName);
        List<User> resultListByGroup = userService.findByGroupName(group);
        List<User> resultList = new LinkedList<>();
        int j = 0;
        for (int i = 0; i < resultListByOrganisation.size(); i++) {
            if (resultListByGroup.indexOf(resultListByOrganisation.get(i)) != -1) {
                resultList.add(resultListByOrganisation.get(i));
            }
        }
        return resultList;
    }
     /**
     * {@inheritDoc}
     * </p>
     * throws NullPointerException - on failure to determine the user as null.
     * 
     */
    @Override
    public void deleteUser(@NotNull @Validated(User.IdGroup.class) User user) {
        Validate.notEmpty(user.getId(), EMPTY_LOGIN_ERROR_MESSAGE);
        userService.delete(user);
    }
     /**
     * {@inheritDoc}
     * <p/>
     * throws NullPointerException - on failure to determine the user as null.
     * throws ConstraintViolationException - on invalid user.
     * throws ConstraintViolationException - on empty or null value of group.
     * throws IllegalArgumentException - on not found administrator.
     */
    @Override
    public User createUser(@NotNull @Valid  User user,
            @NotBlank(message = EMPTY_GRUOP_ERROR_MASSAGE) String group) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String orgName =  userDetails.getUsername();
        Optional<User> o = userRepository.findByUid(userDetails.getUsername());
        User admin = (o.isPresent())? o.get():null;
        notNull(admin,ADMIN_EXIST_ERROR);
        user.setOrganizationName(admin.getOrganizationName());
        user.setRole(group);
        return userService.create(user);
    }
     /**
     * {@inheritDoc}
     * <p/>
     * throws NullPointerException - on failure to determine the user as null.
     * throws ConstraintViolationException - on invalid user.
     * throws ConstraintViolationException - on empty or null value of group.
     * throws IllegalArgumentException - on not found administrator.
     */
    @Override
    public User updateUser(@Valid @NotNull User user,
            @NotBlank(message = EMPTY_GRUOP_ERROR_MASSAGE)String group) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> o = userRepository.findByUid(userDetails.getUsername());
        User admin = (o.isPresent())? o.get():null;
        notNull(admin,ADMIN_EXIST_ERROR);
        User oldUser =userRepository.findByUid(user.getId()).get();
        notNull(oldUser,"User is not found");
        List<User> users = userRepository.findByOrganization(admin.getOrganizationName());
        List<User> resultListByGroup = userService.findByGroupName(group);
        if (users.contains(oldUser) && resultListByGroup.contains(oldUser)) {
            oldUser.setCommonName(user.getCommonName());
            oldUser.setShortName(user.getShortName());
            oldUser.setActive(user.isActive());
            userRepository.update(oldUser);
        }
        return user;
    }
     /**
     * {@inheritDoc}
     * <p/>
     * throws NullPointerException - on failure to determine the user as null.
     * 
     */
    @Override
    public User changePassword(@NotNull @Validated(User.PairLoginPassword.class) User user) {
        Validate.notEmpty(user.getId(), EMPTY_LOGIN_ERROR_MESSAGE);
        System.out.print(user.getPassword());
        // Check password's hash. Throw exception if password's hash equal null hash.
        Validate.isTrue(user.getPassword().equals(NULL_HASH), EMPTY_PASSWORD_ERROR_MESSAGE);
        return userService.changePassword(user);
    }
}
