package pojo;

import io.ConfigParser;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Config {
    public List<Organisation> organizations;

    @Override
  public String toString(){

      return ConfigParser.listOfObjectsToString(this.organizations);
    }
}
