package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import pojo.Config;

import java.io.File;
import java.io.IOException;

public class ConfigParser {

    public Config parse(String path) {
        File configFile = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        Config configDTO = null;
        try {
            configDTO = objectMapper.readValue(configFile, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configDTO;
    }
}
