package wdawson.samples.dropwizard.resources;

import org.junit.Test;
import wdawson.samples.dropwizard.api.UserInfo;
import wdawson.samples.dropwizard.helpers.IntegrationTest;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class UserInfoResourceIT extends IntegrationTest {

    private final UserInfo jane = new UserInfo(1, "Jane Doe");
    private final UserInfo john = new UserInfo(2, "John Doe");

    @Test
    public void userInfoReturnsAllUsersInOrder() throws Exception {

        Client client = getNewSecureClient();

        List<UserInfo> users = client.target(String.format("https://localhost:%d/users", 8443))
                .request()
                .get(new GenericType<List<UserInfo>>() { });

        assertThat(users).containsExactly(jane, john);
    }
}
