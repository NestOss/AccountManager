/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ivc.accountmanager.web;

import java.util.List;
import org.ivc.accountmanager.domain.User;
import org.ivc.accountmanager.repository.UserRepository;
import org.ivc.accountmanager.service.UserService;
import static org.ivc.accountmanager.web.TestController.PATH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Sokolov@ivc.org
 */
@RestController
@RequestMapping(path = PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {
    //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    public static final String PATH = "/test";

    //-------------------Fields---------------------------------------------------
    private UserService userControlService;

    private UserRepository userRepository;

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    @Autowired(required = false)
    public void setUserControlService(UserService userControlService) {
        this.userControlService = userControlService;
    }

    @Autowired(required = false)
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //-------------------Methods--------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, path = "/all")
    public List<User> getUserList() {
        return userRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.GET)
    public void createUser() {
        userControlService.create(new User("999", "999", "cusr", "usr", "TestOrganization1", true));
    }

}
