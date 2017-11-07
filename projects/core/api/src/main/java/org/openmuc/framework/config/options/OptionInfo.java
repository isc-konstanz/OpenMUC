package org.openmuc.framework.config.options;

import org.openmuc.framework.config.ArgumentSyntaxException;

public interface OptionInfo {

    public Parameters parse(String str) throws UnsupportedOperationException, ArgumentSyntaxException;

    public String getSyntax();

}
