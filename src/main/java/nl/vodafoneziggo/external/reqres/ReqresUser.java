package nl.vodafoneziggo.external.reqres;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a user entity from the Reqres API. This class provides a
 * mapping for JSON fields retrieved from the API and is used to
 * encapsulate user-specific details such as ID, email, first name,
 * and last name.
 * <p>
 * The fields in this class correspond to the structure of the user
 * object returned by the Reqres API, including JSON property aliases
 * detected using Jackson annotations.
 * <p>
 * This class is primarily utilized in API interactions within the
 * {@code ReqresClient} and {@code ReqresUsersResponse} classes.
 */
@AllArgsConstructor
@Data
public class ReqresUser {
    private Integer id;
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;
}
