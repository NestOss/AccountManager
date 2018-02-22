/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.repository;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.domain.Group;
import org.springframework.validation.annotation.Validated;

/**
 * Group repository.
 *
 * @author Sokolov@ivc.org
 */
@Validated
public interface GroupRepository {

    /**
     * Returns the list of the groups.
     *
     * @return the list of the groups.
     */
    List<Group> findAll();

    /**
     * Looking for group by it's common name. If was found several groups with such common name,
     * then return the first of them. Returns empty optional, if group not found.
     *
     * @param cn the common name of the group.
     * @return optional group with defining value of the group's common name.
     */
    Optional<Group> findByCn(@NotBlank(message = "{emtpy.argument}") String cn);

    /**
     * Looking for group by user UID. If was found more when one group, then returns the first of
     * them. Returns empty optional, if group not found.
     *
     * @param userUID the user UID.
     * @return optional group containing such member.
     */
    Optional<Group> findByMember(@NotBlank(message = "{empty.argument}") String userUID);

    /**
     * Update group.
     *
     * @param group updating group.
     */
    void update(@Valid @NotNull Group group);

}
