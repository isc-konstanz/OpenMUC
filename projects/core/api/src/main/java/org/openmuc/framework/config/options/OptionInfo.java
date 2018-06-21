package org.openmuc.framework.config.options;

import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Value;

public interface OptionInfo {

    public Map<String, Value> parse(String settingsStr) throws UnsupportedOperationException, ArgumentSyntaxException;

    public String getSyntax();

}
