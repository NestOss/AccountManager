/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.naming.Name;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.Validate.isTrue;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.domain.Group;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.GroupRepository;
import org.ivc.accountmanager.repository.OrganizationRepository;
import org.ivc.accountmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Implementation UserService.
 *
 * @author Sokolov@ivc.org
 */
@Service
public class UserServiceImpl implements UserService {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    public static final String NULL_HASH = "{sha256}pmWkWSBCL51Bfkhn79xPuKBKHz//H6B+mY6G9/eieuM=";
    private static final int USER_UID_POS = 2;

    //-------------------Fields---------------------------------------------------
    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private OrganizationRepository organizationRepository;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Autowired(required = false)
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired(required = false)
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Autowired(required = false)
    public void setOrganizationRepository(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    //-------------------Methods--------------------------------------------------
    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     */
    @Override
    public List<User> findAll() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            Optional<Group> oGroup = groupRepository.findByMember(user.getId());
            String role = oGroup.isPresent() ? oGroup.get().getCommonName() : null;
            user.setRole(role);
        }
        return users;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     * <p/>
     * throws IllegalArgumentException - if user organization or group is not exists.
     * <p/>
     * throws ConstraintViolationException - on invalid or null user.
     */
    @Override
    public User create(@Valid @NotNull User user) {
        isTrue(organizationRepository.isExistsByName(user.getOrganizationName()),
                "Organization with name %s not exist.", user.getOrganizationName());
        Optional<Group> oGroup = groupRepository.findByCn(user.getRole());
        isTrue(oGroup.isPresent(), "Invalid user role: %s" + user.getRole());
        User createdUser = userRepository.create(user);
        Group group = oGroup.get();
        group.addMember(buildAbsUserDn(user));
        groupRepository.update(group);
        return createdUser;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NamingException - on error.
     */
    @Override
    public void delete(@NotNull @Validated(User.IdGroup.class) User user) {
        Optional<Group> oGroup = groupRepository.findByMember(user.getId());
        if (oGroup.isPresent()) {
            Group group = oGroup.get();
            Name userDn = buildAbsUserDn(user);
            group.removeMember(userDn);
            groupRepository.update(group);
        }
        userRepository.delete(user);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * ConstraintViolationException - on invalid or null argument.
     */
    @Override
    public User update(@Valid @NotNull User user) {
        Optional<User> oUser = userRepository.findByUid(user.getId());
        isTrue(oUser.isPresent(), String.format("User %s was not found to update.", user.getId()));
        isTrue(organizationRepository.isExistsByName(user.getOrganizationName()),
                String.format("Organization %s not exists.", user.getOrganizationName()));
        user.setPasswordWithoutEncoding(oUser.get().getPassword());
        Optional<Group> oOldGroup = groupRepository.findByMember(user.getId());
        if (oOldGroup.isPresent()) {
            Group oldGroup = oOldGroup.get();
            if (!oldGroup.getCommonName().equals(user.getRole())) {
                addUserToGroup(user);
                oldGroup.removeMember(buildAbsUserDn(user));
                groupRepository.update(oldGroup);
            }
        } else {
            addUserToGroup(user);
        }
        userRepository.update(user);
        return user;
    }

    private void addUserToGroup(User user) {
        Name userDn = buildAbsUserDn(user);
        Optional<Group> oNewGroup = groupRepository.findByCn(user.getRole());
        isTrue(oNewGroup.isPresent(), String.format(
                "Group(role) %s for user %s was not found.", user.getRole(), user.getId()));
        Group newGroup = oNewGroup.get();
        newGroup.addMember(userDn);
        groupRepository.update(newGroup);
    }

    @Override
    public List<User> findByGroupName(
            @NotBlank(message = "{empty.argument}") String groupName) {
        Optional<Group> oGroup = groupRepository.findByCn(groupName);
        isTrue(oGroup.isPresent(), "Group %s was not found.", groupName);
        List<User> users = new ArrayList<>();
        for (Name name : oGroup.get().getMembers()) {
            String userUID = StringUtils.substringAfter(name.get(USER_UID_POS), "=").trim();
            Optional<User> oUser = userRepository.findByUid(userUID);
            if (oUser.isPresent()) {
                users.add(oUser.get());
            }
        }
        return users;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * throws NullPointerException - on failure to determine the user as null, on failure when
     * userId field isn't determined for user.
     * <p/>
     * throws IllegalArgumentException - on failure when userId field of user determine as empty
     * string.
     */
    @Override
    public User changePassword(@NotNull User user
    ) {
//        Validate.notEmpty(user.getId(), User.UID_PROPERTY + " "
//                +  env.getProperty("null.field"));
        // Check password's hash. Throw exception if password's hash equal null hash.
//        Validate.isTrue(user.getPassword().equals(NULL_HASH),
//                User.PASSWORD_PROPERTY + " " +  env.getProperty("null.field"));
        User euser = userRepository.findByUid(user.getId()).get();
        euser.setPasswordWithoutEncoding(user.getPassword());
        userRepository.update(euser);
        return euser;
    }

    private Name buildAbsUserDn(User user) {
        return LdapNameBuilder.newInstance(LdapConfig.LDAP_BASE).add(User.BASE_DN)
                .add(User.UID_ATTRIBUTE, user.getId()).build();
    }

}
