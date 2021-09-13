package dev.phonis.cannondebugextra.excel;

import dev.phonis.cannondebugextra.networking.CDBlockSelection;
import dev.phonis.cannondebugextra.networking.CDHistory;
import dev.phonis.cannondebugextra.networking.CDLocation;
import net.minecraft.entity.EntityTracker;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelManager {

    private static final File excelFile = new File("cannondebug.xlsx");

    public static void viewAsExcel(CDHistory history) {
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
            FileOutputStream fileOut = new FileOutputStream(ExcelManager.excelFile);

            workbook.write(fileOut);
            fileOut.close();

            Desktop.getDesktop().open(excelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
