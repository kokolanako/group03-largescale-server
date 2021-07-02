package pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Role {
  @JsonProperty("ROLE1")
  private String role1;
  @JsonProperty("ROLE2")
  private String role2;

  @Override
  public String toString(){
    return role1+" "+role2;
  }
}
