package org.ivc.accountmanager.test.listener;

import org.ivc.accountmanager.config.LdapConfig;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.springframework.ldap.test.LdapTestUtils;

/**
 * Embedded LDAP server launcher.
 *
 * @author Roman Osipov
 */
public class EmbeddedLdapServerLauncher extends RunListener {
    //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    public static final int SERVER_PORT = 33389;

    //-------------------Fields---------------------------------------------------
    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------
    @Override
    public void testRunStarted(Description description) throws java.lang.Exception {
        System.out.println("Start embedded LDAP server at port: " + SERVER_PORT + ".");
        LdapTestUtils.startEmbeddedServer(SERVER_PORT, LdapConfig.LDAP_BASE, "ldap-test-server");
    }

    @Override
    public void testRunFinished(Result result) throws java.lang.Exception {
        System.out.println("Shutdown embedded LDAP server.");
        LdapTestUtils.shutdownEmbeddedServer();
    }
}
