package pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.ConfigParser;
import lombok.Data;

import java.util.List;

@Data
public class Organisation extends Object {
  @JsonProperty("name")
  private String name;
  @JsonProperty("roles")
  private List<String> roles;
  @JsonProperty("employees")
  private List<Employee> employees;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Organisation: " + this.name + "\n");
    sb.append("Roles:" + "\n");
    sb.append(roles + "\n");
    sb.append("Employees:" + "\n");
    sb.append(ConfigParser.listOfObjectsToString(employees) + "\n");
    return sb.toString();
  }
}
