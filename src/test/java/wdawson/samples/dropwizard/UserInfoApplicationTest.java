package wdawson.samples.dropwizard;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.io.Resources;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;
import wdawson.samples.dropwizard.health.UserInfoHealthCheck;
import wdawson.samples.dropwizard.resources.UserInfoResource;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author wdawson
 */
public class UserInfoApplicationTest {

    private UserInfoConfiguration configuration;

    private final Environment environment = mock(Environment.class);
    private final HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);

    private final UserInfoApplication userInfoApplication = new UserInfoApplication();

    @Before
    public void setup() throws Exception {
        final File configFile = new File(Resources.getResource("dropwizard/valid-conf.yml").toURI());

        configuration = new DefaultConfigurationFactoryFactory<UserInfoConfiguration>()
                .create(UserInfoConfiguration.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(configFile);

        // Common Expectations
        when(environment.healthChecks()).thenReturn(healthCheckRegistry);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        assertThat(configuration.getNamesResource()).isEqualTo("fixtures/users/test-names.txt");
    }

    @Test
    public void testThatApplicationConfiguresResourcesCorrectly() throws Exception {
        // Exercise
        userInfoApplication.run(configuration, environment);

        // Verify
        verify(healthCheckRegistry).register(anyString(), any(UserInfoHealthCheck.class));
        verify(jerseyEnvironment).register(any(UserInfoResource.class));
    }

    @Test
    public void testThatApplicationRegistersResources() throws Exception {
        // Setup
        ArgumentCaptor<UserInfoHealthCheck> userInfoHealthCheckCaptor = forClass(UserInfoHealthCheck.class);
        ArgumentCaptor<UserInfoResource> userInfoResourceCaptor = forClass(UserInfoResource.class);

        // Exercise
        userInfoApplication.run(configuration, environment);

        // Verify
        verify(healthCheckRegistry).register(anyString(), userInfoHealthCheckCaptor.capture());
        UserInfoHealthCheck userInfoHealthCheck = userInfoHealthCheckCaptor.getValue();
        assertThat(userInfoHealthCheck.getNamesResource()).isEqualTo("fixtures/users/test-names.txt");
        assertThat(userInfoHealthCheck.getNames()).containsExactly("Jane Doe", "John Doe");

        verify(jerseyEnvironment).register(userInfoResourceCaptor.capture());
        UserInfoResource userInfoResource = userInfoResourceCaptor.getValue();
        assertThat(userInfoResource.getNames()).containsExactly("Jane Doe", "John Doe");
    }
}
