package org.dancingcloudservices.security;

import java.io.PrintWriter;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

public final class LoggingSecurityManager extends SecurityManager {
    private final SecurityManager delegate;
    private final PrintWriter logger;
    private final Map<String, Long> permissionMap;
    private final String finalOutputPath;
    private boolean enableDelegate;

    private LoggingSecurityManager(
            SecurityManager delegate,
            PrintWriter logger,
            Map<String, Long> map,
            String path) {
        this.delegate = delegate;
        enableDelegate = (this.delegate != null);
        this.logger = logger;
        this.permissionMap = map;
        this.finalOutputPath = path;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private SecurityManager delegate = null;
        private PrintWriter logger = null;
        private Map<String, Long> permissionMap = null;
        private String finalOutputPath = null;

        public Builder delegate(SecurityManager delegate) {
            this.delegate = delegate;
            return this;
        }

        public Builder logger(PrintWriter logger) {
            this.logger = logger;
            return this;
        }

        public Builder map(Map<String, Long> map) {
            this.permissionMap = map;
            return this;
        }

        public Builder outputTo(String output) {
            this.finalOutputPath = output;
            return this;
        }

        public LoggingSecurityManager build() {
            if (permissionMap == null && finalOutputPath != null) {
                permissionMap = new HashMap<>();
            }
            return new LoggingSecurityManager(delegate, logger, permissionMap, finalOutputPath);
        }
    }

    public void disableDelegate() {
        enableDelegate = false;
    }

    private final void collectInfo(Permission perm) {
        String permText = perm.toString();
        if (logger != null) {
            logger.println(permText);
        }
        if (permissionMap != null) {
            Long count = permissionMap.get(permText);
            if (count == null) {
                count = Long.valueOf(0);
            }
            count = Long.sum(1, count);
            permissionMap.put(permText, count);
        }
    }

    @Override
    public final void checkPermission(Permission perm) {
        collectInfo(perm);
        if (delegate != null && enableDelegate) {
            delegate.checkPermission(perm);
        }
    }

    @Override
    public final void checkPermission(Permission perm, Object context) {
        collectInfo(perm);
        if (delegate != null && enableDelegate) {
            delegate.checkPermission(perm, context);
        }
    }
}
