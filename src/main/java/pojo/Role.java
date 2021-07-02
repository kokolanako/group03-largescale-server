package pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Role {
    @JsonProperty("ROLE1")
    private String role1;
}
