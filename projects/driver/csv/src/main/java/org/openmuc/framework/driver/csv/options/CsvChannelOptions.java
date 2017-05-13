/*
 * Copyright 2011-16 Fraunhofer ISE
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
package org.openmuc.framework.driver.csv.options;

import org.openmuc.framework.config.options.ChannelOptions;
import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.data.ValueType;

public class CsvChannelOptions extends ChannelOptions {
    
    private static final String DESCRIPTION = "The channels of the CSV driver "
            + "each represent a single column of the configured file.";

    public static final String HEADER = "header";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void configureAddress(OptionCollection address) {
        address.setSyntax(";");
        
        address.add(header());
    }

    @Override
    protected void configureScanSettings(OptionCollection scanSettings) {
        // No parameters required
    }

    private Option header() {
        
        Option header = new Option(HEADER, "Column header", ValueType.STRING);
        header.setDescription("The title of the header, defining the column.");
        header.setMandatory(true);
        
        return header;
    }

}
