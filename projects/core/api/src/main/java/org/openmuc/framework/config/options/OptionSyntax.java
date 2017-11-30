package org.openmuc.framework.config.options;

import org.openmuc.framework.config.ArgumentSyntaxException;

public class OptionSyntax implements OptionInfo {

    private final String syntax;

    public OptionSyntax(String syntax) {
        this.syntax = syntax;
    }

	@Override
	public Preferences parse(String settingsStr) throws UnsupportedOperationException, ArgumentSyntaxException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSyntax() {
		return syntax;
	}
}
