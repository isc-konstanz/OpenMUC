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
package org.openmuc.framework.driver.csv.settings;

import org.openmuc.framework.config.info.DeviceOptions;
import org.openmuc.framework.config.info.Option;
import org.openmuc.framework.config.info.OptionCollection;
import org.openmuc.framework.config.info.OptionSelection;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.driver.csv.ESampleMode;

public class CsvDeviceOptions extends DeviceOptions {
    
    private static final String DESCRIPTION = "The devices of the CSV driver "
            + "each represent specific files. Each device may contain several columns, "
            + "addressed by their headers, and is read either line by line "
            + "or by a defined index.";

    public static final String PATH_FILE = "filepath";
    public static final String PATH_DIR = "path";
    public static final String SAMPLING_MODE = "samplingmode";
    public static final ESampleMode SAMPLING_MODE_DEFAULT = ESampleMode.LINE;
    public static final String REWIND = "rewind";
    public static final boolean REWIND_DEFAULT = true;

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void configureAddress(OptionCollection address) throws UnsupportedOperationException {
        address.setDelimiter(";");
        address.enableKeyValuePairs(false);
        
        address.add(filePath());
    }

    @Override
    protected void configureSettings(OptionCollection settings) throws UnsupportedOperationException {
        settings.setDelimiter(";");
        settings.setKeyValueSeperator("=");
        
        settings.add(samplingMode());
        settings.add(rewind());
    }

    @Override
    protected void configureScanSettings(OptionCollection scanSettings) throws UnsupportedOperationException {
        scanSettings.setDelimiter(";");
        scanSettings.setKeyValueSeperator("=");
        
        scanSettings.add(dirPath());
    }

    private Option filePath() {
        
        Option filePath = new Option(PATH_FILE, "CSV file path", ValueType.STRING);
        filePath.setDescription("The systems path to the CSV file.</br></br>"
                + "<b>Example:</b> /home/usr/bin/openmuc/csv/meter.csv");
        filePath.setMandatory(true);
        
        return filePath;
    }
    
    private Option dirPath() {
        
        Option filePath = new Option(PATH_DIR, "CSV files directory path", ValueType.STRING);
        filePath.setDescription("The systems path to the folder, containing the CSV files.</br></br>"
                + "<b>Example:</b> /home/usr/bin/openmuc/csv/");
        filePath.setMandatory(true);
        
        return filePath;
    }

    private Option samplingMode() {
        
        Option samplingMode = new Option(SAMPLING_MODE, "Sampling mode", ValueType.STRING);
        samplingMode.setDescription("The sampling mode configures the drivers method to read the CSV file:</br></br>"
                + "<b>Modes:</b>"
                + "<ol><li><b>unixtimestamp</b>: Find the line closest to the sampling timestamp in the <em>unixtimestamp</em> column.</li>"
                + "<li><b>hhmmss</b>: Find the line closest to the sampling times hours, minutes and seconds in the <em>hhmmss</em> column.</li>"
                + "<li><b>line</b>: Read the file line by line.</li></ol>");
        samplingMode.setMandatory(true);
        
        OptionSelection selection = new OptionSelection(ValueType.STRING);
        selection.addString("Unix timestamp", "unixtimestamp");
        selection.addString("Closest hour, minute, second", "hhmmss");
        selection.addString("Line by line", "line");
        samplingMode.setValueSelection(selection);
        samplingMode.setValueDefault(new StringValue(SAMPLING_MODE_DEFAULT.toString()));
        
        return samplingMode;
    }

    private Option rewind() {
        
        Option rewind = new Option(REWIND, "Rewind", ValueType.BOOLEAN);
        rewind.setDescription("Start from the beginning of the file again, when the end was reached.");
        rewind.setMandatory(false);
        rewind.setValueDefault(new BooleanValue(REWIND_DEFAULT));
        
        return rewind;
    }

}
