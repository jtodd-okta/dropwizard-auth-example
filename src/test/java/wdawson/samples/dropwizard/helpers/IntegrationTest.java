package wdawson.samples.dropwizard.helpers;

import com.google.common.collect.Lists;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.AfterClass;
import org.junit.ClassRule;
import wdawson.samples.dropwizard.UserInfoApplication;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;
import wdawson.samples.dropwizard.util.resources.ConfigurableURLStreamHandlerFactory;

import javax.ws.rs.client.Client;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

/**
 * Base integration test and boiler plate
 *
 * @author wdawson
 */
public class IntegrationTest {

    @ClassRule
    public static final DropwizardAppRule<UserInfoConfiguration> RULE =
            new DropwizardAppRule<>(UserInfoApplication.class, resourceFilePath("dropwizard/valid-conf.yml"));

    @AfterClass
    public static void teardown() throws Exception {
        ConfigurableURLStreamHandlerFactory.unsetURLStringHandlerFactory();
    }

    public Client getNewSecureClient() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(resourceFilePath("tls/homepage-service-keystore.jks")), "notsecret".toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JCEKS");
        trustStore.load(new FileInputStream(resourceFilePath("tls/truststore.jks")), "notsecret".toCharArray());

        TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setKeyStorePath(new File(resourceFilePath("tls/homepage-service-keystore.jks")));
        tlsConfiguration.setKeyStorePassword("notsecret");
        tlsConfiguration.setSupportedCiphers(Lists.newArrayList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));
        tlsConfiguration.setSupportedProtocols(Lists.newArrayList("TLSv1.2"));
        tlsConfiguration.setTrustStorePath(new File(resourceFilePath("tls/truststore.jks")));
        tlsConfiguration.setTrustStorePassword("notsecret");
        tlsConfiguration.setTrustStoreType("JCEKS");
        tlsConfiguration.setVerifyHostname(false);

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTlsConfiguration(tlsConfiguration);

        return new JerseyClientBuilder(RULE.getEnvironment())
                .using(configuration)
                .build(UUID.randomUUID().toString());
    }

}
