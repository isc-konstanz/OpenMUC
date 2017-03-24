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
package org.openmuc.framework.datalogger.ascii;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.ascii.exceptions.WrongCharacterException;
import org.openmuc.framework.datalogger.ascii.exceptions.WrongScalingException;
import org.openmuc.framework.datalogger.ascii.utils.Const;
import org.openmuc.framework.datalogger.ascii.utils.IESDataFormatUtils;
import org.openmuc.framework.datalogger.ascii.utils.LogRecordContainerAscii;
import org.openmuc.framework.datalogger.ascii.utils.LoggerUtils;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileWriter {

    private final String directoryPath;
    private static final Logger logger = LoggerFactory.getLogger(LogFileWriter.class);
    private File actualFile;
    private final boolean isFillUpFiles;

    public LogFileWriter(String directoryPath, boolean isFillUpFiles) {

        this.isFillUpFiles = isFillUpFiles;
        this.directoryPath = directoryPath;
    }

    /**
     * Main logger writing controller.
     * 
     * @param group
     *            log interval container group
     * @param loggingInterval
     *            logging interval
     * @param logTimeOffset
     *            logging time offset
     * @param calendar
     *            calendar of current time
     * @param logChannelList
     *            logging channel list
     */
    public void log(LogIntervalContainerGroup group, int loggingInterval, int logTimeOffset, Calendar calendar,
            HashMap<String, LogChannel> logChannelList) {

        PrintStream out = getStream(group, loggingInterval, logTimeOffset, calendar, logChannelList);

        if (out == null) {
            return;
        }

        List<LogRecordContainer> logRecordContainer = group.getList();

        // TODO match column with container id, so that they don't get mixed up

        if (isFillUpFiles) {
            fillUpFile(loggingInterval, logTimeOffset, calendar, logChannelList, logRecordContainer, out);
        }

        String logLine = getLoggingLine(logRecordContainer, logChannelList, calendar, false);

        out.print(logLine); // print because of println makes different newline char on different systems
        out.flush();
        out.close();
    }

    private void fillUpFile(int loggingInterval, int logTimeOffset, Calendar calendar,
            HashMap<String, LogChannel> logChannelList, List<LogRecordContainer> logRecordContainer, PrintStream out) {

        Long lastLoglineTimestamp = AsciiLogger.getLastLoggedLineTimeStamp(loggingInterval, logTimeOffset);

        if (lastLoglineTimestamp != null && lastLoglineTimestamp > 0) {

            long diff = calendar.getTimeInMillis() - lastLoglineTimestamp;
            if (diff >= loggingInterval) {

                Calendar errCalendar = new GregorianCalendar(Locale.getDefault());
                errCalendar.setTimeInMillis(lastLoglineTimestamp);

                if (errCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
                        && errCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {

                    long numOfErrorLines = diff / loggingInterval;

                    for (int i = 1; i < numOfErrorLines; ++i) {
                        errCalendar.setTimeInMillis(lastLoglineTimestamp + ((long) loggingInterval * i));
                        out.print(getLoggingLine(logRecordContainer, logChannelList, errCalendar, true));
                    }
                }
            }
        }
    }

    private String getLoggingLine(List<LogRecordContainer> logRecordContainer,
            HashMap<String, LogChannel> logChannelList, Calendar calendar, boolean isError32) {

        StringBuilder sb = new StringBuilder();

        LoggerUtils.setLoggerTimestamps(sb, calendar);

        for (int i = 0; i < logRecordContainer.size(); i++) {

            String value = "";
            int size = Const.VALUE_SIZE_MINIMAL;
            boolean left = true;

            Record record = logRecordContainer.get(i).getRecord();
            String channelID = logRecordContainer.get(i).getChannelId();
            LogChannel logChannel = logChannelList.get(channelID);

            if (record != null) {

                Value recordValue = record.getValue();
                Record recordBackup = null;

                if (isError32) {
                    recordBackup = logRecordContainer.get(i).getRecord();
                    logRecordContainer.set(i,
                            new LogRecordContainerAscii(channelID, new Record(Flag.DATA_LOGGING_NOT_ACTIVE)));
                }
                record = logRecordContainer.get(i).getRecord();

                if (record.getFlag() == Flag.VALID) {
                    if (recordValue == null) {
                        // write error flag
                        value = LoggerUtils.buildError(Flag.CANNOT_WRITE_NULL_VALUE);
                        size = getDataTypeSize(logChannel, i);
                    }
                    else {
                        ValueType valueType = logChannel.getValueType();

                        switch (valueType) {
                        case BOOLEAN:
                            value = String.valueOf(recordValue.asShort());
                            break;
                        case LONG:
                            value = String.valueOf(recordValue.asLong());
                            size = Const.VALUE_SIZE_LONG;
                            break;
                        case INTEGER:
                            value = String.valueOf(recordValue.asInt());
                            size = Const.VALUE_SIZE_INTEGER;
                            break;
                        case SHORT:
                            value = String.valueOf(recordValue.asShort());
                            size = Const.VALUE_SIZE_SHORT;
                            break;
                        case DOUBLE:
                        case FLOAT:
                            size = Const.VALUE_SIZE_DOUBLE;
                            try {
                                value = IESDataFormatUtils.convertDoubleToStringWithMaxLength(recordValue.asDouble(),
                                        size);
                            } catch (WrongScalingException e) {
                                value = LoggerUtils.buildError(Flag.UNKNOWN_ERROR);
                                logger.error(e.getMessage() + " ChannelId: " + channelID);
                            }
                            break;
                        case BYTE_ARRAY:
                            left = false;
                            size = checkMinimalValueSize(getDataTypeSize(logChannel, i));
                            byte[] byteArray = recordValue.asByteArray();
                            if (byteArray.length > size) {
                                value = LoggerUtils.buildError(Flag.UNKNOWN_ERROR);
                                logger.error("The byte array is too big, length is " + byteArray.length
                                        + " but max. length allowed is " + size + ", ChannelId: " + channelID);
                            }
                            else {
                                value = Const.HEXADECIMAL + LoggerUtils.byteArrayToHexString(byteArray);
                            }
                            break;
                        case STRING:
                            left = false;
                            size = checkMinimalValueSize(getDataTypeSize(logChannel, i));
                            value = recordValue.toString();
                            int valueLength = value.length();
                            try {
                                checkStringValue(value);
                            } catch (WrongCharacterException e) {
                                value = LoggerUtils.buildError(Flag.UNKNOWN_ERROR);
                                logger.error(e.getMessage());
                            }
                            if (valueLength > size) {
                                value = LoggerUtils.buildError(Flag.UNKNOWN_ERROR);
                                logger.error("The string is too big, length is " + valueLength
                                        + " but max. length allowed is " + size + ", ChannelId: " + channelID);
                            }
                            break;
                        case BYTE:
                            value = String.format("0x%02x", recordValue.asByte());
                            break;
                        default:
                            throw new RuntimeException("unsupported valueType");
                        }
                    }
                }
                else {
                    // write errorflag
                    value = LoggerUtils.buildError(record.getFlag());
                    size = checkMinimalValueSize(getDataTypeSize(logChannel, i));
                }

                if (isError32) {
                    logRecordContainer.set(i, new LogRecordContainerAscii(channelID, recordBackup));
                }
            }
            else {
                // got no data
                value = LoggerUtils.buildError(Flag.UNKNOWN_ERROR);
                size = checkMinimalValueSize(getDataTypeSize(logChannel, i));
            }

            if (left) {
                LoggerUtils.addSpaces(value, size, sb);
                sb.append(value);
            }
            else {
                sb.append(value);
                LoggerUtils.addSpaces(value, size, sb);
            }

            if (LoggerUtils.hasNext(logRecordContainer, i)) {
                sb.append(Const.SEPARATOR);
            }
        }
        sb.append(Const.LINESEPARATOR); // All systems with the same newline charter
        return sb.toString();
    }

    /**
     * Checkes a string if it is IESData conform, e.g. wrong characters. If not it will drop a error.
     * 
     * @param value
     *            the string value which should be checked
     */
    private void checkStringValue(String value) throws WrongCharacterException {

        if (value.contains(Const.SEPARATOR)) {
            throw new WrongCharacterException(
                    "Wrong character: String contains Seperator character: " + Const.SEPARATOR);
        }
        else if (value.startsWith(Const.ERROR)) {
            throw new WrongCharacterException("Wrong character: String begins with: " + Const.ERROR);
        }
        else if (value.startsWith(Const.HEXADECIMAL)) {
            throw new WrongCharacterException("Wrong character: String begins with: " + Const.HEXADECIMAL);
        }
        else if (!value.matches("^[\\x00-\\x7F]*")) {
            throw new WrongCharacterException("Wrong character: Non ASCII character in String.");
        }
    }

    private int checkMinimalValueSize(int size) {

        if (size < Const.VALUE_SIZE_MINIMAL) {
            size = Const.VALUE_SIZE_MINIMAL;
        }
        return size;
    }

    /**
     * Returns the PrintStream for logging.
     * 
     * @param group
     * @param loggingInterval
     * @param date
     * @param logChannelList
     * @return the PrintStream for logging.
     */
    private PrintStream getStream(LogIntervalContainerGroup group, int loggingInterval, int logTimeOffset,
            Calendar calendar, HashMap<String, LogChannel> logChannelList) {

        String filename = LoggerUtils.buildFilename(loggingInterval, logTimeOffset, calendar);

        File file = new File(directoryPath + filename);
        actualFile = file;
        PrintStream out = null;

        try {
            if (file.exists()) {
                out = new PrintStream(new FileOutputStream(file, true), false, Const.CHAR_SET);
            }
            else {
                out = new PrintStream(new FileOutputStream(file, true), false, Const.CHAR_SET);
                String headerString = LogFileHeader.getIESDataFormatHeaderString(group, file.getName(), loggingInterval,
                        logChannelList);

                out.print(headerString);
                out.flush();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Returns the size of a DataType / ValueType.
     * 
     * @param logChannel
     * @param iterator
     * @return size of DataType / ValueType.
     */
    private int getDataTypeSize(LogChannel logChannel, int iterator) {

        int size = Const.VALUE_SIZE_MINIMAL;

        if (logChannel != null) {
            boolean isByteArray = logChannel.getValueType().equals(ValueType.BYTE_ARRAY);
            boolean isString = logChannel.getValueType().equals(ValueType.STRING);

            if (isString) {
                // get length from channel for String / ByteArray
                size = logChannel.getValueTypeLength();
            }
            else if (isByteArray) {
                size = Const.HEXADECIMAL.length() + logChannel.getValueTypeLength() * 2;
            }
            else {
                // get length from channel for simple value types
                size = LoggerUtils.getLengthOfValueType(logChannel.getValueType());
            }
        }
        else {
            // get length from file
            ValueType vt = LoggerUtils.identifyValueType(iterator + Const.NUM_OF_TIME_TYPES_IN_HEADER + 1, actualFile);
            size = LoggerUtils.getLengthOfValueType(vt);
            if ((vt.equals(ValueType.BYTE_ARRAY) || (vt.equals(ValueType.STRING)))
                    && size <= Const.VALUE_SIZE_MINIMAL) {
                size = LoggerUtils.getValueTypeLengthFromFile(iterator + Const.NUM_OF_TIME_TYPES_IN_HEADER + 1,
                        actualFile);
            }
        }
        return size;
    }
}
