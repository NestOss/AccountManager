/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ivc.accountmanager.service.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.fest.assertions.api.Assertions;
import static org.fest.assertions.api.Assertions.fail;
import static org.ivc.accountmanager.config.Role.ROCKET_ADMIN;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.UserRepository;
import org.ivc.accountmanager.service.GroupAdministratorService;
import org.ivc.accountmanager.service.UserService;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.validation.ConstraintViolationException;
import org.ivc.accountmanager.config.ValidatorConfig;
import org.ivc.accountmanager.service.GroupAdministratorServiceImpl;
import static org.ivc.accountmanager.service.GroupAdministratorServiceImpl.ADMIN_EXIST_ERROR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 * @author Администратор
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class GroupAdministratorServiceImplTest {

    //-------------------Inner classes--------------------------------------------
   @Configuration
   @EnableAutoConfiguration
   @Import({ValidatorConfig.class})
    public static class GroupAdministratorRestControllerTestConfig {

        @Bean
        public UserService beanUserService() {
           String[] sm =  ac.getBeanNamesForType(Object.class);
              System.out.print(sm.toString());
               return mockUserService;
        }

        @Bean
        public UserRepository beanUserRepository() {
            return mockUserRepository;
        }

        @Bean
        public GroupAdministratorService beanGroupAdministratorService() {

            return new GroupAdministratorServiceImpl();
        }
        
        @Autowired
        ApplicationContext ac;

    }

    //-------------------Constants------------------------------------------------
    private static final String EMPTY_LOGIN_ERROR_MESSAGE = "Parameter " + User.JSON_UID_PROPERTY + " can't be an empty string.";
    private static final String EMPTY_GRUOP_ERROR_MASSAGE = "name group can't be an empty string";
    private static final ObjectReader jsonReader = new ObjectMapper().reader();
    private static final String USER_ID = "Badmem";
    private static final String VALID_USER_PASSWORD = "123";
    private static final String PASSWORD = "password";
    private static final String COMMONNAME = "ValUser11";
    private static final String SHORTNAME = "Val";
    private static final String ADMIN_ORGANIZATION_NAME = "Organization11";
    private static final ObjectWriter jsonWriter = new ObjectMapper().writer();
    private static final String ADMIN_ID = "rocketAdmin";
    private static final String VALID_GROUP = "validGroup";
    private final List<User> VALID_USERS
            = new ArrayList<>(Arrays.asList(new User[]{
                new User("000", "000", "Admin user", "Admin", "org1", true),
                new User("111", "000", "Simple user", "Simple", "org2", true)}));
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String EMPTY_PASSWORD_ERROR_MESSAGE
            = "Parameter " + User.PASSWORD_PROPERTY + " can't be an empty string.";
    //-------------------Fields--------------------------------------------------- 
    private User VALID_USER = new User(USER_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, "AAAAA", true);
    private User rocketAdmin = new User(ADMIN_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, ADMIN_ORGANIZATION_NAME, true);

    private static UserService mockUserService = mock(UserService.class);
    private static UserRepository mockUserRepository = mock(UserRepository.class);

    @Autowired
    private GroupAdministratorService groupAdministratorService;

    

    //-------------------Before tests------------------------------------------
    @Before
    public void setup() {
        reset(mockUserService, mockUserRepository);
      
    }

    //-------------------Tests-------------------------------------------------
    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testShouldCreateUser() throws Exception {
        rocketAdmin.setOrganizationName(ADMIN_ORGANIZATION_NAME);
        User controlUser = new User(USER_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, "AAAAA", true);
        when(mockUserRepository.findByUid(ADMIN_ID)).thenReturn(Optional.of(rocketAdmin));
        controlUser.setOrganizationName(ADMIN_ORGANIZATION_NAME);
        controlUser.setRole(VALID_GROUP);
        when(mockUserService.create(VALID_USER)).thenReturn(VALID_USER);
        assertEquals(controlUser, groupAdministratorService.createUser(VALID_USER, VALID_GROUP));
        verify(mockUserService).create(controlUser);
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void createUserShouldThrowsEmptyGroupNullExcaption() {
        groupAdministratorService.createUser(VALID_USER, null);
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void CreateUserShouldThrowsEmptyGroupConstraintViolationException() {
        groupAdministratorService.createUser(VALID_USER, "");
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testGetUserListBy() throws Exception {
        User rocketAdmin = new User();
        rocketAdmin.setOrganizationName(ADMIN_ORGANIZATION_NAME);
        VALID_USERS.get(0).setDn(null);
        VALID_USERS.get(0).setPasswordWithoutEncoding(null);
        VALID_USERS.get(1).setDn(null);
        VALID_USERS.get(1).setPasswordWithoutEncoding(null);
        List<User> listGroupUser = new LinkedList<>(VALID_USERS);
        listGroupUser.add(new User("User1", PASSWORD, COMMONNAME, SHORTNAME, COMMONNAME, true));
        listGroupUser.add(new User("User2", PASSWORD, COMMONNAME, SHORTNAME, COMMONNAME, true));
        listGroupUser.add(new User("User3", PASSWORD, COMMONNAME, SHORTNAME, COMMONNAME, true));
        when(mockUserService.findByGroupName(VALID_GROUP)).thenReturn(listGroupUser);
        List<User> listOrganizationUser = new LinkedList<>(VALID_USERS);
        when(mockUserRepository.findByOrganization(ADMIN_ORGANIZATION_NAME)).thenReturn(listOrganizationUser);
        when(mockUserRepository.findByUid(ADMIN_ID)).thenReturn(Optional.of(rocketAdmin));
        List<User> users = groupAdministratorService.UserList(VALID_GROUP);
        Assertions.assertThat(users).hasSize(VALID_USERS.size()).
                containsOnly(VALID_USERS.toArray(new User[0]));
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void getUserListShouldThrowsEmptyGroupIllegalArgumentExcaption() {
        groupAdministratorService.UserList("");
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void getUserListShouldThrowsEmptyGroupNullExcaption() {
        groupAdministratorService.UserList(null);
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testDeleteUser() throws Exception {
        when(mockUserRepository.findByUid(VALID_USER.getId())).thenReturn(Optional.of(VALID_USER));
        groupAdministratorService.deleteUser(VALID_USER);
        verify(mockUserService).delete(VALID_USER);
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void deleteUserListShouldThrowsEmptyGroupIllegalArgumentExcaption() {
        try {
            VALID_USER.setId("");
            groupAdministratorService.deleteUser(VALID_USER);
            fail("Has not generated an exception ");
        } catch (IllegalArgumentException e) {
            assertEquals(EMPTY_LOGIN_ERROR_MESSAGE, e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void deleteUserShouldThrowsEmptyGroupNullExcaption() {
        try {
            VALID_USER.setId(null);
            groupAdministratorService.deleteUser(VALID_USER);
            fail("Has not generated an exception ");
        } catch (NullPointerException e) {
            assertEquals(EMPTY_LOGIN_ERROR_MESSAGE, e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testUpdateUser() throws Exception {
        //arrange
        User rocketAdmin = new User();
        User old = new User(USER_ID, VALID_USER_PASSWORD, "BBB", "BBB", "BBB", true);
        rocketAdmin.setOrganizationName(ADMIN_ORGANIZATION_NAME);
        rocketAdmin.setId(ADMIN_ID);
        List<User> groupList = new LinkedList<>();
        groupList.add(old);
        List<User> organizationList = new LinkedList<>();
        organizationList.add(old);
        when(mockUserRepository.findByUid(ADMIN_ID)).thenReturn(Optional.of(rocketAdmin));
        when(mockUserRepository.findByUid(VALID_USER.getId())).thenReturn(Optional.of(old));
        when(mockUserRepository.findByOrganization(ADMIN_ORGANIZATION_NAME)).thenReturn(organizationList);
        when(mockUserService.findByGroupName(VALID_GROUP)).thenReturn(groupList);
        //act
        groupAdministratorService.updateUser(old, VALID_GROUP);
        old.setCommonName(VALID_USER.getCommonName());
        old.setShortName(VALID_USER.getShortName());
        old.setActive(VALID_USER.isActive());
        //assert
        verify(mockUserRepository).update(old);
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void updateUserShouldThrowsEmptyGroupIllegalArgumentException() {
        groupAdministratorService.updateUser(VALID_USER, "");
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void updateUserShouldThrowsEmptyGroupNullExcaption() {
        groupAdministratorService.updateUser(VALID_USER, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserPassword() throws Exception {
        // arrange
        User user = new User(USER_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, "AAAAA", true);
        when(mockUserService.changePassword(user))
                .thenReturn(user);
        // act & assert
        User receivedUser = groupAdministratorService.changePassword(user);
        user.setPassword(VALID_USER_PASSWORD);
        verify(mockUserService).changePassword(user);
        assertEquals(user, receivedUser);
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void changeUserPasswordShouldThrowsEmptyGroupIllegalArgumentException() {
        try {
            VALID_USER.setId("");
            groupAdministratorService.changePassword(VALID_USER);
            fail("Has not generated an exception ");
        } catch (IllegalArgumentException e) {
            assertEquals(EMPTY_LOGIN_ERROR_MESSAGE, e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void changeUserPasswordShouldThrowsEmptyGroupNullExcaption() {
        try {
            VALID_USER.setId(null);
            groupAdministratorService.changePassword(VALID_USER);
            fail("Has not generated an exception ");
        } catch (NullPointerException e) {
            assertEquals(EMPTY_LOGIN_ERROR_MESSAGE, e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void hangeUserPasswordShouldThrowsForNullPassword() {
        try {
            VALID_USER.setPassword(null);
            groupAdministratorService.changePassword(VALID_USER);
            fail("Has not generated an exception ");
        } catch (IllegalArgumentException e) {
            assertEquals(EMPTY_PASSWORD_ERROR_MESSAGE, e.getMessage());
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void createUserShouldThrowsNullUserException() {
        groupAdministratorService.createUser(null, VALID_GROUP);
    }

    @Test(expected = ConstraintViolationException.class)
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void updateUserShouldThrowsNullUserException() {
        groupAdministratorService.updateUser(null, VALID_GROUP);
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void creatUserShouldThrowExceptionByEmptyAdmin() {
        // arrange
        when(mockUserRepository.findByUid(ADMIN_ID)).thenReturn(Optional.empty());
        // act & assert
        try {

            groupAdministratorService.createUser(VALID_USER, VALID_GROUP);
        } catch (IllegalArgumentException e) {
            assertEquals(ADMIN_EXIST_ERROR, e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void updateUserShouldThrowExceptionByEmptyAdmin() {
        // arrange
        when(mockUserRepository.findByUid(ADMIN_ID)).thenReturn(Optional.empty());
        // act & assert
        try {
            groupAdministratorService.updateUser(VALID_USER, VALID_GROUP);
        } catch (IllegalArgumentException e) {
            assertEquals(ADMIN_EXIST_ERROR, e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void userListShouldThrowExceptionByEmptyAdmin() {
        // arrange
        when(mockUserRepository.findByUid(ADMIN_ID)).thenReturn(Optional.empty());
        // act & assert
        try {
            groupAdministratorService.UserList(VALID_GROUP);
        } catch (IllegalArgumentException e) {
            assertEquals(ADMIN_EXIST_ERROR, e.getMessage());
        }
    }

}
