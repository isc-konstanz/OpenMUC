package org.openmuc.framework.lib.filePersistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class FilePersistenceTest {
    private static FilePersistence filePersistence;
    private static final String DIRECTORY = "/tmp/OpenMUC-filePersistenceTest";

    @AfterAll
    static void cleanUp() {
        FileSystems.getDefault().getPath(DIRECTORY).toFile().delete();
    }

    @Test
    void fileLogInvalidMaxFileCount() {
        // Nothing can be written into a file, when no file creation is allowed
        filePersistence = new FilePersistence(DIRECTORY, 0, 16);
        Exception exception = assertThrows(IOException.class, () -> filePersistence.fileLog("test", "test".getBytes()));
        assertEquals("maxFileSize is 0", exception.getMessage());
    }

    @Test
    void fileLogSingle() throws IOException {
        // If only one file is allowed this one file needs to be overwritten immediately when maxFileSize is reached
        filePersistence = new FilePersistence(DIRECTORY, 1, 16);
        filePersistence.fileLog("test", "test1234test123".getBytes());
        filePersistence.fileLog("test", "test1234test123".getBytes());
        File file = FileSystems.getDefault().getPath(DIRECTORY, "test").toFile();
        File file1 = FileSystems.getDefault().getPath(DIRECTORY, "test.1").toFile();
        assertTrue(file.exists());
        assertFalse(file1.exists());
        file.delete();
    }

    @Test
    void fileLogMultiple() throws IOException {
        // Proofs whether the maxFileCount limit is recognized
        filePersistence = new FilePersistence(DIRECTORY, 2, 16);
        filePersistence.fileLog("test", "test1234test123".getBytes());
        filePersistence.fileLog("test", "test1234test123".getBytes());
        filePersistence.fileLog("test", "test1234test123".getBytes());
        File file = FileSystems.getDefault().getPath(DIRECTORY, "test").toFile();
        File file1 = FileSystems.getDefault().getPath(DIRECTORY, "test.1").toFile();
        // file2 is not being created
        File file2 = FileSystems.getDefault().getPath(DIRECTORY, "test.2").toFile();
        assertTrue(file.exists() && file1.exists() && !file2.exists());
        file.delete();
        file1.delete();
    }

    @Test
    void emptyFile() throws IOException {
        // proofs whether overwriting happens correctly, order of emptying is correct and files got deleted
        // as this behaviour is connected together this is in a single test
        filePersistence = new FilePersistence(DIRECTORY, 2, 16);
        // the next message gets overwritten
        filePersistence.fileLog("test", "test1234test121".getBytes());
        filePersistence.fileLog("test", "test1234test122".getBytes());
        // the next message overwrites the first message
        filePersistence.fileLog("test", "test1234test123".getBytes());

        String recoveredMessage1 = new String(filePersistence.emptyFile("test"));
        String recoveredMessage2 = new String(filePersistence.emptyFile("test"));
        // next should be null, so we can't construct a String out of it
        byte[] recoveredMessage3 = filePersistence.emptyFile("test");

        assertEquals("test1234test122", recoveredMessage1);
        assertEquals("test1234test123", recoveredMessage2);
        // file limit was reached, so one message got intentionally lost
        assertNull(recoveredMessage3);

        File file = FileSystems.getDefault().getPath(DIRECTORY, "test").toFile();
        File file1 = FileSystems.getDefault().getPath(DIRECTORY, "test.1").toFile();
        // both files got deleted
        assertFalse(file.exists() || file1.exists());
    }

    @Test
    void getFileNames() throws IOException {
        // checks for correct suffix removing with unproblematic fileNames
        // unproblematic fileNames are names without a ".<number>" suffix
        filePersistence = new FilePersistence(DIRECTORY, 2, 16);
        filePersistence.fileLog("test", "test1234test121".getBytes());
        filePersistence.fileLog("test", "test1234test122".getBytes());
        filePersistence.fileLog("test1", "test1234test121".getBytes());
        filePersistence.fileLog("test1", "test1234test122".getBytes());

        String[] fileNames = filePersistence.getFileNames();
        assertEquals(fileNames.length, 2);
        assertEquals("test", fileNames[0]);
        assertEquals("test1", fileNames[1]);

        FileSystems.getDefault().getPath(DIRECTORY, "test").toFile().delete();
        FileSystems.getDefault().getPath(DIRECTORY, "test.1").toFile().delete();
    }
}
