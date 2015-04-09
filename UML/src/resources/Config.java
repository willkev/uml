package resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import utils.PopUp;

public class Config {

    private static final ResourceBundle RESOURCE = ResourceBundle.getBundle("resources.config");

    private Config() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE.getString(key);
        } catch (MissingResourceException ex) {
            PopUp.error(ex);
            return null;
        }
    }
}
