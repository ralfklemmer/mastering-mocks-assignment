package victor.testing.mocks.purity.service;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface VisibleForTesting {
    // just putted it somewhere, normally I take this from Guava.
}
