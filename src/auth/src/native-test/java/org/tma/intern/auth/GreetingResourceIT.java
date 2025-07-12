package org.tma.intern.auth;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
//@QuarkusTestResource(value = KeycloakTestContainer.class, restrictToAnnotatedClass = false)
class GreetingResourceIT extends GreetingResourceTest {
    // Execute the same tests but in packaged mode.
}
