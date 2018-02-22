/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ivc.accountmanager.web;

import java.util.List;
import org.ivc.accountmanager.domain.Group;
import static org.ivc.accountmanager.web.GroupRestController.GROUPS_PATH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ivc.accountmanager.repository.GroupRepository;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Sokolov@ivc.org
 */
@RestController
@RequestMapping(path = GROUPS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupRestController {
    //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    public static final String GROUPS_PATH = "/groups";

    //-------------------Fields---------------------------------------------------
    private GroupRepository groupRepository;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    //-------------------Methods--------------------------------------------------
    @RequestMapping(method = RequestMethod.GET)
    public List<Group> getGroupList() {
        for(Group group : groupRepository.findAll()){
            System.out.println(group.getMembers());
        }
        return groupRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/cn")
    public Group getUser() {
        return groupRepository.findByCn("admin").get();
    }

}
