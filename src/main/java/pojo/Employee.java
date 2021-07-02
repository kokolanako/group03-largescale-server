/*
 * Copyright (c) 2020, KISTERS AG, Germany.
 * All rights reserved.
 * Modification, redistribution and use in source and binary
 * forms, with or without modification, are not permitted
 * without prior written approval by the copyright holder.
 */
package pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.ConfigParser;
import lombok.Data;

import java.util.List;

/*
 *@author pmrachkovskaya
 */
@Data
public class Employee {

  @JsonProperty("id")
  private int id;
  @JsonProperty("roles")
  private List<String> roles;

  @Override
  public String toString(){
    StringBuilder sb=new StringBuilder();
    sb.append("Employee: "+this.id+"\n");
    sb.append("Roles:"+"\n");
    sb.append(ConfigParser.listOfObjectsToString(roles));
    return sb.toString();
  }

}
