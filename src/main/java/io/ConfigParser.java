package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import pojo.Config;
import pojo.Organisation;
import pojo.Role;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigParser {

  public static Config parse(String path) {
    InputStream is = ConfigParser.class.getClassLoader().getResourceAsStream(path);
//    File configFile = new File(is);
    ObjectMapper objectMapper = new ObjectMapper();
    Config configDTO = null;
    try {
      configDTO = objectMapper.readValue(is, Config.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return configDTO;
  }

  public static String listOfObjectsToString(List<?> objects) {
    StringBuilder sb = new StringBuilder();
    for (Object object : objects) {
      sb.append(object.toString() + "\n");
    }
    return sb.toString();
  }


}
