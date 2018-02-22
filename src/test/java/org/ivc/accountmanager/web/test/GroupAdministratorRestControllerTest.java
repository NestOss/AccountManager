/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ivc.accountmanager.web.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections.IteratorUtils;
import org.fest.assertions.api.Assertions;
import static org.hamcrest.Matchers.is;
import static org.ivc.accountmanager.config.Role.*;
import org.ivc.accountmanager.config.SecurityConfig;
import org.ivc.accountmanager.domain.RestError;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.service.GroupAdministratorService;
import org.ivc.accountmanager.web.ExceptionHandlerAdvice;
import org.ivc.accountmanager.web.GroupAdministratorRestController;
import static org.ivc.accountmanager.web.GroupAdministratorRestController.*;
import org.ivc.accountmanager.web.TrimmerAdvice;
import static org.ivc.accountmanager.web.UserRestController.PASS_PATH;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.web.context.WebApplicationContext;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author Администратор
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class GroupAdministratorRestControllerTest {

    //-------------------Inner classes--------------------------------------------
    @SpringBootApplication
    @Import({SecurityConfig.class, GroupAdministratorRestController.class, ExceptionHandlerAdvice.class,
        TrimmerAdvice.class})
    public static class GroupAdministratorRestControllerTestConfig {
    }
    //-------------------Constants------------------------------------------------
    private static final String ERROR_MESAGE  = "Error message";
    private static final ObjectReader jsonReader = new ObjectMapper().reader();
    private static final String USER_ID = "user";
    private static final String VALID_USER_PASSWORD = "123";
    private static final String PASSWORD = "password";
    private static final String COMMONNAME = "ValUser11";
    private static final String SHORTNAME = "Val";
    private static final String ADMIN_ORGANIZATION_NAME = "ValidOrganization";
    private static final String ROLE = "";
    private static final ObjectWriter jsonWriter = new ObjectMapper().writer();
    private static final String ROCKET_USER = "rocketuser";
    private static final String ADMIN_ID = "rocketAdmin";
    private static final String SENSOR_USER = "sensoruser";
    private final List<User> VALID_USERS
            = new ArrayList<>(Arrays.asList(new User[]{
                new User("000", "000", "Admin user", "Admin", "org1", true),
                new User("111", "000", "Simple user", "Simple", "org2", true)}));
 


    private static final ObjectMapper mapper = new ObjectMapper();

    private User VALID_USER = new User(USER_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, "AAAAA", true);
    private User INVALID_USER = new User(USER_ID, "", "","", "", true);
    //-------------------Fields--------------------------------------------------- 
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Mock
    GroupAdministratorService groupAdministratorService;

    @Autowired
    @InjectMocks
    private GroupAdministratorRestController restContrioler;
    private MockMvc mockMvc;

    //-------------------Before tests------------------------------------------
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    //-------------------Tests-------------------------------------------------
    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void shouldCreateRocketUser() throws Exception {
        shouldCreateUser(ROCKET_USER, ROCKET_ADMIN_DIRECTORY);
    }

    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void shouldCreateSensorUser() throws Exception {
        shouldCreateUser(SENSOR_USER, SENSOR_ADMIN_DIRECTORY);
    }

    private void shouldCreateUser(String group, String domain) throws Exception {
        //arrange
        User controlUser = new User(USER_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, "AAAAA", true);
        JsonNode jsonNode = mapper.valueToTree(VALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        when(groupAdministratorService.createUser(VALID_USER, group)).thenReturn(VALID_USER);
         //act&assert
        mockMvc.perform(post(GROUP_ADMINISTRATOR_PATH
                + domain)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult mr) throws Exception {
                        System.out.print(mr.getResponse().getContentAsString());
                    }
                })
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo((MvcResult mr) -> {
                    User u = jsonReader.forType(User.class).
                    readValue(mr.getResponse().getContentAsString());
                    VALID_USER.setDn(u.getDn());
                    VALID_USER.setPasswordWithoutEncoding(null);
                    ReflectionAssert.assertReflectionEquals(VALID_USER, u);
                });
        verify(groupAdministratorService).createUser(controlUser,group);
    }


    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testGetUserListBySensorAdmin() throws Exception {
        GetUserListBy(SENSOR_USER, SENSOR_ADMIN_DIRECTORY);
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testGetUserListByRocketAdmin() throws Exception {
        GetUserListBy(ROCKET_USER, ROCKET_ADMIN_DIRECTORY);
    }

    private void GetUserListBy(String group, String domain) throws Exception {
        //arrange
        VALID_USERS.get(0).setDn(null);
        VALID_USERS.get(0).setPasswordWithoutEncoding(null);
        VALID_USERS.get(1).setDn(null);
        VALID_USERS.get(1).setPasswordWithoutEncoding(null);
        List<User> listOrganizationUser = new LinkedList<>(VALID_USERS);
        when(groupAdministratorService.UserList(group)).thenReturn(listOrganizationUser);
         //act&assert
        mockMvc.perform(get(GROUP_ADMINISTRATOR_PATH + domain))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo((MvcResult mr) -> {
                    Iterator<User> iterator = jsonReader.forType(User.class)
                    .<User>readValues(mr.getResponse().getContentAsString());
                    List<User> users = IteratorUtils.toList(iterator);
                    Assertions.assertThat(users).hasSize(VALID_USERS.size()).
                    containsOnly(VALID_USERS.toArray(new User[0]));
                }
                );
         verify(groupAdministratorService).UserList(group);
    }
    
    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testDeleteRocketUser() throws Exception {
        testDeleteUser();
    }
    
    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testDeleteSensorUser() throws Exception {
        testDeleteUser();
    }

    private void testDeleteUser() throws Exception {
        //arrange
        JsonNode jsonNode = mapper.valueToTree(VALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
         //act&assert
        mockMvc.perform(delete(GROUP_ADMINISTRATOR_PATH + ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isOk());
        verify(groupAdministratorService).deleteUser(VALID_USER);
    }
    
    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testUpdateRocketUser() throws Exception {
        UpdateUser(ROCKET_USER, ROCKET_ADMIN_DIRECTORY);
    }
    
    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testUpdateSensorUser() throws Exception {
        UpdateUser(SENSOR_USER, SENSOR_ADMIN_DIRECTORY);
    }

    private void UpdateUser(String group, String domain) throws Exception {
        //arrange
        User controlUser = new User(USER_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, "AAAAA", true);
        JsonNode jsonNode = mapper.valueToTree(VALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        when(groupAdministratorService.updateUser(VALID_USER, group)).thenReturn(VALID_USER);
        //act
        mockMvc.perform(put(GROUP_ADMINISTRATOR_PATH + domain)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isOk())
                .andDo((MvcResult mr) -> {
                    User u = jsonReader.forType(User.class).
                    readValue(mr.getResponse().getContentAsString());
                    VALID_USER.setDn(u.getDn());
                    VALID_USER.setPasswordWithoutEncoding(null);
                    ReflectionAssert.assertReflectionEquals(VALID_USER, u);
                });  
        //assert
        verify(groupAdministratorService).updateUser(controlUser, group);
    }
    
    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void shouldReturnErrorForCreateRocketUser() throws Exception {
        shouldReturnErrorForCreateUser(ROCKET_USER, ROCKET_ADMIN_DIRECTORY);
    }

    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void shouldReturnErrorForCreateSensorUser() throws Exception {
        shouldReturnErrorForCreateUser(SENSOR_USER, SENSOR_ADMIN_DIRECTORY);
    }
    
    private void shouldReturnErrorForCreateUser(String group, String domain) throws Exception{
        //arrange
        JsonNode jsonNode = mapper.valueToTree(INVALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, "");
        doThrow(new IllegalArgumentException(ERROR_MESAGE)).when(groupAdministratorService).createUser(INVALID_USER, group);
        //act&assert
        mockMvc.perform(post(GROUP_ADMINISTRATOR_PATH
                + domain)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult mr) throws Exception {
                        System.out.print(mr.getResponse().getContentAsString());
                    }
                })
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(ERROR_MESAGE)));
    }
    
    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testShouldReturnErrorForGetUserListBySensorAdmin() throws Exception {
        ShouldReturnErrorForGetUserByList(SENSOR_USER, SENSOR_ADMIN_DIRECTORY);
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testShouldReturnErrorForGetUserListByRocketAdmin() throws Exception {
        ShouldReturnErrorForGetUserByList(ROCKET_USER, ROCKET_ADMIN_DIRECTORY);
    }
    
    
    private void ShouldReturnErrorForGetUserByList(String group, String domain) throws Exception {
        //arrange
        doThrow(new IllegalArgumentException(ERROR_MESAGE)).when(groupAdministratorService).UserList(group);
         //act&assert
        mockMvc.perform(get(GROUP_ADMINISTRATOR_PATH + domain))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(ERROR_MESAGE)));
    }

    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testShouldReturnErrorForDeleteRocketUser() throws Exception {
        ShouldReturnErrorForDeleteUser();
    }
    
    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testShouldReturnErrorForDeleteSensorUser() throws Exception {
        ShouldReturnErrorForDeleteUser();
    }

    private void ShouldReturnErrorForDeleteUser() throws Exception {
        //arrange
        JsonNode jsonNode = mapper.valueToTree(INVALID_USER);
        doThrow(new IllegalArgumentException(ERROR_MESAGE)).when(groupAdministratorService).deleteUser(INVALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, "");
         //act&assert
        mockMvc.perform(delete(GROUP_ADMINISTRATOR_PATH + ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(ERROR_MESAGE)));
    }
    
    @Test
    @WithMockUser(roles = ROCKET_ADMIN, username = ADMIN_ID)
    public void testShouldReturnErrorForUpdateRocketUser() throws Exception {
        shouldReturnErrorForUpdateRocketUser(ROCKET_USER, ROCKET_ADMIN_DIRECTORY);
    }
    
    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testShouldReturnErrorForUpdateSensorUser() throws Exception {
        shouldReturnErrorForUpdateRocketUser(SENSOR_USER, SENSOR_ADMIN_DIRECTORY);
    }
    
    
    private void shouldReturnErrorForUpdateRocketUser(String group, String domain) throws Exception {
        //arrange
        JsonNode jsonNode = mapper.valueToTree(INVALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, "");
        doThrow(new IllegalArgumentException(ERROR_MESAGE)).when(groupAdministratorService).updateUser(INVALID_USER, group);
        //act&assert
        mockMvc.perform(put(GROUP_ADMINISTRATOR_PATH + domain)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())  
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(ERROR_MESAGE))); 
    }
    
    
    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testChangeUserPassword() throws Exception{
        //arrange
        User controlUser = new User(USER_ID, VALID_USER_PASSWORD, COMMONNAME, SHORTNAME, "AAAAA", true);
        JsonNode jsonNode = mapper.valueToTree(VALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        when(groupAdministratorService.changePassword(VALID_USER)).thenReturn(VALID_USER);
        //act
        mockMvc.perform(put(GROUP_ADMINISTRATOR_PATH + PASS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo((MvcResult mr) -> {
                    User u = jsonReader.forType(User.class).
                    readValue(mr.getResponse().getContentAsString());
                    VALID_USER.setDn(u.getDn());
                    VALID_USER.setPasswordWithoutEncoding(null);
                    ReflectionAssert.assertReflectionEquals(VALID_USER, u);
                });  
        //assert
        verify(groupAdministratorService).changePassword(controlUser);
    }
    
    @Test
    @WithMockUser(roles = SENSOR_ADMIN, username = ADMIN_ID)
    public void testShouldReturnErrorChangeUserPassword() throws Exception{
        //arrange
        JsonNode jsonNode = mapper.valueToTree(INVALID_USER);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, "");
        doThrow(new IllegalArgumentException(ERROR_MESAGE)).when(groupAdministratorService).changePassword(INVALID_USER);
        //act
        mockMvc.perform(put(GROUP_ADMINISTRATOR_PATH + PASS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                 .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(ERROR_MESAGE))); 
    }

}

