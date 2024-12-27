package util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExperimentLogger {
    private static final String RESULTS_DIR = "experiment_results";
    private static Workbook workbook;
    private static Sheet currentSheet;
    private static Map<String, Integer> sheetRowNums = new HashMap<>();
    private static Set<String> headerKeys = new HashSet<>();
    
    public static void initializeExperiment(String experimentName) {
        workbook = new XSSFWorkbook();
        sheetRowNums.clear();
        headerKeys.clear();
    }
    
    public static void addSheet(String sheetName) {
        currentSheet = workbook.createSheet(sheetName);
        sheetRowNums.put(sheetName, 0);
    }
    
    public static void logResult(Map<String, Object> result) {
        if (currentSheet == null) {
            return;
        }
        
        String sheetName = currentSheet.getSheetName();
        int rowNum = sheetRowNums.get(sheetName);
        
        // 如果是新的sheet，创建表头
        if (rowNum == 0) {
            createHeader(result.keySet());
            rowNum++;
        }
        
        // 创建数据行
        Row dataRow = currentSheet.createRow(rowNum);
        int colNum = 0;
        
        // 按表头顺序填充数据
        for (String key : headerKeys) {
            Cell cell = dataRow.createCell(colNum++);
            Object value = result.get(key);
            if (value != null) {
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        }
        
        // 更新行号
        sheetRowNums.put(sheetName, rowNum + 1);
        
        // 自动调整列宽
        for (int i = 0; i < headerKeys.size(); i++) {
            currentSheet.autoSizeColumn(i);
        }
    }
    
    private static void createHeader(Set<String> keys) {
        Row headerRow = currentSheet.createRow(0);
        headerKeys.clear();
        headerKeys.addAll(keys);
        
        int colNum = 0;
        for (String key : headerKeys) {
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(key);
        }
    }
    
    public static void saveResults() {
        try {
            // 确保目录存在
            File dir = new File(RESULTS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成文件名
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("%s/experiment_results_%s.xlsx", RESULTS_DIR, timestamp);
            
            // 保存文件
            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }
            
            System.out.println("实验结果已保存到: " + filename);
            
        } catch (IOException e) {
            System.err.println("保存实验结果失败: " + e.getMessage());
        }
    }
} 