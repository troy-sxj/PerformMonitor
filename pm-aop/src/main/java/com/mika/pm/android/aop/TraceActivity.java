package com.mika.pm.android.aop;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @Author: mika
 * @Time: 2020-03-17 16:30
 * @Description:
 */
@Aspect
public class TraceActivity {

    @Pointcut("execution(* android.app.Application.onCreate(android.content.Context)) && args(context)")
    public void applicationOnCreate(Context context) {

    }

    @After("applicationOnCreate(context)")
    public void applicationOnCreateAdvice(Context context) {
//        AH.applicationOnCreate(context);
    }

    @Pointcut("execution(* android.app.Application.attachBaseContext(android.content.Context)) && args(context)")
    public void applicationAttachBaseContext(Context context) {
    }

    @Before("applicationAttachBaseContext(context)")
    public void applicationAttachBaseContextAdvice(Context context) {
//        AH.applicationAttachBaseContext(context);
    }

    @Pointcut("execution(* android.app.Activity.on**(..))")
    public void activityOnXXX() {
    }

    @Around("activityOnXXX()")
    public Object activityOnXXXAdvice(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;
        try {
            Activity activity = (Activity) proceedingJoinPoint.getTarget();
            Log.d("AJAOP", "Aop Info" + activity.getClass().getCanonicalName() +
                    "\r\nkind : " + proceedingJoinPoint.getKind() +
                    "\r\nargs : " + proceedingJoinPoint.getArgs() +
                    "\r\nClass : " + proceedingJoinPoint.getClass() +
                    "\r\nsign : " + proceedingJoinPoint.getSignature() +
                    "\r\nsource : " + proceedingJoinPoint.getSourceLocation() +
                    "\r\nthis : " + proceedingJoinPoint.getThis()
            );
            long startTime = System.currentTimeMillis();
            result = proceedingJoinPoint.proceed();
            String activityName = activity.getClass().getCanonicalName();

            Signature signature = proceedingJoinPoint.getSignature();
            String sign = "";
            String methodName = "";
            if (signature != null) {
                sign = signature.toString();
                methodName = signature.getName();
            }

            if (!TextUtils.isEmpty(activityName) && !TextUtils.isEmpty(sign) && sign.contains(activityName)) {
                invoke(activity, startTime, methodName, sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }

    public void invoke(Activity activity, long startTime, String methodName, String sign) {
//        AH.invoke(activity, startTime, methodName, sign);
    }
}
