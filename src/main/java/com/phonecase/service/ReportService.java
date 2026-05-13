package com.phonecase.service;

import java.io.File;

/** Інтерфейс сервісу звітності. */
public interface ReportService {
    void exportDesignsToExcel(File outputFile);
    void exportOrdersToExcel(File outputFile);
}