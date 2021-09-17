package dev.phonis.cannondebugextra.excel;

import dev.phonis.cannondebugextra.event.ChatManager;
import dev.phonis.cannondebugextra.networking.*;
import dev.phonis.cannondebugextra.util.ImmutablePair;
import dev.phonis.cannondebugextra.util.NumberUtils;
import dev.phonis.cannondebugextra.util.Pair;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
        if (history.selections.size() == 0) {
            ChatManager.messageQueue.add("Cannot open spreadsheet! empty cannondebug history.");

            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFFormulaEvaluator formulaEvaluator = new XSSFFormulaEvaluator(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();

        if (history.byOrder) history.selections.sort(Comparator.comparingLong((CDBlockSelection o) -> o.tracker.spawnTick).thenComparingInt(blockSelection -> blockSelection.order));

        List<Pair<String, Hyperlink>> hyperLinks = new ArrayList<>(history.selections.size());

        for (int h = 0; h < history.selections.size(); h++) {
            CDBlockSelection selection = history.selections.get(h);
            Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            String refLink =  history.byOrder ? ("Tick" + selection.tracker.spawnTick + " OOE" + selection.order) : Integer.toString(selection.id);
            String refName = history.byOrder ? ("OOE" + selection.order) : ("ID" + selection.id);

            hyperlink.setAddress("'" + refLink + "'!A1");
            hyperLinks.add(new ImmutablePair<>(refName, hyperlink));
        }

        ChatManager.messageQueue.add("Starting conversion... 0%");

        int s = 0;
        int p = 0;

        for (CDBlockSelection selection : history.selections) {
            int progress = (int) ((s / (double) history.selections.size()) * 100);

            if (progress > (p + 10)) {
                p = progress - (progress % 10);

                ChatManager.messageQueue.add(p + "%");
            }

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
            startRow.createCell(8).setCellValue("Total Velocity");
            startRow.createCell(9).setCellValue("XZ 1x1");
            startRow.createCell(10).setCellValue("Y 1x1");

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

                XSSFCell formulaCell = row.createCell(8);

                formulaCell.setCellFormula("SQRT(POWER(F" + (i + 1) + ", 2)+POWER(G" + (i + 1) + ", 2)+POWER(H" + (i + 1) + ", 2))");
                formulaEvaluator.evaluate(formulaCell);
                row.createCell(9).setCellValue(xz1x1);
                row.createCell(10).setCellValue(y1x1);
            }

            XSSFDrawing drawing = spreadsheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 11, 0, 27, 20);
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
            XDDFNumericalDataSource<Double> totalDataSource = XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 8, 8)
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
            data.addSeries(
                categoryDataSource,
                totalDataSource
            ).setTitle("Total", null);
            chart.plot(data);
            formulaEvaluator.clearAllCachedResultValues();

            if (history.byOrder)
                ExcelManager.addOrderedLinks(history, spreadsheet, hyperLinks);
            else
                ExcelManager.addLinks(history, spreadsheet, hyperLinks);

            s += 1;
        }

        ChatManager.messageQueue.add("Finished conversion... 100%");
        ChatManager.messageQueue.add("Writing to file...");

        try {
            File excelFile = new File(ExcelManager.excelFolder, "history" + UUID.randomUUID().toString().replace("-", "") + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(excelFile);

            workbook.write(fileOut);
            fileOut.close();

            ChatManager.messageQueue.add("Done. Opening file.");
            Desktop.getDesktop().open(excelFile);
        } catch (IOException e) {
            ChatManager.messageQueue.add("Failed to write to file!");

            e.printStackTrace();
        }
    }

    private static XSSFRow getOrCreateRow(XSSFSheet spreadsheet, int r) {
        XSSFRow row = spreadsheet.getRow(r);

        return (row != null) ? row : spreadsheet.createRow(r);
    }

    private static void addOrderedLinks(CDHistory history, XSSFSheet spreadsheet, List<Pair<String, Hyperlink>> hyperLinks) {
        int cStart = 12;
        int r = 21, c = cStart;
        int colSpan = 15;
        int size = history.selections.size();
        long currentTick = history.selections.get(0).tracker.spawnTick;

        XSSFRow row = ExcelManager.getOrCreateRow(spreadsheet, r);
        XSSFCell tickCell = row.createCell(c);

        tickCell.setCellValue("TICK " + currentTick);

        r++;
        row = ExcelManager.getOrCreateRow(spreadsheet, r);

        for (int i = 0; i < size; i++) {
            CDBlockSelection refSelection = history.selections.get(i);

            if (refSelection.tracker.spawnTick != currentTick) {
                currentTick = refSelection.tracker.spawnTick;
                c = cStart;
                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);
                tickCell = row.createCell(c);

                tickCell.setCellValue("Tick " + currentTick);

                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);
            } else if (c == (cStart + colSpan)) {
                c = cStart;
                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);
            }

            XSSFCell refCell = row.createCell(c);
            Pair<String, Hyperlink> ref = hyperLinks.get(i);

            refCell.setCellValue(ref.getLeft());
            refCell.setHyperlink(ref.getRight());

            c++;
        }
    }

    private static void addLinks(CDHistory history, XSSFSheet spreadsheet, List<Pair<String, Hyperlink>> hyperLinks) {
        int cStart = 12;
        int r = 21, c = cStart;
        int colSpan = 15;
        int size = history.selections.size();

        XSSFRow row = ExcelManager.getOrCreateRow(spreadsheet, r);
        XSSFCell tickCell = row.createCell(c);

        tickCell.setCellValue("Tip: '/c e ooe' will order tracked entities by OOE");

        r++;
        row = ExcelManager.getOrCreateRow(spreadsheet, r);

        for (int i = 0; i < size; i++) {
            if (c == (cStart + colSpan)) {
                c = cStart;
                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);
            }

            XSSFCell refCell = row.createCell(c);
            Pair<String, Hyperlink> ref = hyperLinks.get(i);

            refCell.setCellValue(ref.getLeft());
            refCell.setHyperlink(ref.getRight());

            c++;
        }
    }

}
