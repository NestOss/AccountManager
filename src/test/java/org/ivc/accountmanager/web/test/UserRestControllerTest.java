package org.ivc.accountmanager.web.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.IteratorUtils;
import org.fest.assertions.api.Assertions;
import static org.hamcrest.Matchers.is;
import org.ivc.accountmanager.config.SecurityConfig;
import org.ivc.accountmanager.domain.RestError;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.service.UserService;
import org.ivc.accountmanager.web.ExceptionHandlerAdvice;
import org.ivc.accountmanager.web.TrimmerAdvice;
import org.ivc.accountmanager.web.UserRestController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyObject;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 * UserRestController tests.
 *
 * @author Sokolov@ivc.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class UserRestControllerTest {

    //-------------------Inner classes--------------------------------------------
    @SpringBootApplication
    @Import({SecurityConfig.class, UserRestController.class, ExceptionHandlerAdvice.class,
        TrimmerAdvice.class})
    public static class UserRestControllerTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectWriter jsonWriter = new ObjectMapper().writer();
    private static final ObjectReader jsonReader = new ObjectMapper().reader();

    private static final String VALID_USER_ID = "user";
    private static final String VALID_USER_PASSWORD = "123";
    private static final String VALID_USER_COMMON_NAME = "common";
    private static final String VALID_USER_SHORT_NAME = "short";
    private static final String VALID_USER_ORGANIZATION = "organization";
    private static final boolean VALID_USER_ACTIVE_STATUS = true;

    private final List<User> VALID_USERS
            = new ArrayList<>(Arrays.asList(new User[]{
                new User("000", "000", "Admin user", "Admin", "org1", true),
                new User("111", "000", "Simple user", "Simple", "org2", true)}));

    //-------------------Fields---------------------------------------------------
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Mock
    private UserService userService;

    @Autowired
    @InjectMocks
    UserRestController userRestController;

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
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnUserListForAdmin() throws Exception {
        // arrange
        VALID_USERS.get(0).setDn(null);
        VALID_USERS.get(0).setPasswordWithoutEncoding(null);
        VALID_USERS.get(1).setDn(null);
        VALID_USERS.get(1).setPasswordWithoutEncoding(null);
        when(userService.findAll())
                .thenReturn(VALID_USERS);
        // act & assert
        mockMvc.perform(get(UserRestController.USERS_PATH))
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
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyAccessToUserListForUser() throws Exception {
        // act & assert
        mockMvc.perform(get(UserRestController.USERS_PATH))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldCreateUserForAdmin() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        when(userService.create(user))
                .thenReturn(user);
        // act & assert
        mockMvc.perform(post(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo((MvcResult mr) -> {
                    User u = jsonReader.forType(User.class).
                    readValue(mr.getResponse().getContentAsString());
                    user.setDn(u.getDn());
                    user.setPasswordWithoutEncoding(null);
                    ReflectionAssert.assertReflectionEquals(user, u);
                });
        user.setPassword(VALID_USER_PASSWORD);
        verify(userService).create(user);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyCreateUserForUser() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(post(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldUpdateUserForAdmin() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        when(userService.update(user))
                .thenReturn(user);
        // act & assert
        mockMvc.perform(put(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo((MvcResult mr) -> {
                    User u = jsonReader.forType(User.class).
                    withoutAttribute(User.PASSWORD_PROPERTY).
                    readValue(mr.getResponse().getContentAsString());
                    user.setDn(u.getDn());
                    user.setPasswordWithoutEncoding(null);
                    ReflectionAssert.assertReflectionEquals(user, u);
                });
        user.setPassword(VALID_USER_PASSWORD);
        verify(userService).update(user);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyUpdateUserForUser() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(put(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldRemoveUserForAdmin() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(delete(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isOk());
        user.setPassword(VALID_USER_PASSWORD);
        verify(userService).delete(user);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyRemoveUserForAdmin() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(delete(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldChangePasswordForAdmin() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        when(userService.changePassword(user))
                .thenReturn(user);
        // act & assert
        mockMvc.perform(put(UserRestController.USERS_PATH
                + UserRestController.PASS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo((MvcResult mr) -> {
                    User u = jsonReader.forType(User.class).
                    withoutAttribute(User.PASSWORD_PROPERTY).
                    readValue(mr.getResponse().getContentAsString());
                    user.setDn(u.getDn());
                    user.setPasswordWithoutEncoding(null);
                    ReflectionAssert.assertReflectionEquals(user, u);
                });
        user.setPassword(VALID_USER_PASSWORD);
        verify(userService).changePassword(user);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyChangePasswordForUser() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(put(UserRestController.USERS_PATH
                + UserRestController.PASS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForCreateBadFormedUserForAdmin() throws Exception {
        // arrange
        User user = new User("", VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(post(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(MethodArgumentNotValidException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(User.UID_PROPERTY + " " /*+ User.EMPTY_ERROR_MESSAGE*/)));
        verify(userService, never()).delete(anyObject());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForUpdateBadFormedUserForAdmin() throws Exception {
        // arrange
        User user = new User("", VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(post(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(MethodArgumentNotValidException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(User.UID_PROPERTY + " "/* + User.EMPTY_ERROR_MESSAGE*/)));
        verify(userService, never()).update(anyObject());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForRemoveEmptyUserIdForAdmin() throws Exception {
        // arrange
        User user = new User("", VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(delete(UserRestController.USERS_PATH
                + UserRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(User.UID_PROPERTY + " " /*+User.EMPTY_ERROR_MESSAGE*/)));
        verify(userService, never()).delete(anyObject());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForChangePasswordOfEmptyUserIdForAdmin() throws Exception {
        // arrange
        User user = new User("", VALID_USER_PASSWORD,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, VALID_USER_PASSWORD);
        // act & assert
        mockMvc.perform(put(UserRestController.USERS_PATH
                + UserRestController.PASS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(User.UID_PROPERTY + " " /* + User.EMPTY_ERROR_MESSAGE */)));
        verify(userService, never()).changePassword(anyObject());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForChangePasswordToEmptyPasswordForAdmin() throws Exception {
        // arrange
        User user = new User(VALID_USER_ID, null,
                VALID_USER_COMMON_NAME, VALID_USER_SHORT_NAME,
                VALID_USER_ORGANIZATION, VALID_USER_ACTIVE_STATUS);
        JsonNode jsonNode = mapper.valueToTree(user);
        byte[] Invalidpassword = null;
        ((ObjectNode) jsonNode).put(User.PASSWORD_PROPERTY, Invalidpassword);
        // act & assert
        mockMvc.perform(put(UserRestController.USERS_PATH
                + UserRestController.PASS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(jsonNode))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(User.PASSWORD_PROPERTY + " " /*+ User.EMPTY_ERROR_MESSAGE*/)));
        verify(userService, never()).changePassword(anyObject());
    }

}
