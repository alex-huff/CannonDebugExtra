package dev.phonis.cannondebugextra.excel;

import dev.phonis.cannondebugextra.CannonDebugExtra;
import dev.phonis.cannondebugextra.event.ChatManager;
import dev.phonis.cannondebugextra.networking.*;
import dev.phonis.cannondebugextra.util.ImmutablePair;
import dev.phonis.cannondebugextra.util.NumberUtils;
import dev.phonis.cannondebugextra.util.Pair;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

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

    private static void logToPlayer(String message) {
        ChatManager.messageQueue.add(CannonDebugExtra.prefix + message);
        System.out.println(message);
    }

    public static void viewAsExcel(CDHistory history) {
        if (history.selections.size() == 0) {
            ExcelManager.logToPlayer("Cannot open spreadsheet! empty cannondebug history.");

            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFFormulaEvaluator formulaEvaluator = new XSSFFormulaEvaluator(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle xStyle = workbook.createCellStyle();
        CellStyle yStyle = workbook.createCellStyle();
        CellStyle zStyle = workbook.createCellStyle();
        CellStyle totalStyle = workbook.createCellStyle();
        CellStyle trueStyle = workbook.createCellStyle();
        CellStyle falseStyle = workbook.createCellStyle();
        CellStyle booleanStyle = workbook.createCellStyle();
        CellStyle tickStyle = workbook.createCellStyle();
        CellStyle linkTickStyle = workbook.createCellStyle();
        CellStyle linkBorderStyle = workbook.createCellStyle();
        CellStyle linkStyle = workbook.createCellStyle();
        BorderStyle borderStyle = BorderStyle.THIN;
        short xColor = IndexedColors.PALE_BLUE.getIndex();
        short yColor = IndexedColors.LIGHT_ORANGE.getIndex();
        short zColor = IndexedColors.GREY_25_PERCENT.getIndex();
        short totalColor = IndexedColors.LEMON_CHIFFON.getIndex();
        short borderColor = IndexedColors.BLACK.getIndex();
        short trueColor = IndexedColors.LIGHT_GREEN.getIndex();
        short falseColor = IndexedColors.CORAL.getIndex();
        short booleanColor = IndexedColors.GREY_40_PERCENT.getIndex();
        short tickColor = IndexedColors.ROSE.getIndex();
        short linkBorderColor = IndexedColors.GREY_80_PERCENT.getIndex();
        short linkColor = IndexedColors.GREY_25_PERCENT.getIndex();
        Font underlineFont = workbook.createFont();

        underlineFont.setUnderline(XSSFFont.U_SINGLE);
        underlineFont.setColor(IndexedColors.BLUE.getIndex());
        ExcelManager.setStyle(xStyle, borderStyle, xColor, borderColor);
        ExcelManager.setStyle(yStyle, borderStyle, yColor, borderColor);
        ExcelManager.setStyle(zStyle, borderStyle, zColor, borderColor);
        ExcelManager.setStyle(totalStyle, borderStyle, totalColor, borderColor);
        ExcelManager.setStyle(trueStyle, borderStyle, trueColor, borderColor);
        ExcelManager.setStyle(falseStyle, borderStyle, falseColor, borderColor);
        ExcelManager.setStyle(booleanStyle, borderStyle, booleanColor, borderColor);
        ExcelManager.setStyle(tickStyle, borderStyle, tickColor, borderColor);
        ExcelManager.setStyle(linkTickStyle, BorderStyle.NONE, tickColor, borderColor);
        ExcelManager.setStyle(linkBorderStyle, BorderStyle.NONE, linkBorderColor, borderColor);
        ExcelManager.setStyle(linkStyle, borderStyle, linkColor, borderColor);
        linkStyle.setFont(underlineFont);

        if (history.byOrder) history.selections.sort(Comparator.comparingLong((CDBlockSelection o) -> o.tracker.spawnTick).thenComparingInt(blockSelection -> blockSelection.order));

        List<Pair<String, Hyperlink>> hyperLinks = history.selections.parallelStream().map(
            selection -> {
                Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
                String refLink =  history.byOrder ? ("Tick" + selection.tracker.spawnTick + " OOE" + selection.order) : Integer.toString(selection.id);
                String refName = history.byOrder ? ("OOE" + selection.order) : ("ID" + selection.id);

                hyperlink.setAddress("'" + refLink + "'!A1");

                return new ImmutablePair<>(refName, hyperlink);
            }
        ).collect(Collectors.toList());

        ExcelManager.logToPlayer("Starting conversion... 0%");

        int p = 0;

        for (int s = 0; s < history.selections.size(); s++) {
            CDBlockSelection selection = history.selections.get(s);
            int progress = (int) ((s / (double) history.selections.size()) * 100);

            if (progress > (p + 10)) {
                p = progress - (progress % 10);

                ExcelManager.logToPlayer(p + "%");
            }

            XSSFSheet spreadsheet = workbook.createSheet(history.byOrder ? ("Tick" + selection.tracker.spawnTick + " OOE" + selection.order) : Integer.toString(selection.id));
            XSSFRow startRow = spreadsheet.createRow(0);

            startRow.createCell(0).setCellValue("Entity Type");
            ExcelManager.createStyledCell(startRow, 1, "Tick", tickStyle);
            ExcelManager.createStyledCell(startRow, 2, "X", xStyle);
            ExcelManager.createStyledCell(startRow, 3, "Y", yStyle);
            ExcelManager.createStyledCell(startRow, 4, "Z", zStyle);
            ExcelManager.createStyledCell(startRow, 5, "X Velocity", xStyle);
            ExcelManager.createStyledCell(startRow, 6, "Y Velocity", yStyle);
            ExcelManager.createStyledCell(startRow, 7, "Z Velocity", zStyle);
            ExcelManager.createStyledCell(startRow, 8, "Total Velocity", totalStyle);
            ExcelManager.createStyledCell(startRow, 9, "XZ 1x1", booleanStyle);
            ExcelManager.createStyledCell(startRow, 10, "Y 1x1", booleanStyle);

            for (int i = 1; i < selection.tracker.locationHistory.size() + 1; i++) {
                CDLocation location = selection.tracker.locationHistory.get(i - 1);
                CDVec3D velocity = selection.tracker.velocityHistory.get(i - 1);
                XSSFRow row = spreadsheet.createRow(i);
                boolean xz1x1 = false;
                boolean y1x1 = false;
                String typeString = ExcelManager.getStringFromEntityType(selection.tracker.entityType);

                if (NumberUtils.isInsideCube(location.x) && NumberUtils.isInsideCube(location.z) ||
                    Math.abs(velocity.x) != 0.0 && NumberUtils.isInsideCube(location.x) ||
                    Math.abs(velocity.z) != 0.0 && NumberUtils.isInsideCube(location.z)) {
                    xz1x1 = true;
                }

                if (NumberUtils.isInsideCube(location.y + (double) 0.49F)) {
                    y1x1 = true;
                }

                if (i == 1) row.createCell(0).setCellValue(typeString);
                ExcelManager.createStyledCell(row, 1, selection.tracker.spawnTick + (i - 1), tickStyle);
                ExcelManager.createStyledCell(row, 2, location.x, xStyle);
                ExcelManager.createStyledCell(row, 3, location.y, yStyle);
                ExcelManager.createStyledCell(row, 4, location.z, zStyle);
                ExcelManager.createStyledCell(row, 5, velocity.x, xStyle);
                ExcelManager.createStyledCell(row, 6, velocity.y, yStyle);
                ExcelManager.createStyledCell(row, 7, velocity.z, zStyle);
                formulaEvaluator.evaluate(
                    ExcelManager.createStyledFormulaCell(
                        row,
                        8,
                        "SQRT(POWER(F" + (i + 1) + ", 2)+POWER(G" + (i + 1) + ", 2)+POWER(H" + (i + 1) + ", 2))",
                        totalStyle
                    )
                );
                ExcelManager.createStyledCell(row, 9, xz1x1, xz1x1 ? trueStyle : falseStyle);
                ExcelManager.createStyledCell(row, 10, y1x1, y1x1 ? trueStyle : falseStyle);
            }

            ExcelManager.createGraph(spreadsheet, selection);
            formulaEvaluator.clearAllCachedResultValues();

            if (history.byOrder)
                ExcelManager.addOrderedLinks(history, spreadsheet, hyperLinks, linkStyle, linkTickStyle, linkBorderStyle);
            else
                ExcelManager.addLinks(history, spreadsheet, hyperLinks, linkStyle, linkBorderStyle);
        }

        ExcelManager.logToPlayer("Finished conversion... 100%");
        ExcelManager.logToPlayer("Writing to file...");
        ExcelManager.writeToFile(workbook);
    }

    private static void writeToFile(XSSFWorkbook workbook) {
        try {
            File excelFile = new File(ExcelManager.excelFolder, "history" + UUID.randomUUID().toString().replace("-", "") + ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(excelFile);

            workbook.write(fileOut);
            fileOut.close();

            ExcelManager.logToPlayer("Done. Opening file.");
            Desktop.getDesktop().open(excelFile);
        } catch (IOException e) {
            ExcelManager.logToPlayer("Failed to write to file!");

            e.printStackTrace();
        }
    }

    private static void createGraph(XSSFSheet spreadsheet, CDBlockSelection selection) {
        XSSFDrawing drawing = spreadsheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 11, 0, 28, 20);
        XSSFChart chart = drawing.createChart(anchor);

        chart.setTitleText("Entity Velocity");

        XDDFChartLegend legend = chart.getOrAddLegend();
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);

        bottomAxis.setTitle("Tick");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

        leftAxis.setTitle("Velocity");

        XDDFCategoryDataSource tickDataSource = XDDFDataSourcesFactory.fromStringCellRange(
            spreadsheet,
            new CellRangeAddress(1, selection.tracker.locationHistory.size(), 1, 1)
        );
        XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        legend.setPosition(LegendPosition.BOTTOM);
        data.addSeries(
            tickDataSource,
            XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 5, 5)
            )
        ).setTitle("X", null);
        data.addSeries(
            tickDataSource,
            XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 6, 6)
            )
        ).setTitle("Y", null);
        data.addSeries(
            tickDataSource,
            XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 7, 7)
            )
        ).setTitle("Z", null);
        data.addSeries(
            tickDataSource,
            XDDFDataSourcesFactory.fromNumericCellRange(
                spreadsheet,
                new CellRangeAddress(1, selection.tracker.locationHistory.size(), 8, 8)
            )
        ).setTitle("Total", null);
        chart.plot(data);
    }

    private static String getStringFromEntityType(CDEntityType entityType) {
        String typeString;

        switch (entityType) {
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

        return typeString;
    }

    private static XSSFCell createStyledFormulaCell(XSSFRow row, int index, String value, CellStyle style) {
        XSSFCell cell = row.createCell(index);

        cell.setCellFormula(value);
        cell.setCellStyle(style);

        return cell;
    }

    private static XSSFCell createStyledCell(XSSFRow row, int index, CellStyle style) {
        XSSFCell cell = row.createCell(index);

        cell.setCellStyle(style);

        return cell;
    }

    private static XSSFCell createStyledCell(XSSFRow row, int index, long value, CellStyle style) {
        XSSFCell cell = row.createCell(index);

        cell.setCellValue(value);
        cell.setCellStyle(style);

        return cell;
    }

    private static XSSFCell createStyledCell(XSSFRow row, int index, Boolean value, CellStyle style) {
        XSSFCell cell = row.createCell(index);

        cell.setCellValue(value);
        cell.setCellStyle(style);

        return cell;
    }

    private static XSSFCell createStyledCell(XSSFRow row, int index, String value, CellStyle style) {
        XSSFCell cell = row.createCell(index);

        cell.setCellValue(value);
        cell.setCellStyle(style);

        return cell;
    }

    private static XSSFCell createStyledCell(XSSFRow row, int index, Double value, CellStyle style) {
        XSSFCell cell = row.createCell(index);

        cell.setCellValue(value);
        cell.setCellStyle(style);

        return cell;
    }

    private static void setStyle(CellStyle style, BorderStyle borderStyle, short color, short borderColor) {
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(color);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setBorderTop(borderStyle);
        style.setLeftBorderColor(borderColor);
        style.setRightBorderColor(borderColor);
        style.setBottomBorderColor(borderColor);
        style.setTopBorderColor(borderColor);
    }

    private static XSSFRow getOrCreateRow(XSSFSheet spreadsheet, int r) {
        XSSFRow row = spreadsheet.getRow(r);

        return (row != null) ? row : spreadsheet.createRow(r);
    }

    private static XSSFCell createLinkCell(XSSFRow row, int index, Pair<String, Hyperlink> linkRef, CellStyle linkStyle) {
        return ExcelManager.createLinkCell(row, index, linkRef.getLeft(), linkRef.getRight(), linkStyle);
    }

    private static XSSFCell createLinkCell(XSSFRow row, int index, String name, Hyperlink link, CellStyle linkStyle) {
        XSSFCell cell = row.createCell(index);

        cell.setCellStyle(linkStyle);
        cell.setCellValue(name);
        cell.setHyperlink(link);

        return cell;
    }

    private static void addOrderedLinks(CDHistory history, XSSFSheet spreadsheet, List<Pair<String, Hyperlink>> hyperLinks, CellStyle linkStyle, CellStyle tickStyle, CellStyle linkBorderStyle) {
        final int cStart = 12;
        int r = 20, c = cStart;
        final int colSpan = 15;
        final int size = history.selections.size();
        long currentTick = history.selections.get(0).tracker.spawnTick;

        XSSFRow row = ExcelManager.getOrCreateRow(spreadsheet, r);

        ExcelManager.fillOrderedLinkSpacer(row, cStart, colSpan, currentTick, tickStyle, linkBorderStyle);

        r++;
        row = ExcelManager.getOrCreateRow(spreadsheet, r);

        ExcelManager.createStyledCell(row, cStart - 1, linkBorderStyle);

        for (int i = 0; i < size; i++) {
            CDBlockSelection refSelection = history.selections.get(i);

            if (refSelection.tracker.spawnTick != currentTick) {
                ExcelManager.fillLinkBorderRange(row, c, cStart + colSpan + 1, linkBorderStyle);

                currentTick = refSelection.tracker.spawnTick;
                c = cStart;
                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);

                ExcelManager.fillOrderedLinkSpacer(row, cStart, colSpan, currentTick, tickStyle, linkBorderStyle);

                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);

                ExcelManager.createStyledCell(row, cStart - 1, linkBorderStyle);
            } else if (c == (cStart + colSpan)) {
                ExcelManager.createStyledCell(row, c, linkBorderStyle);

                c = cStart;
                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);

                ExcelManager.createStyledCell(row, cStart - 1, linkBorderStyle);
            }

            ExcelManager.createLinkCell(row, c, hyperLinks.get(i), linkStyle);

            c++;
        }

        ExcelManager.fillLinkBorderRange(row, c, cStart + colSpan + 1, linkBorderStyle);

        r++;
        row = ExcelManager.getOrCreateRow(spreadsheet, r);

        ExcelManager.fillLinkBorderSpan(row, cStart - 1, colSpan + 2, linkBorderStyle);
    }

    private static void addLinks(CDHistory history, XSSFSheet spreadsheet, List<Pair<String, Hyperlink>> hyperLinks, CellStyle linkStyle, CellStyle linkBorderStyle) {
        final int cStart = 12;
        int r = 20, c = cStart;
        final int colSpan = 15;
        final int size = history.selections.size();

        XSSFRow row = ExcelManager.getOrCreateRow(spreadsheet, r);

        ExcelManager.fillLinkBorderSpan(row, cStart - 1, 1, linkBorderStyle);
        ExcelManager.createStyledCell(row, cStart, "Tip: '/c e ooe' will order tracked entities by OOE", linkBorderStyle);
        ExcelManager.fillLinkBorderSpan(row, cStart + 1, colSpan, linkBorderStyle);

        r++;
        row = ExcelManager.getOrCreateRow(spreadsheet, r);

        ExcelManager.createStyledCell(row, cStart - 1, linkBorderStyle);

        for (int i = 0; i < size; i++) {
            if (c == (cStart + colSpan)) {
                ExcelManager.createStyledCell(row, c, linkBorderStyle);

                c = cStart;
                r++;
                row = ExcelManager.getOrCreateRow(spreadsheet, r);

                ExcelManager.createStyledCell(row, cStart - 1, linkBorderStyle);
            }

            ExcelManager.createLinkCell(row, c, hyperLinks.get(i), linkStyle);

            c++;
        }

        ExcelManager.fillLinkBorderRange(row, c, cStart + colSpan + 1, linkBorderStyle);

        r++;
        row = ExcelManager.getOrCreateRow(spreadsheet, r);

        ExcelManager.fillLinkBorderSpan(row, cStart - 1, colSpan + 2, linkBorderStyle);
    }

    private static void fillOrderedLinkSpacer(XSSFRow row, int cStart, int colSpan, long currentTick, CellStyle tickStyle, CellStyle linkBorderStyle) {
        ExcelManager.fillLinkBorderSpan(row, cStart - 1, 1, linkBorderStyle);
        ExcelManager.createStyledCell(row, cStart, "Tick " + currentTick, tickStyle);
        ExcelManager.fillLinkBorderSpan(row, cStart + 1, colSpan, linkBorderStyle);
    }

    private static void fillLinkBorderRange(XSSFRow row, int start, int end, CellStyle linkBorderStyle) {
        for (int i = start; i < end; i++) {
            ExcelManager.createStyledCell(row, i, linkBorderStyle);
        }
    }

    private static void fillLinkBorderSpan(XSSFRow row, int start, int span, CellStyle linkBorderStyle) {
        for (int i = start; i < start + span; i++) {
            ExcelManager.createStyledCell(row, i, linkBorderStyle);
        }
    }

}
