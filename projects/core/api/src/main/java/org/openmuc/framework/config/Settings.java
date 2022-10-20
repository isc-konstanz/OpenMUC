/*
 * Copyright 2011-2022 Fraunhofer ISE
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

import org.openmuc.framework.config.option.OptionSyntax;
import org.openmuc.framework.config.option.Options;
import org.openmuc.framework.config.option.annotation.Syntax;
import org.openmuc.framework.config.option.annotation.OptionType;

public class Settings extends Configurations {

    public Settings(String configuration, Class<? extends Configurable> configurable) 
            throws ArgumentSyntaxException {
        
        this(configurable.getAnnotation(Syntax.class));
        parse(configuration, Options.parse(OptionType.SETTING, configurable));
    }

    public Settings(String configuration, Class<? extends Configurable> configurable, OptionSyntax syntax) 
            throws ArgumentSyntaxException {
        
        this(syntax);
        parse(configuration, Options.parse(OptionType.SETTING, syntax, configurable));
    }

    private Settings(Syntax syntax) throws ArgumentSyntaxException {
        this(new OptionSyntax(OptionType.SETTING, syntax));
    }

    private Settings(OptionSyntax syntax) {
        super(syntax);
    }

}
