
package pojo;

import io.ConfigParser;
import lombok.Data;

import java.util.List;

/*
 *@author pmrachkovskaya
 */
@Data
public class PersonDTO {

  private String id;
  private List<String> roles;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("-Employee: " + this.id + "\n");
    sb.append("-Roles:" + "\n");
    sb.append(ConfigParser.listOfObjectsToString(roles));
    return sb.toString();
  }


}
