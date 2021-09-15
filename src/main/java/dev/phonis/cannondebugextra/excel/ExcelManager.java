package dev.phonis.cannondebugextra.excel;

import dev.phonis.cannondebugextra.networking.*;
import dev.phonis.cannondebugextra.util.NumberUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ExcelManager {

    public static final BlockingQueue<CDHistory> historyQueue = new LinkedBlockingQueue<>();
    private static final File excelFolder = new File("cannondebug/");
    private static final String TNTString = "TNT";
    private static final String fallingBlockString = "SAND";
    private static final String otherString = "OTHER";

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
        if (history.selections.size() == 0) return;

        XSSFWorkbook workbook = new XSSFWorkbook();

        if (history.byOrder) history.selections.sort(Comparator.comparingLong((CDBlockSelection o) -> o.tracker.spawnTick).thenComparingInt(blockSelection -> blockSelection.order));

        for (CDBlockSelection selection : history.selections) {
            XSSFSheet spreadsheet = workbook.createSheet(history.byOrder ? ("Tick" + selection.tracker.spawnTick + " OOE" + selection.order) : Integer.toString(selection.id));
            XSSFRow startRow = spreadsheet.createRow(0);

            startRow.createCell(0).setCellValue("Tick");
            startRow.createCell(1).setCellValue("Entity Type");
            startRow.createCell(2).setCellValue("X");
            startRow.createCell(3).setCellValue("Y");
            startRow.createCell(4).setCellValue("Z");
            startRow.createCell(5).setCellValue("X Velocity");
            startRow.createCell(6).setCellValue("Y Velocity");
            startRow.createCell(7).setCellValue("Z Velocity");
            startRow.createCell(8).setCellValue("XZ 1x1");
            startRow.createCell(9).setCellValue("Y 1x1");

            for (int i = 1; i < selection.tracker.locationHistory.size() + 1; i++) {
                CDLocation location = selection.tracker.locationHistory.get(i - 1);
                CDVec3D velocity = selection.tracker.velocityHistory.get(i - 1);
                XSSFRow row = spreadsheet.createRow(i);
                boolean xz1x1 = false;
                boolean y1x1 = false;
                String typeString;

                switch (selection.tracker.entityType) {
                    case TNT:
                        typeString = ExcelManager.TNTString;

                        break;
                    case FALLINGBLOCK:
                        typeString = ExcelManager.fallingBlockString;

                        break;
                    default:
                        typeString = ExcelManager.otherString;

                        break;
                }

                if (NumberUtils.isInsideCube(location.x) && NumberUtils.isInsideCube(location.z) ||
                    Math.abs(velocity.x) != 0.0 && NumberUtils.isInsideCube(location.x) ||
                    Math.abs(velocity.z) != 0.0 && NumberUtils.isInsideCube(location.z)) {
                    xz1x1 = true;
                }

                if (NumberUtils.isInsideCube(location.y + (double) 0.49F)) {
                    y1x1 = true;
                }

                row.createCell(0).setCellValue(selection.tracker.spawnTick + (i - 1));
                row.createCell(1).setCellValue(typeString);
                row.createCell(2).setCellValue(location.x);
                row.createCell(3).setCellValue(location.y);
                row.createCell(4).setCellValue(location.z);
                row.createCell(5).setCellValue(velocity.x);
                row.createCell(6).setCellValue(velocity.y);
                row.createCell(7).setCellValue(velocity.z);
                row.createCell(8).setCellValue(xz1x1);
                row.createCell(9).setCellValue(y1x1);
            }

            XSSFDrawing drawing = spreadsheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 10, 0, 30, 40);
            XSSFChart chart = drawing.createChart(anchor);

            chart.setTitleText("Entity Velocity");

            XDDFChartLegend legend = chart.getOrAddLegend();
            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);

            bottomAxis.setTitle("Tick");

            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

            leftAxis.setTitle("Velocity");

            XDDFCategoryDataSource categoryDataSource = XDDFDataSourcesFactory.fromStringCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 0, 0)
            );
            XDDFNumericalDataSource<Double> xDataSource = XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 5, 5)
            );
            XDDFNumericalDataSource<Double> yDataSource = XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 6, 6)
            );
            XDDFNumericalDataSource<Double> zDataSource = XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 7, 7)
            );
            XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

            legend.setPosition(LegendPosition.BOTTOM);
            data.addSeries(
                categoryDataSource,
                xDataSource
            ).setTitle("X", null);
            data.addSeries(
                categoryDataSource,
                yDataSource
            ).setTitle("Y", null);
            data.addSeries(
                categoryDataSource,
                zDataSource
            ).setTitle("Z", null);
            chart.plot(data);
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
