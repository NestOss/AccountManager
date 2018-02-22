package org.ivc.accountmanager.service.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.naming.Name;
import javax.validation.ConstraintViolationException;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.config.ValidatorConfig;
import org.ivc.accountmanager.domain.Group;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.GroupRepository;
import org.ivc.accountmanager.repository.OrganizationRepository;
import org.ivc.accountmanager.repository.UserRepository;
import org.ivc.accountmanager.service.UserService;
import org.ivc.accountmanager.service.UserServiceImpl;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 * UserServiceImpl integration tests.
 *
 * @author Sokolov@ivc.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class UserServiceImplTest {

    //-------------------Inner classes--------------------------------------------
    @Configuration
    @EnableAutoConfiguration
    @Import(ValidatorConfig.class)
    public static class UserControlServiceImplTestConfig {

        @Bean
        public UserService userService() {
            return new UserServiceImpl();
        }

        @Bean
        public GroupRepository groupRepository() {
            return mockGroupRepository;
        }

        @Bean
        public OrganizationRepository organizationRepository() {
            return mockOrganizationRepository;
        }

        @Bean
        public UserRepository userRepository() {
            return mockUserRepository;
        }
    }

    //-------------------Constants------------------------------------------------
    private final User invalidUser = new User("", "101", "common name1",
            "shortName1", "Org1", true);
    private final User validToDeleteUser = new User("111", null, null, null, null, true);
    private final User invalidToDeleteUser = new User("", null, null, null, null, true);
    private final User validToUpdateUser = new User("111", null, "common name1",
            "shortName1", "Org1", true);

    private final User validUser1 = new User("111", "101", "common name1",
            "shortName1", "Org1", true);
    private final User validUser2 = new User("222", "202", "common name2",
            "shortName2", "Org2", false);
    private final List<User> VALID_USERS
            = new ArrayList<>(Arrays.asList(validUser1, validUser2));
    private final Group validGroup = new Group("admin");
    private final Group validNewGroup = new Group("newRole");

    //-------------------Fields---------------------------------------------------
    private static final OrganizationRepository mockOrganizationRepository
            = mock(OrganizationRepository.class);
    private static final GroupRepository mockGroupRepository = mock(GroupRepository.class);
    private static final UserRepository mockUserRepository = mock(UserRepository.class);

    @Autowired
    private UserService userService;

    //-------------------Before test---------------------------------------------
    @Before
    public void setup() {
        reset(mockOrganizationRepository, mockGroupRepository, mockUserRepository);
    }

    //-------------------Tests------------------------------------------------
    //===================findAll============================================   
    @Test
    public void happyPathFindAll() {
        //arrange
        when(mockUserRepository.findAll()).thenReturn(VALID_USERS);
        when(mockGroupRepository.findByMember(validUser1.getId()))
                .thenReturn(Optional.of(validGroup));
        when(mockGroupRepository.findByMember(validUser2.getId()))
                .thenReturn(Optional.empty());
        //act
        List<User> users = userService.findAll();
        //assert
        verify(mockUserRepository).findAll();
        assertThat(users).hasSameSizeAs(VALID_USERS).containsAll(VALID_USERS);
        assertThat(extractProperty(User.ROLE_PROPERTY).from(users))
                .containsExactly(validGroup.getCommonName(), null);
    }

    //===================create============================================
    @Test
    public void happyPathCreate() {
        //arrange
        validUser1.setRole(validGroup.getCommonName());
        when(mockOrganizationRepository.isExistsByName(validUser1.getOrganizationName()))
                .thenReturn(true);
        when(mockGroupRepository.findByCn(validGroup.getCommonName()))
                .thenReturn(Optional.of(validGroup));
        when(mockUserRepository.create(validUser1)).thenReturn(validUser1);
        Name ldapName = LdapNameBuilder.newInstance(LdapConfig.LDAP_BASE).add(User.BASE_DN)
                .add(User.UID_ATTRIBUTE, validUser1.getId()).build();
        //act
        User createdUser = userService.create(validUser1);
        //assert
        ReflectionAssert.assertReflectionEquals(validUser1, createdUser);
        validGroup.addMember(ldapName);
        verify(mockGroupRepository).update(validGroup);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForCreateWithNullArgument() {
        // act
        userService.create(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForCreateWithNotValidArgument() {
        // act
        userService.create(invalidUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIAEForCreateWithNotExistsOrganization() {
        // arrange
        validUser1.setRole(validGroup.getCommonName());
        when(mockOrganizationRepository.isExistsByName(validUser1.getOrganizationName()))
                .thenReturn(false);
        // act
        userService.create(validUser1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIAEForCreateWithInvalidRole() {
        // arrange
        validUser1.setRole(validGroup.getCommonName());
        when(mockOrganizationRepository.isExistsByName(validUser1.getOrganizationName()))
                .thenReturn(true);
        when(mockGroupRepository.findByCn(validGroup.getCommonName()))
                .thenReturn(Optional.empty());
        // act
        userService.create(validUser1);
    }

    //===================delete============================================
    @Test
    public void happyPathDeleteWhenUserIsMemberOfGroup() {
        // arrange
        validGroup.addMember(buildAbsUserDn(validToDeleteUser.getId()));
        when(mockGroupRepository.findByMember(validToDeleteUser.getId()))
                .thenReturn(Optional.of(validGroup));
        // act
        userService.delete(validToDeleteUser);
        // assert
        InOrder inOrder = inOrder(mockGroupRepository, mockUserRepository);
        assertTrue(validGroup.getMembers().isEmpty());
        inOrder.verify(mockGroupRepository).update(validGroup);
        inOrder.verify(mockUserRepository).delete(validToDeleteUser);
    }

    @Test
    public void happyPathDeleteWhenUserIsNotMemberOfGroup() {
        // arrange
        when(mockGroupRepository.findByMember(validUser1.getId()))
                .thenReturn(Optional.empty());
        // act
        userService.delete(validToDeleteUser);
        // assert
        verify(mockGroupRepository, never()).update(anyObject());
        verify(mockUserRepository).delete(validToDeleteUser);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForDeleteWithNullArgument() {
        // act
        userService.delete(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForDeleteWithInvalidArgument() {
        // act
        userService.delete(invalidToDeleteUser);
    }

    //===================update============================================
    @Test
    public void happyPathUpdateWhenRoleNotIsChanged() {
        // arrange
        User argumentUser = new User("111", null, "newCN", "newSN", "Org1", true);
        argumentUser.setRole("admin");
        User repositoryUser = new User("111", "password", "oldCN", "oldSN", "oldOrg", true);
        Group repositoryGroup = new Group("admin");
        when(mockUserRepository.findByUid("111")).thenReturn(Optional.of(repositoryUser));
        when(mockOrganizationRepository.isExistsByName("Org1")).thenReturn(true);
        when(mockGroupRepository.findByMember("111")).thenReturn(Optional.of(repositoryGroup));
        // act
        userService.update(argumentUser);
        // assert
        verify(mockGroupRepository, never()).update(anyObject());
        User expectedToUpdateUser = new User("111", "password", "newCN", "newSN", "Org1", true);
        expectedToUpdateUser.setRole("admin");
        verify(mockUserRepository).update(expectedToUpdateUser);
    }

    @Test
    public void happyPathUpdateWhenRoleIsChanged() {
        // arrange
        User argumentUser = new User("111", null, "newCN", "newSN", "Org1", true);
        argumentUser.setRole("admin");
        User repositoryUser = new User("111", "password", "oldCN", "oldSN", "oldOrg", true);
        Group oldRepositoryGroup = new Group("user");
        Group newRepositoryGroup = new Group("admin");
        when(mockUserRepository.findByUid("111")).thenReturn(Optional.of(repositoryUser));
        when(mockOrganizationRepository.isExistsByName("Org1")).thenReturn(true);
        when(mockGroupRepository.findByMember("111")).thenReturn(Optional.of(oldRepositoryGroup));
        when(mockGroupRepository.findByCn("admin")).thenReturn(Optional.of(newRepositoryGroup));
        // act
        userService.update(argumentUser);
        // assert
        Group expectedToUpdateNewGroup = new Group("admin");
        expectedToUpdateNewGroup.addMember(buildAbsUserDn(argumentUser.getId()));
        verify(mockGroupRepository).update(expectedToUpdateNewGroup);
        Group expectedToUpdateOldGroup = new Group("user");
        verify(mockGroupRepository).update(expectedToUpdateOldGroup);
        User expectedToUpdateUser = new User("111", "password", "newCN", "newSN", "Org1", true);
        expectedToUpdateUser.setRole("admin");
        verify(mockUserRepository).update(expectedToUpdateUser);
    }

    @Test
    public void happyPathUpdateWhenUserNotInGroupBefore() {
        // arrange
        User argumentUser = new User("111", null, "newCN", "newSN", "Org1", true);
        argumentUser.setRole("admin");
        User repositoryUser = new User("111", "password", "oldCN", "oldSN", "oldOrg", true);
        Group newRepositoryGroup = new Group("admin");
        when(mockUserRepository.findByUid("111")).thenReturn(Optional.of(repositoryUser));
        when(mockOrganizationRepository.isExistsByName("Org1")).thenReturn(true);
        when(mockGroupRepository.findByMember("111")).thenReturn(Optional.empty());
        when(mockGroupRepository.findByCn("admin")).thenReturn(Optional.of(newRepositoryGroup));
        // act
        userService.update(argumentUser);
        // assert
        Group expectedToUpdateNewGroup = new Group("admin");
        expectedToUpdateNewGroup.addMember(buildAbsUserDn(argumentUser.getId()));
        expectedToUpdateNewGroup.addMember(buildAbsUserDn(validToUpdateUser.getId()));
        verify(mockGroupRepository).update(expectedToUpdateNewGroup);
        User expectedToUpdateUser = new User("111", "password", "newCN", "newSN", "Org1", true);
        expectedToUpdateUser.setRole("admin");
        verify(mockUserRepository).update(expectedToUpdateUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIAEForUpdateNotExistsUser() {
        // arrange
        User argumentUser = new User("111", null, "newCN", "newSN", "Org1", true);
        argumentUser.setRole("admin");
        when(mockUserRepository.findByUid("111")).thenReturn(Optional.empty());
        // act
        userService.create(argumentUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIAEForUpdateUserWithNotExistsOrganization() {
        // arrange
        User argumentUser = new User("111", null, "newCN", "newSN", "Org1", true);
        argumentUser.setRole("admin");
        User repositoryUser = new User("111", "password", "oldCN", "oldSN", "oldOrg", true);
        when(mockUserRepository.findByUid("111")).thenReturn(Optional.of(repositoryUser));
        when(mockOrganizationRepository.isExistsByName("Org1")).thenReturn(false);
        // act
        userService.create(argumentUser);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForUpdateWithNullArgument() {
        // act
        userService.update(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForUpdateWithInvalidArgument() {
        User invalidToUpdateUser = new User("", "101", "common name1", "shortName1", "Org1", true);
        // act
        userService.delete(invalidToUpdateUser);
    }

    //===================findByGroupName============================================
    @Test
    public void happyPathFindByGroupName() {
        // arrange
        Group repositoryGroup = new Group("admin");
        repositoryGroup.addMember(buildAbsUserDn("111"));
        repositoryGroup.addMember(buildAbsUserDn("222"));
        repositoryGroup.addMember(buildAbsUserDn("333"));
        User repositoryUser1 = new User("111", "pass1", "newCN", "newSN", "Org1", true);
        User repositoryUser2 = new User("222", "password", "oldCN", "oldSN", "oldOrg", true);
        when(mockGroupRepository.findByCn("admin")).thenReturn(Optional.of(repositoryGroup));
        when(mockUserRepository.findByUid("111")).thenReturn(Optional.of(repositoryUser1));
        when(mockUserRepository.findByUid("222")).thenReturn(Optional.of(repositoryUser2));
        when(mockUserRepository.findByUid("333")).thenReturn(Optional.empty());
        // act
        List<User> users = userService.findByGroupName("admin");
        // assert
        assertThat(users).containsOnly(repositoryUser1, repositoryUser2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIAEForFindByGroupNameForNotExistsGroup() {
        // arrange
        when(mockGroupRepository.findByCn("admin")).thenReturn(Optional.empty());
        // act
        userService.findByGroupName("admin");
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByGroupNameWithNullArgument() {
        // act
        userService.findByGroupName(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldThrowCVEForFindByGroupNameWithInvalidArgument() {
        // act
        userService.findByGroupName("");
    }

    private Name buildAbsUserDn(String userId) {
        return LdapNameBuilder.newInstance(LdapConfig.LDAP_BASE).add(User.BASE_DN)
                .add(User.UID_ATTRIBUTE, userId).build();
    }

}
