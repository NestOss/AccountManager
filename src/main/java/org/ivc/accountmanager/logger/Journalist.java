/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.logger;

//import com.sun.tools.internal.xjc.outline.Aspect;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Roman Osipovi
 */
@Aspect
@Component
public class Journalist {

    //-------------------Logger---------------------------------------------------
    private final static Logger logger = LoggerFactory.getLogger(Journalist.class);

    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------
    @Pointcut("execution(* org.ivc.accountmanager..*(..))")
    public void applicationMethodPointcut() {
    }

    @Pointcut("@annotation(org.ivc.accountmanager.annotation.LogToJournal)")
    public void logAnnotationPointcut() {
    }

    @Around("applicationMethodPointcut() && logAnnotationPointcut()")
    public Object log(ProceedingJoinPoint jp) throws Throwable {
        String userName = "anonimous";
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null) {
                userName =  authentication.getName();
            }
        }
        Object result = jp.proceed();
        logger.info(String.format("%s %s %s", userName, jp.getSignature().getName(),
                Arrays.toString(jp.getArgs())));
        return result;
    }
}
