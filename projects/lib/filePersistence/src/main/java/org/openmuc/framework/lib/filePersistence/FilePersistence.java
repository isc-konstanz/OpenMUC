package org.openmuc.framework.lib.filePersistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides configurable RAM friendly file persistence functionality
 */
public class FilePersistence {
    private static final Logger logger = LoggerFactory.getLogger(FilePersistence.class);
    private final Path DIRECTORY;
    private final int MAX_FILE_COUNT;
    private final long MAX_FILE_SIZE;
    private final Map<String, Integer> currentFile = new HashMap<>();
    private final Map<String, Long> readBytes = new HashMap<>();

    /**
     * @param directory
     *            the directory in which files are stored
     * @param maxFileCount
     *            the maximum number of files created Must be greater than 0
     * @param maxFileSize
     *            the maximum file size in bytes when fileSize is reached a new file is created or the oldest
     *            overwritten
     */
    public FilePersistence(String directory, int maxFileCount, long maxFileSize) {
        DIRECTORY = FileSystems.getDefault().getPath(directory);
        MAX_FILE_COUNT = maxFileCount;
        MAX_FILE_SIZE = maxFileSize;

        if (!DIRECTORY.toFile().exists()) {
            if (!DIRECTORY.toFile().mkdirs()) {
                logger.error("The directory {} could not be created", DIRECTORY);
            }
        }
    }

    /**
     * @param fileName
     *            the base fileName without suffix
     * @param payload
     *            the data to be written. needs to be smaller than MAX_FILE_SIZE
     * @throws IOException
     *             when writing fails
     */
    public void fileLog(String fileName, byte[] payload) throws IOException {
        if (MAX_FILE_COUNT == 0) {
            throw new IOException("maxFileSize is 0");
        }
        if (payload.length >= MAX_FILE_SIZE) {
            logger.error("Payload is bigger than maxFileSize. Current maxFileSize is {}kB", MAX_FILE_SIZE / 1024);
            return;
        }
        File file = Paths.get(DIRECTORY.toString(), fileName).toFile();
        boolean full = false;

        try (FileOutputStream fileStream = new FileOutputStream(file, true)) {
            // current file size + payload size + newline >= max allowed file size
            if (file.length() + payload.length + 1 > MAX_FILE_SIZE) {
                full = true;
            }
            else {
                fileStream.write(payload);
                fileStream.write("\n".getBytes());
            }
        } finally {
            if (full) {
                if (MAX_FILE_COUNT > 1) {
                    if (currentFile.containsKey(fileName)) {
                        currentFile.put(fileName, currentFile.get(fileName) + 1);
                    }
                    else {
                        currentFile.put(fileName, 1);
                    }
                    if (currentFile.get(fileName) == MAX_FILE_COUNT) {
                        currentFile.put(fileName, 1);
                    }
                    Files.move(file.toPath(),
                            Paths.get(DIRECTORY.toString(), fileName + '.' + currentFile.get(fileName)),
                            StandardCopyOption.REPLACE_EXISTING);
                    fileLog(fileName, payload);
                }
                else {
                    if (!file.delete()) {
                        logger.error("Failed to delete {}", fileName);
                    }
                    else {
                        fileLog(fileName, payload);
                    }
                }
            }
        }
    }

    /**
     * @param fileName
     *            the base fileName without suffix
     * @return a single line read
     * @throws IOException
     *             when reading fails
     */
    public byte[] emptyFile(String fileName) throws IOException {
        return emptyFile(fileName, 0);
    }

    private byte[] emptyFile(String fileName, int depth) throws IOException {
        File file;
        if (depth > 0) {
            file = Paths.get(DIRECTORY.toString(), fileName + '.' + depth).toFile();
        }
        else {
            file = Paths.get(DIRECTORY.toString(), fileName).toFile();
        }

        if (!file.exists()) {
            return null;
        }

        if (Paths.get(DIRECTORY.toString(), fileName + '.' + (depth + 1)).toFile().exists()) {
            byte[] line = emptyFile(fileName, depth + 1);
            if (line != null) {
                return line;
            }
        }

        Long readBytes = this.readBytes.get(fileName);
        if (readBytes == null) {
            readBytes = 0L;
        }

        StringBuilder line;
        try (FileInputStream fileStream = new FileInputStream(file)) {
            line = new StringBuilder();
            fileStream.skip(readBytes);
            int next = fileStream.read();
            readBytes++;
            while (next != -1 && next != '\n') {
                line.appendCodePoint(next);
                next = fileStream.read();
                readBytes++;
            }
        }

        this.readBytes.put(fileName, readBytes);

        if (line.toString().isEmpty()) {
            this.readBytes.put(fileName, 0L);
            if (!file.delete()) {
                logger.error("Something went wrong while deleting {}", file.getName());
            }
            return emptyFile(fileName, depth - 1);
        }

        logger.info("Accessed file {} with content {}", fileName, line.toString());

        return line.toString().getBytes();
    }

    /**
     * Tries to remove suffixes but this is not guaranteed
     *
     * @return the files in this directory
     */
    public String[] getFileNames() {
        List<String> fileNames = new ArrayList<>();
        String previousFileName = null;
        String[] files = DIRECTORY.toFile().list();
        Arrays.sort(files);
        for (String fileName : files) {
            if (previousFileName != null && fileName.startsWith(previousFileName)) {
                String[] split = fileName.split("\\.");
                if (split.length == 2 && split[1].matches("\\d+")) {
                    continue;
                }
            }
            fileNames.add(fileName);
            previousFileName = fileName;
        }
        return fileNames.toArray(new String[] {});
    }
}
