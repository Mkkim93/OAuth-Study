package oauth.oauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {

    private String username;
    private String name;
    private String role;
}
