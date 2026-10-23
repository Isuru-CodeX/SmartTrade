package lk.jiat.smarttrade.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Env {
    private static final Properties APP_PROPERTIES = new Properties();

    static {
        try {
            InputStream inputStream = Env.class.getClassLoader().getResourceAsStream("app.properties");
            APP_PROPERTIES.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Application properties loading failed: " + e.getMessage());
        }
    }

    public static String get(String key){
        String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }
        envValue = System.getenv(key.replace('.', '_').toUpperCase());
        if (envValue != null) {
            return envValue;
        }
        return APP_PROPERTIES.getProperty(key);
    }
    public static Properties getAppProperties(){
        return APP_PROPERTIES;
    }
}
