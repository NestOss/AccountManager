/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.web;

import java.util.List;
import javax.validation.Valid;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.service.GroupAdministratorService;
import static org.ivc.accountmanager.web.GroupAdministratorRestController.GROUP_ADMINISTRATOR_PATH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Администратор
 */
@RestController
@RequestMapping(path = GROUP_ADMINISTRATOR_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupAdministratorRestController {
  //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    public final static String GROUP_ADMINISTRATOR_PATH = "/group_administrator";
    public static final String ITEM_PATH = "/item";
    public static final String ROCKET_ADMIN_DIRECTORY = "/rocket";
    public static final String SENSOR_ADMIN_DIRECTORY = "/sensor";
    public static final String ROCKET_ADMIN_NAME = "rocketadmin";
    public static final String SENSOR_ADMIN_NAME = "sensoradmin";
    public static final String UPDATE_USER = "/updateuser";
    private static final String ROCKET_USER = "rocketuser";
    private static final String SENSOR_USER = "sensoruser";
    private static final String PASS_PATH = "/pass";
  
    //-------------------Fields---------------------------------------------------
    GroupAdministratorService groupAdministratorService;
    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
   
     @Autowired(required = false)
    public void setGroupAdministratorService(GroupAdministratorService groupAdministratorService) {
        this.groupAdministratorService = groupAdministratorService;
    }
    
    //-------------------Methods--------------------------------------------------
    /**
     * Returns the list of user from rocketuser group.
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = ROCKET_ADMIN_DIRECTORY)
    public List<User> getUserListByRocketAdmin() {
        return groupAdministratorService.UserList(ROCKET_USER);
    }

    /**
     * Returns the list of user from sensoruser group.
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = SENSOR_ADMIN_DIRECTORY)
    public List<User> getUserListBySensorAdmin() {
        return groupAdministratorService.UserList(SENSOR_USER);
     }

    /**
     * Deleted user from system.
     *
     * @param user - user which should be removed.
     */
    @RequestMapping(method = RequestMethod.DELETE, path = ITEM_PATH)
    public void deleteUser(@RequestBody User user) {
        groupAdministratorService.deleteUser(user);
    }

    /**
     * Create sensor user in rocketuser group.
     *
     * @param user - new user in rocketuser group.
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = ROCKET_ADMIN_DIRECTORY)
    public User createRocketUser(@RequestBody User user) {
        return groupAdministratorService.createUser(user,ROCKET_USER);
    }

    /**
     * Create sensor user in sensoruser group.
     *
     * @param user - new user in sensoruser group.
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = SENSOR_ADMIN_DIRECTORY)
    public User createSensorUser(@RequestBody User user) {
        return groupAdministratorService.createUser(user,SENSOR_USER);
    }
    

    /**
     * Updates the user. Before checks presence of the user in lists of the organisation and in
     * group rocketuser.
     *
     * @param user - new user value.
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, path = ROCKET_ADMIN_DIRECTORY)
    public User updateRocketUser(@RequestBody User user) {
        return groupAdministratorService.updateUser(user,ROCKET_USER);
    }

    /**
     * Updates the user. Before checks presence of the user in lists of the organisation and in
     * group sensoruser.
     *
     * @param user - new user value.
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, path = SENSOR_ADMIN_DIRECTORY)
    public User updateSensorUser(@RequestBody User user) {
        return groupAdministratorService.updateUser(user,SENSOR_USER);
    }
    


    /**
     * Changes the password of the user.
     *
     * @param user - the object contains the information on login of the user and its new password.
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, path = PASS_PATH)
    public User changeUserPassword(@RequestBody User user) {
        return groupAdministratorService.changePassword(user);
    }
    

}
