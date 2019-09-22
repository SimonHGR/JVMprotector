package com.dancingcloudservices.security;

import org.dancingcloudservices.security.LoggingSecurityManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FilePermission;
import java.io.PrintWriter;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

public class LoggingSecurityManagerTest {
    static class MockSecurityManager extends SecurityManager {
        int callCount = 0;
        Permission perm;
        Object context;
        @Override public void checkPermission(Permission perm) {
            callCount++;
            this.perm = perm;
        }
        @Override public void checkPermission(Permission perm, Object context) {
            callCount++;
            this.perm = perm;
            this.context = context;
        }
    }

    static class MockLogger extends PrintWriter {
        ByteArrayOutputStream baos;
        private MockLogger(ByteArrayOutputStream baos) {
            super(baos);
        }
        public static MockLogger get() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MockLogger created = new MockLogger(baos);
            created.baos = baos;
            return created;
        }
        public String getText() {
            return baos.toString();
        }
        @Override
        public void println(String s) {
            super.println(s);
            flush();
        }
    }

    @Test
    public void securityManagerDelegatesSimple() {
        MockSecurityManager delegate = new MockSecurityManager();
        SecurityManager underTest = LoggingSecurityManager.builder()
                .delegate(delegate)
                .build();
        Permission fp = new FilePermission("/usr/bin", "read");
        underTest.checkPermission(fp);
        Assert.assertEquals("call count should be one", 1, delegate.callCount);
        Assert.assertEquals("permission should match", fp, delegate.perm);
    }
    @Test
    public void securityManagerDelegatesContext() {
        MockSecurityManager delegate = new MockSecurityManager();
        SecurityManager underTest = LoggingSecurityManager.builder()
                .delegate(delegate)
                .build();
        Permission fp = new FilePermission("/usr/bin", "read");
        Object context = new Object();
        underTest.checkPermission(fp, context);
        Assert.assertEquals("call count should be one", 1, delegate.callCount);
        Assert.assertEquals("permission should match", fp, delegate.perm);
        Assert.assertEquals("context should match", context, delegate.context);
    }
    @Test
    public void securityManagerLogsSimple() {
        MockSecurityManager delegate = new MockSecurityManager();
        MockLogger mockLogger = MockLogger.get();
        SecurityManager underTest = LoggingSecurityManager.builder()
                .delegate(delegate)
                .logger(mockLogger)
                .build();
        Permission fp = new FilePermission("/usr/bin", "read");
        underTest.checkPermission(fp);
        Assert.assertEquals("Logged message should be ???",
                "(\"java.io.FilePermission\" \"/usr/bin\" \"read\")\n",
                mockLogger.getText());
        Assert.assertEquals("call count should be one", 1, delegate.callCount);
        Assert.assertEquals("permission should match", fp, delegate.perm);
    }
    @Test
    public void securityManagerAddsToMap() {
        Map<String, Long> map = new HashMap<>();
        SecurityManager underTest = LoggingSecurityManager.builder()
                .map(map)
                .build();
        Object context = new Object();
        Permission fp = new FilePermission("/usr/bin", "read");
        Permission fp2 = new FilePermission("/usr/bin", "write");
        Permission fp3 = new FilePermission("/usr/bin", "execute");
        underTest.checkPermission(fp, context);
        underTest.checkPermission(new FilePermission("/usr/bin", "read"));
        underTest.checkPermission(new FilePermission("/usr/bin", "read"), context);
        underTest.checkPermission(fp2);
        underTest.checkPermission(fp3, context);
        Assert.assertEquals("Map should have three rows", 3, map.size());
        Assert.assertEquals("read action should show three times", Long.valueOf(3), map.get(fp.toString()));
        Assert.assertEquals("write action should show once", Long.valueOf(1), map.get(fp2.toString()));
        Assert.assertEquals("execute action should show once", Long.valueOf(1), map.get(fp3.toString()));
    }
}
