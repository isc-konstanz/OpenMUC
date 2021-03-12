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
package org.openmuc.framework.config;

import org.openmuc.framework.config.annotation.AddressSyntax;
import org.openmuc.framework.config.option.Options;

public class Address extends Configurations {

    protected Address(String configuration, Class<? extends Configurable> configurable) throws ArgumentSyntaxException {
        this(configurable.getAnnotation(AddressSyntax.class));
        parse(configuration, Options.parseAddress(configurable));
    }

    private Address(AddressSyntax syntax) throws ArgumentSyntaxException {
        super(syntax != null ? syntax.separator() : AddressSyntax.SEPARATOR_DEFAULT, 
                syntax != null ? syntax.assignmentOperator(): AddressSyntax.ASSIGNMENT_OPERATOR_DEFAULT, 
                syntax != null ? syntax.keyValuePairs() : AddressSyntax.KEY_VAL_PAIRS_DEFAULT);
    }

}
