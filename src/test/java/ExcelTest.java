import dev.phonis.cannondebugextra.excel.ExcelManager;
import dev.phonis.cannondebugextra.networking.*;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelTest {

    @Test
    public void testExcelGeneration1() throws IOException {
        ExcelTest.testFile("src/test/resources/testhistory.cdhistory");
    }

    @Test
    public void testExcelGeneration2() throws IOException {
        ExcelTest.testFile("src/test/resources/testhistory2.cdhistory");
    }

    private static void testFile(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        DataInputStream dis = new DataInputStream(fis);
        CDHistory history = CDHistory.fromBytes(dis);

        dis.close();
        ExcelManager.viewAsExcel(history);
    }

}