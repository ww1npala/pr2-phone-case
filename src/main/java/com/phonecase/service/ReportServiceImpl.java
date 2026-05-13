package com.phonecase.service;

import com.phonecase.dto.DesignDTO;
import com.phonecase.dto.OrderDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Реалізація сервісу звітності через Apache POI.
 * Генерує Excel (.xlsx) звіти.
 */
@Singleton
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final DesignService designService;
    private final OrderService orderService;

    @Inject
    public ReportServiceImpl(DesignService designService, OrderService orderService) {
        this.designService = designService;
        this.orderService = orderService;
    }

    @Override
    public void exportDesignsToExcel(File outputFile) {
        List<DesignDTO> designs = designService.getAllDesigns();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Дизайни");

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);


            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Звіт: Дизайни чохлів для мобільних телефонів");
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));


            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Дата: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));


            Row headerRow = sheet.createRow(3);
            String[] headers = {"ID", "Назва", "Категорія", "Ціна (грн)", "Доступний", "Дата створення"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }


            CellStyle altStyle = wb.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 4;
            for (DesignDTO d : designs) {
                Row row = sheet.createRow(rowNum++);
                if (rowNum % 2 == 0) {
                    for (int i = 0; i < 6; i++) row.createCell(i).setCellStyle(altStyle);
                }
                row.createCell(0).setCellValue(d.getId());
                row.createCell(1).setCellValue(d.getName());
                row.createCell(2).setCellValue(d.getCategoryName() != null ? d.getCategoryName() : "");
                row.createCell(3).setCellValue(d.getPrice());
                row.createCell(4).setCellValue(d.isAvailable() ? "Так" : "Ні");
                row.createCell(5).setCellValue("");
            }


            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                wb.write(fos);
            }
            logger.info("Excel-звіт з дизайнами збережено: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Помилка генерації Excel-звіту: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалось згенерувати звіт", e);
        }
    }

    @Override
    public void exportOrdersToExcel(File outputFile) {
        List<OrderDTO> orders = orderService.getAllOrders();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Замовлення");

            CellStyle headerStyle = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            headerStyle.setFont(f);
            headerStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Звіт: Замовлення");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row headerRow = sheet.createRow(2);
            String[] headers = {"ID", "Клієнт", "Дизайн", "Модель телефону", "К-сть", "Сума (грн)", "Статус"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 3;
            double total = 0;
            for (OrderDTO o : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(o.getId());
                row.createCell(1).setCellValue(o.getUsername() != null ? o.getUsername() : "");
                row.createCell(2).setCellValue(o.getDesignName() != null ? o.getDesignName() : "");
                row.createCell(3).setCellValue(o.getPhoneModelName() != null ? o.getPhoneModelName() : "");
                row.createCell(4).setCellValue(o.getQuantity());
                row.createCell(5).setCellValue(o.getTotalPrice());
                row.createCell(6).setCellValue(o.getStatus() != null ? o.getStatus() : "");
                total += o.getTotalPrice();
            }


            Row totalRow = sheet.createRow(rowNum + 1);
            totalRow.createCell(4).setCellValue("Загальна сума:");
            Cell totalCell = totalRow.createCell(5);
            totalCell.setCellValue(total);
            CellStyle boldStyle = wb.createCellStyle();
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            totalCell.setCellStyle(boldStyle);

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                wb.write(fos);
            }
            logger.info("Excel-звіт із замовленнями збережено: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Помилка генерації Excel-звіту: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалось згенерувати звіт", e);
        }
    }
}