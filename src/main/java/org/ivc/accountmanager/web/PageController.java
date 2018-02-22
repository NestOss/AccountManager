package org.ivc.accountmanager.web;

import java.util.Collection;
import static org.ivc.accountmanager.config.Role.ROCKET_ADMIN;
import static org.ivc.accountmanager.config.Role.SENSOR_ADMIN;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PageController {
    public static final String GROUP_ADMINISTRATOR_PAGE_PATH = "/group_administrator_page";
    public static final String ORGANIZATION_PAGE_PATH = "/organizations_page";
    public static final String USER_PAGE_PATH = "/users_page";
    private static final String ROLE ="ROLE_";
  

    @RequestMapping(ORGANIZATION_PAGE_PATH)
    String organizations() {
        return "organizations";
    }
    
    @RequestMapping(GROUP_ADMINISTRATOR_PAGE_PATH)
    ModelAndView groupAdmins(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelAndView mav = new ModelAndView("groupadmins");
        Collection<? extends GrantedAuthority> d = userDetails.getAuthorities();
                for(GrantedAuthority authority: d){
            if(authority.getAuthority().equals(ROLE+ROCKET_ADMIN)){
                    mav.addObject("domain", "/rocket");
                    break;
            }
            if(authority.getAuthority().equals(ROLE+SENSOR_ADMIN)){
                    mav.addObject("domain", "/sensor");
                    break;
            }
        }
        return mav;
    }
    
    @RequestMapping(USER_PAGE_PATH)
    String users() {
        return "users";
    }
}
