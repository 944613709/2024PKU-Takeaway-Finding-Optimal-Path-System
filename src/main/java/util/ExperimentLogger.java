package util;

import model.Solution;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExperimentLogger {
    private static final String RESULTS_DIR = "experiment_results";
    private static Workbook workbook;
    private static Sheet currentSheet;
    private static int rowNum = 0;
    
    public static void initializeExperiment(String algorithmName) {
        workbook = new XSSFWorkbook();
        currentSheet = workbook.createSheet(algorithmName);
        rowNum = 0;
        
        // 创建表头
        Row headerRow = currentSheet.createRow(rowNum++);
        String[] headers = {
            "实验时间", "算法类型", "问题规模(顾客数)", "路径数/顾客",
            "总配送成本", "总配送时间(分钟)", "是否可行解", "求解时间(ms)",
            "数据生成策略", "时间约束"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }
    
    public static void logExperimentResult(
            String algorithmName,
            int customerCount,
            int pathsPerCustomer,
            Solution solution,
            long solvingTime,
            String dataStrategy,
            double timeConstraint) {
            
        Row row = currentSheet.createRow(rowNum++);
        int colNum = 0;
        
        // 实验时间
        row.createCell(colNum++).setCellValue(
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
        );
        
        // 算法类型
        row.createCell(colNum++).setCellValue(algorithmName);
        
        // 问题规模
        row.createCell(colNum++).setCellValue(customerCount);
        
        // 每个顾客的路径数
        row.createCell(colNum++).setCellValue(pathsPerCustomer);
        
        // 解的信息
        if (solution != null) {
            row.createCell(colNum++).setCellValue(solution.getTotalCost());
            row.createCell(colNum++).setCellValue(solution.getTotalTime());
            row.createCell(colNum++).setCellValue(solution.getTotalTime() <= timeConstraint ? "是" : "否");
        } else {
            row.createCell(colNum++).setCellValue("N/A");
            row.createCell(colNum++).setCellValue("N/A");
            row.createCell(colNum++).setCellValue("否");
        }
        
        // 求解时间
        row.createCell(colNum++).setCellValue(solvingTime);
        
        // 数据生成策略
        row.createCell(colNum++).setCellValue(dataStrategy);
        
        // 时间约束
        row.createCell(colNum++).setCellValue(timeConstraint);
        
        // 自动调整列宽
        for (int i = 0; i < colNum; i++) {
            currentSheet.autoSizeColumn(i);
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