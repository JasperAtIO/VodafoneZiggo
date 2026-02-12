package nl.vodafoneziggo.external.reqres;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class ReqresClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8089))
            .build();

    private ReqresClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        client = new ReqresClient(restTemplate, "http://localhost:8089/api/users", "");
    }

    @Test
    void findUserByEmail_whenFoundOnSecondPage_returnsUser_andFetchesTwoPages() {
        // Arrange
        wireMock.stubFor(get(urlEqualTo("/api/users?page=1"))
                .willReturn(okJson("""
                        {
                          "page": 1,
                          "total_pages": 2,
                          "data": [
                            { "id": 1, "email": "first@example.com", "first_name": "First", "last_name": "User" }
                          ]
                        }
                        """)));

        wireMock.stubFor(get(urlEqualTo("/api/users?page=2"))
                .willReturn(okJson("""
                        {
                          "page": 2,
                          "total_pages": 2,
                          "data": [
                            { "id": 2, "email": "target@example.com", "first_name": "Target", "last_name": "User" }
                          ]
                        }
                        """)));

        // Act
        Optional<ReqresUser> result = client.findUserByEmail("TARGET@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2);
        assertThat(result.get().getEmail()).isEqualTo("target@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("Target");
        assertThat(result.get().getLastName()).isEqualTo("User");

        // Verify both requests were made
        wireMock.verify(getRequestedFor(urlEqualTo("/api/users?page=1")));
        wireMock.verify(getRequestedFor(urlEqualTo("/api/users?page=2")));
    }

    @Test
    void findUserByEmail_whenNotFound_returnsEmpty_andStopsAfterLastPage() {
        // Arrange: 2 pages, no matching email on either
        wireMock.stubFor(get(urlEqualTo("/api/users?page=1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "page": 1,
                                  "total_pages": 2,
                                  "data": [
                                    { "id": 1, "email": "a@example.com", "first_name": "A", "last_name": "User" }
                                  ]
                                }
                                """)));

        wireMock.stubFor(get(urlEqualTo("/api/users?page=2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "page": 2,
                                  "total_pages": 2,
                                  "data": [
                                    { "id": 2, "email": "b@example.com", "first_name": "B", "last_name": "User" }
                                  ]
                                }
                                """)));

        // Act
        Optional<ReqresUser> result = client.findUserByEmail("missing@example.com");

        // Assert
        assertThat(result).isEmpty();

        // Verify both pages were requested
        wireMock.verify(getRequestedFor(urlEqualTo("/api/users?page=1")));
        wireMock.verify(getRequestedFor(urlEqualTo("/api/users?page=2")));
    }

    @Test
    void findUserByEmail_whenTotalPagesMissing_returnsEmpty_afterFirstPage() {
        // Arrange: total_pages is absent -> client should stop and return empty
        wireMock.stubFor(get(urlEqualTo("/api/users?page=1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "page": 1,
                                  "data": [
                                    { "id": 1, "email": "a@example.com", "first_name": "A", "last_name": "User" }
                                  ]
                                }
                                """)));

        // Act
        Optional<ReqresUser> result = client.findUserByEmail("missing@example.com");

        // Assert
        assertThat(result).isEmpty();

        // Verify only first page was requested
        wireMock.verify(getRequestedFor(urlEqualTo("/api/users?page=1")));
        wireMock.verify(0, getRequestedFor(urlEqualTo("/api/users?page=2")));
    }

}
