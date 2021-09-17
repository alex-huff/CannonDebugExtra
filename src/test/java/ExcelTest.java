import dev.phonis.cannondebugextra.excel.ExcelManager;
import dev.phonis.cannondebugextra.networking.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelTest {

    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("src/test/resources/testhistory.cdhistory");
        CDHistory history = CDHistory.fromBytes(new DataInputStream(fis));

        ExcelManager.viewAsExcel(history);
        System.exit(0);
    }

}