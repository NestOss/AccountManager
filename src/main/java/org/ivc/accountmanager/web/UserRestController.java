/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.web;

import java.util.List;
import javax.validation.Valid;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.service.UserService;
import static org.ivc.accountmanager.web.UserRestController.USERS_PATH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author Sokolov@ivc.org
 */
@RestController
@RequestMapping(path = USERS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRestController {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    public static final String USERS_PATH = "/users";
    public static final String ITEM_PATH = "/item";
    public static final String PASS_PATH = "/pass";
    public static final String EMPTY_LOGIN_ERROR_MESSAGE
            = "Parameter " + User.JSON_UID_PROPERTY + " can't be an empty string.";
    public static final String EMPTY_SHORT_NAME_ERROR_MESSAGE
            = "Parameter " + User.SHORT_NAME_PROPERTY + " can't be an empty string.";
    public static final String EMPTY_COMMON_NAME_ERROR_MESSAGE
            = "Parameter " + User.COMMON_NAME_PROPERTY + " can't be an empty string.";
    public static final String EMPTY_ORGANIZATION_NAME_ERROR_MESSAGE
            = "Parameter " + User.ORGANIZATION_NAME_PROPERTY + " can't be an empty string.";
    public static final String EMPTY_ROLE_ERROR_MESSAGE = "Parameter role can't be an empty string.";
    public static final String EMPTY_PASSWORD_ERROR_MESSAGE
            = "Parameter " + User.PASSWORD_PROPERTY + " can't be an empty string.";

    //-------------------Fields---------------------------------------------------
    private UserService userService;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Autowired(required = false) // False for test purpose.
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    //-------------------Methods--------------------------------------------------
    @RequestMapping(method = RequestMethod.GET)
    public List<User> getUserList() {
        return userService.findAll();
    }

    @RequestMapping(method = RequestMethod.POST,
            path = ITEM_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }

    @RequestMapping(method = RequestMethod.PUT,
            path = ITEM_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public User updateUser(@RequestBody User user) {
        return userService.update(user);
    }

    @RequestMapping(method = RequestMethod.PUT,
            path = PASS_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public User changeUserPassword(@RequestBody User user) {
        return userService.changePassword(user);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            path = ITEM_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteUser(@RequestBody User user) {
        userService.delete(user);
    }

}
