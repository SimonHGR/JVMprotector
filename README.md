JVMprotector
============

A number of attacks on Java based systems could easily have been prevented by the use
of the core Java feature known as a SecurityManager. However, configuring the permissions
for a codebase is widely seen as to complex and it seems that nobody bothers to use
this facility (though I expect that folks like Experian might wish they had)

This project seeks to create an "intercepting" SecurityManager that helps with determining
what permissions a body of code actually request.

Notes
-----

One major issue at this point is that it seems that the generation of lambda expressions performs
various operations using reflection. This normally passes security checks, but when the interceptor
is added, these checks again fail. I belive this is related to the "the request must come within
this many stack frames of the actual point the SecurityManager is invoked" and adding an extra
level to this stack breaks things. This is demonstrated in the class lambda.Broken in the test 
subtree. Run the code, and it fails. But replace the call to
`System.setSecurityManager(new DelegateSecurityManager());` with one to 
`System.setSecurityManager(new SecurityManager());`  and it works fine.

Of course, this means that any code that uses lambdas is hard to distinguish from code that's
doing potentially nefarious operations gaining improper access to private fields, which is rather
a nuisance.

The class TryLoggingSecurityManager illustrates roughly how to use this. Notice the `sm.disableDelegate`
call is necessary to allow the dumping of the contents of the map of the count of requests for 
various permissions. (Actually, creating this lambda before installing the security manager also
works around this issue).


