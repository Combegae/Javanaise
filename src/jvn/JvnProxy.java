package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import irc.Irc;
import irc.Sentence;

public class JvnProxy implements InvocationHandler {
    
    private JvnObject objet;
    private JvnProxy(JvnObject obj){this.objet= obj;}

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Object result;
            if (method.isAnnotationPresent(ProxyAnnotation.class)) {
                ProxyAnnotation annotation = method.getAnnotation(ProxyAnnotation.class);
                if (annotation.name().equals(("read"))) {
                    objet.jvnLockRead();
                    result = method.invoke(objet.jvnGetSharedObject() ,args);
                    objet.jvnUnLock();
                    return result;
                } else if (annotation.name().equals(("write"))) {
                    objet.jvnLockWrite();
                    result = method.invoke(objet.jvnGetSharedObject(), args);
                    objet.jvnUnLock();
                    return result;
                }
            }
            result = method.invoke(objet, args); 
            return result;

            
        } catch (Exception e) {
            throw new Exception();
        }

    }
    
    public static Object newInstance(Object obj, String objectName) throws JvnException {
        JvnServerImpl js = JvnServerImpl.jvnGetServer();
        JvnObject objet;
        try {
            objet = js.jvnCreateObject((Serializable) obj);
            js.jvnRegisterObject(objectName, objet);
        } catch (JvnException e) {
            e.printStackTrace();
            throw new JvnException();
        }
        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new JvnProxy(objet));
    }
    
    public static Object lookUpObject(String objectName) throws JvnException {
        
        JvnServerImpl js = JvnServerImpl.jvnGetServer();
        JvnObject jvnObject = js.jvnLookupObject(objectName);
        if (jvnObject == null) {
            return null;
        }else {
            return java.lang.reflect.Proxy.newProxyInstance(
                jvnObject.jvnGetSharedObject().getClass().getClassLoader(),
                jvnObject.jvnGetSharedObject().getClass().getInterfaces(),
                new JvnProxy(js.jvnLookupObject(objectName)));
        }
    }




}