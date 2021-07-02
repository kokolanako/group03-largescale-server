package pojo;

import io.ConfigParser;
import lombok.Data;

import java.util.List;

@Data
public class Organisation extends Object{
    private String name;
    private List<Role> roles;
    private List<Employee> employees;

    @Override
  public String toString(){
      StringBuilder sb=new StringBuilder();
      sb.append("Organisation: "+this.name+"\n");
      sb.append("Roles:"+"\n");
      sb.append(ConfigParser.listOfObjectsToString(roles)+"\n");
      sb.append("Employees:"+"\n");
      sb.append(ConfigParser.listOfObjectsToString(employees)+"\n");
      return sb.toString();
    }
}
