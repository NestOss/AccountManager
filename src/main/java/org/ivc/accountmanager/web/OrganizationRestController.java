/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.web;

import java.util.List;
import static org.apache.commons.lang3.Validate.*;

import static org.ivc.accountmanager.web.OrganizationRestController.ORGANIZATIONS_PATH;

import org.ivc.accountmanager.domain.Organization;
import org.ivc.accountmanager.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Organization Rest Controller.
 *
 * @author Roman Osipov
 */
@RestController
@RequestMapping(path = ORGANIZATIONS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizationRestController {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    public static final String ORGANIZATIONS_PATH = "/organizations";
    public static final String ITEM_PATH = "/item";
    public static final String INVALID_REPLACE_ORGANIZATION_REQUEST_BODY_MESSAGE
            = "Request body for replace must contain array of two organizations!";
    public static final String OLD_ORGANIZATION = "oldOrganization";
    public static final String NEW_ORGANIZATION = "newOrganization";

    //-------------------Fields---------------------------------------------------
    private OrganizationRepository organizationRepository;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Autowired(required = false) // False for test purpose.
    public void setOrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    //-------------------Methods--------------------------------------------------
    @RequestMapping(method = RequestMethod.GET)
    public List<Organization> getOrganizationsList() {
        return organizationRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST,
            path = ITEM_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Organization createOrganization(@RequestBody Organization organization) {
        return organizationRepository.create(organization);
    }

    @RequestMapping(method = RequestMethod.PUT,
            path = ITEM_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Organization replaceOrganization(@RequestBody List<Organization> organizations) {
        isTrue(organizations.size() == 2, INVALID_REPLACE_ORGANIZATION_REQUEST_BODY_MESSAGE);
        Organization oldOrganization = organizations.get(0);
        Organization newOrganization = organizations.get(1);
        organizationRepository.replace(oldOrganization, newOrganization);
        return newOrganization;
    }

    @RequestMapping(method = RequestMethod.DELETE,
            path = ITEM_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Organization deleteOrganization(@RequestBody Organization organization) {
        organizationRepository.delete(organization);
        return organization;
    }
}
