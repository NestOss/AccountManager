/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.repository;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.ivc.accountmanager.domain.Organization;
import org.springframework.validation.annotation.Validated;

/**
 * Organization service.
 *
 * @author Roman Osipov
 */
@Validated
public interface OrganizationRepository {
     
    /**
     * Returns the list of organizations.
     *
     * @return the list of organizations.
     */
    List<Organization> findAll();

    /**
     * Creates the organization.
     *
     * @param organization the organization.
     * @return created organization.
     */
    Organization create(@Valid @NotNull Organization organization);

    /**
     * Verification of the existence of the organization.
     *
     * @param name the organization name.
     * @return the result of verifying.
     */
    boolean isExistsByName(@NotBlank(message = "{emtpy.argument}") String name);

    /**
     * Delete the organization.
     *
     * @param organization the organization to delete.
     */
    void delete(@Valid @NotNull Organization organization);

    /**
     * Replace the organization.
     *
     * @param oldOrganization the old organization, that should be replaced.
     * @param newOrganization the new organization, that replaces old organization.
     */
    void replace(@Valid @NotNull Organization oldOrganization,
                 @Valid @NotNull Organization newOrganization);
}
