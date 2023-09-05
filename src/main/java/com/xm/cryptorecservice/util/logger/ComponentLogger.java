package com.xm.cryptorecservice.util.logger;

import static com.xm.cryptorecservice.util.logger.MethodLoggingMessages.msg;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * AOP-enabled class that logs all calls to {@literal public} methods of repositories annotated with
 * {@link Logged}. Calls to entrance, exit and exception throwing are all logged.
 *
 * @author jason
 * @see MethodLoggingMessages
 */
@Component
@Aspect
@Slf4j
public class ComponentLogger {

    @Before("execution(* (@com.xm.cryptorecservice.util.logger.Logged *..*).*(..))")
    public void beforeCallingAnyMethod(JoinPoint jp) {
        log.info(msg(Loc.BEGIN, jp));
    }

    @AfterReturning("execution(* (@com.xm.cryptorecservice.util.logger.Logged *..*).*(..))")
    public void afterCallingAnyMethod(JoinPoint jp) {
        log.info(msg(Loc.END, jp));
    }

    @AfterThrowing(
            value = ("execution(* (@com.xm.cryptorecservice.util.logger.Logged *..*).*(..))"),
            throwing = "ex")
    public void afterCallingAMethodThrows(JoinPoint jp, Throwable ex) {
        log.warn(msg(jp, ex.getClass()));
    }
}
