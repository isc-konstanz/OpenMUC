/*
 * Copyright 2011-2021 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.config.option; 

import static java.util.Arrays.stream;

import org.openmuc.framework.config.option.annotation.Syntax;
import org.openmuc.framework.config.Configurable;
import org.openmuc.framework.config.option.annotation.OptionType;

public class OptionSyntax {

    protected String separator;
    protected String assignment;
    protected boolean keyValue;

    public OptionSyntax(OptionType type) {
    	setSyntaxDefault(type);
    }

    public OptionSyntax(OptionType type, Class<? extends Configurable> configs) {
        this(type, configs.getAnnotation(Syntax.class));
    }

    public OptionSyntax(OptionType type, Syntax syntax) {
        if (syntax != null) {
        	setSyntax(type, syntax);
        }
        else {
        	setSyntaxDefault(type);
        }
    }

    void setSyntaxDefault(OptionType type) {
        separator = Syntax.SEPARATOR_DEFAULT;
        assignment = Syntax.ASSIGNMENT_DEFAULT;
        keyValue = stream(Syntax.KEY_VAL_PAIRS_DEFAULT).anyMatch(type::equals);
    }

    void setSyntax(OptionType type, Syntax syntax) {
        separator = syntax.separator();
        assignment = syntax.assignment();
        keyValue = stream(syntax.keyValuePairs()).anyMatch(type::equals);
    }

    void setSyntax(String separator) {
        this.separator = separator;
        this.assignment = null;
        this.keyValue = false;
    }

    void setSyntax(String separator, String assignment) {
        this.separator = separator;
        this.assignment = assignment;
        this.keyValue = true;
    }

    public String getSeparator() {
        return separator;
    }

    OptionSyntax setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public String getAssignment() {
        return assignment;
    }

    OptionSyntax setAssignmentOperator(String assignment) {
        this.assignment = assignment;
        return this;
    }

    public boolean hasKeyValuePairs() {
        return keyValue;
    }

    OptionSyntax setKeyValuePairs(boolean enable) {
        if (!enable) {
            this.assignment = null;
        }
        this.keyValue = enable;
        return this;
    }

}
