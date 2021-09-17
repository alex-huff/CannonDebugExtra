import dev.phonis.cannondebugextra.excel.ExcelManager;
import dev.phonis.cannondebugextra.networking.*;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelTest {

    @Test
    public void testExcelGeneration() throws IOException {
        FileInputStream fis = new FileInputStream("src/test/resources/testhistory.cdhistory");
        CDHistory history = CDHistory.fromBytes(new DataInputStream(fis));

        ExcelManager.viewAsExcel(history);
    }

}