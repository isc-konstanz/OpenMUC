package org.openmuc.framework.config.options;

import java.util.Map;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.data.Value;

public class OptionSyntax implements OptionInfo {

    private final String syntax;

    public OptionSyntax(String syntax) {
        this.syntax = syntax;
    }

	@Override
	public Map<String, Value> parse(String settingsStr) throws UnsupportedOperationException, ArgumentSyntaxException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSyntax() {
		return syntax;
	}
}
