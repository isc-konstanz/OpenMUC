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
package org.openmuc.framework.driver.mbus.options;

import org.openmuc.framework.config.options.ChannelOptions;
import org.openmuc.framework.config.options.Option;
import org.openmuc.framework.config.options.OptionCollection;
import org.openmuc.framework.data.ValueType;

public class MBusChannelOptions extends ChannelOptions {
    
    private static final String DESCRIPTION = "A channel references data records (sometimes called variable data blocks), which contain the measured data. " +
    		"Each data record is made up of a data information block (DIB), a value information block (VIB) and a value. " +
    		"Similar to OBIS codes DIBs and VIBs code information such as the meaning of a value.";

    public static final String DIB_KEY = "dib";
    public static final String VIB_KEY = "vib";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void configureAddress(OptionCollection address) {
        address.setSyntax(":");

        address.add(dib());
        address.add(vib());
    }

    @Override
    protected void configureScanSettings(OptionCollection scanSettings) {
        // No parameters required
    }

    private Option dib() {
        
        Option dib = new Option(DIB_KEY, "Data Information Block", ValueType.STRING);
        dib.setDescription("The DIB codes:<ol>" +
        		"<li><b>Storage number</b> – a meter can have several storages e.g. to store historical time series data. The storage number 0 signals an actual value.</li>" +
        		"<li><b>Function</b> – Data can have the following four function types: instantaneous value, max value, min value, value during error state.</li>" +
        		"<li><b>Data value type</b> – The length and coding of the data value field following the DIB and VIB. Possible value types are 8/16/24/32/48/64 bit integer, 32 bit real, 2/4/6/8/12 digit binary coded decimals (BCD), date and string. In addition the value type “none” exists to label data records that have no data value field.</li>" +
        		"<li><b>Tariff</b> – Indicates the tariff number of this data field. The data of tariff 0 is usually the sum of all other tariffs.</li>" +
        		"<li><b>Subunit</b> – Can be used by a slave to distinguish several subunits of the metering device.</li></ol>");
        dib.setMandatory(true);
        
        return dib;
    }

    private Option vib() {
        
        Option vib = new Option(VIB_KEY, "Value Information Block", ValueType.STRING);
        vib.setDescription("The VIB codes:<ol>" +
        		"<li><b>Description</b> – The meaning of the data value (e.g. “Energy”, “Volume” etc.)</li>" +
        		"<li><b>Unit</b> – The unit of the data value.</li>" +
        		"<li><b>Multiplier</b> – A factor by which the data value coded in the data field has to be multiplied with.</li></ol>");
        vib.setMandatory(true);
        
        return vib;
    }

}
