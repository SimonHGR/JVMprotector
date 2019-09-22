package com.dancingcloudservices.security;

import org.dancingcloudservices.security.LoggingSecurityManager;

import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TryLoggingSecurityManager {
    public static void main(String[] args) {
        Map<String, Long> map = new HashMap<>();
        LoggingSecurityManager sm = LoggingSecurityManager.builder()
                .delegate(new SecurityManager())
                .logger(new PrintWriter(System.out))
                .map(map)
                .build();
        System.setSecurityManager(sm);
        try (FileInputStream fis = new FileInputStream("test")) {
        } catch (Throwable t) {
            System.out.println("***" + t.getMessage());
        }
        try {
            sm.checkPermission(new FilePermission("test", "execute"));
        } catch (Throwable t) {
            System.out.println("***" + t.getMessage());
        }
        try (Socket s = new Socket("openjdk.java.net", 8080)){
        } catch (Throwable t) {
            System.out.println("***Socket open failed");
        }
        try {
            Class<?> self = TryLoggingSecurityManager.class;
            Method[] m = self.getMethods();
            m[0].setAccessible(false);
        } catch (Throwable t) {
            System.out.println("***" + t.getMessage());
        }
        sm.disableDelegate(); // Creating lambdas fails otherwise
        map.forEach((k,v) -> System.out.println(v + ": " + k));
    }
}
