/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.application;

import org.ivc.accountmanager.config.AppConfig;
import org.springframework.boot.SpringApplication;

/**
 * Spring boot entry point.
 *
 * @author Roman Osipov
 */
public class Application {

    //-------------------Methods--------------------------------------------------
    public static void main(String[] args) {
        SpringApplication.run(AppConfig.class, args);
    }
}
