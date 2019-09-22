package lambda;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Broken {
    static class DelegateSecurityManager extends SecurityManager {
        private SecurityManager delegate = new SecurityManager(); // standard one
        @Override
        public void checkPermission(Permission p) {
            delegate.checkPermission(p);
        }
        @Override
        public void checkPermission(Permission p, Object context) {
            delegate.checkPermission(p, context);
        }
    }
    public static void main(String[] args) {
        Map<String, Long> map = new HashMap<>();
        map.put("Hello", Long.valueOf(3));
        System.setSecurityManager(new DelegateSecurityManager());
        map.forEach((k, v) -> System.out.println(v + ": " + k));
    }
}
