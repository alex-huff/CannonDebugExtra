package dev.phonis.cannondebugextra.excel;

import dev.phonis.cannondebugextra.networking.CDBlockSelection;
import dev.phonis.cannondebugextra.networking.CDHistory;
import dev.phonis.cannondebugextra.networking.CDLocation;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ExcelManager {

    public static final BlockingQueue<CDHistory> historyQueue = new LinkedBlockingQueue<>();
    private static final File excelFolder = new File("cannondebug/");

    static {
        if (excelFolder.mkdirs()) System.out.println("Creating excel directory.");

        new Thread(ExcelManager::loop).start();
    }

    private static void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ExcelManager.viewAsExcel(ExcelManager.historyQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();

                Thread.currentThread().interrupt();
            }
        }
    }

    private static void viewAsExcel(CDHistory history) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        for (CDBlockSelection selection : history.selections) {
            XSSFSheet spreadsheet = workbook.createSheet(Integer.toString(selection.id));

            for (int i = 0; i < selection.tracker.locationHistory.size(); i++) {
                CDLocation location = selection.tracker.locationHistory.get(i);
                XSSFRow row = spreadsheet.createRow(i);

                row.createCell(0).setCellValue(location.x);
                row.createCell(1).setCellValue(location.y);
                row.createCell(2).setCellValue(location.z);
            }
        }

        try {
            File excelFile = new File(ExcelManager.excelFolder, "history" + UUID.randomUUID().toString().replace("-", "") + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(excelFile);

            workbook.write(fileOut);
            fileOut.close();
            Desktop.getDesktop().open(excelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
