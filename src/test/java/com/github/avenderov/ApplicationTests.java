package com.github.avenderov;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {

    private static final String DYNAMIC_MESSAGE_KEY = "config/application/dynamic/message";

    private static final String INITIAL_VALUE = "foobar";

    private static GenericContainer consul =
            new GenericContainer("consul:1.1.0")
                    .withExposedPorts(8500)
                    .waitingFor(Wait.forHttp("/v1/agent/self"));

    static {
        consul.start();

        final String ipAddress = consul.getContainerIpAddress();
        final Integer port = consul.getMappedPort(8500);

        System.setProperty("consul.host", ipAddress);
        System.setProperty("consul.port", Integer.toString(port));

        final ConsulClient consulClient = new ConsulClient(ipAddress, port);
        consulClient.setKVValue(DYNAMIC_MESSAGE_KEY, INITIAL_VALUE);
    }

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Environment environment;

    @Test
    public void shouldNotUseIncorrectValue() {
        final MessageDto responseBefore = restTemplate.getForObject("/message", MessageDto.class);
        assertThat(responseBefore.getMessage()).isEqualTo(INITIAL_VALUE);

        // Setting incorrect value: length less than minimum
        final String incorrectValue = "foo";
        final Response<Boolean> setValueResponse = consulClient.setKVValue(DYNAMIC_MESSAGE_KEY, incorrectValue);
        assertThat(setValueResponse.getValue()).isTrue();

        // XXX: Following asserts should fail, incorrect value must not be put into the environment
        await().atMost(5L, TimeUnit.SECONDS).pollInterval(500L, TimeUnit.MILLISECONDS)
                .until(() -> Objects.equals(environment.getProperty("dynamic.message"), incorrectValue));

        final MessageDto responseAfter = restTemplate.getForObject("/message", MessageDto.class);
        assertThat(responseAfter.getMessage()).isEqualTo(incorrectValue);
    }

}
