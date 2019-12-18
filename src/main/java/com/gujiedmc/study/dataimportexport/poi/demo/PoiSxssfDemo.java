package com.gujiedmc.study.dataimportexport.poi.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 大数据xlsx导入导出
 *
 * @author admin
 * @date 2019/12/12
 */
@RestController
@RequestMapping("/poi/sxssf")
public class PoiSxssfDemo {

    public static void main(String[] args) throws Exception {
        String path = "sxssf.xlsx";
        // 导出
        exportData(path);
        // 导入
        importData(path);
        // 压缩文件
        compressFile(path);
    }

    /**
     * 压缩文件
     */
    private static void compressFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(path);
        BufferedInputStream bis = new BufferedInputStream(fis);

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path + ".zip"));
        zos.putNextEntry(new ZipEntry(file.getName()));

        int len = 0;
        byte[] buffer = new byte[1024];

        while ((len = (bis.read(buffer,0,buffer.length))) != -1){
            zos.write(buffer,0,len);
        }
        fis.close();
        zos.close();
    }

    /**
     * 导入数据
     */
    private static void importData(String path) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        for (Sheet rows : workbook) {
            for (Row row : rows) {
                if (row.getRowNum() > 0){
                    StringBuffer sb = new StringBuffer();
//                    row.getCell(0).setCellType(CellType.STRING);
                    sb.append("用户ID：").append(new DecimalFormat("0").format(row.getCell(0).getNumericCellValue()));
                    sb.append(",用户邮箱：").append(row.getCell(1).getStringCellValue());
                    sb.append(",创建时间：").append(row.getCell(2).getDateCellValue());
                    System.out.println(sb.toString());
                }
            }
        }
        System.out.println("导入结束");
    }

    /**
     * 导出数据
     */
    private static void exportData(String path) throws IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("导出");

        SXSSFWorkbook workbook = getWorkbook();

        workbook.write(new FileOutputStream(path));
        stopWatch.stop();

        System.out.println("导出成功\n"+stopWatch.prettyPrint());
    }

    private static SXSSFWorkbook getWorkbook() {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("export");
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        CreationHelper creationHelper = workbook.getCreationHelper();

        SXSSFSheet sheet1 = workbook.createSheet("表格一");

        // 标题行
        SXSSFRow row0 = sheet1.createRow(0);
        SXSSFCell cell0 = row0.createCell(0, CellType.STRING);
        cell0.setCellValue("用户id");
        SXSSFCell cell1 = row0.createCell(1, CellType.STRING);
        cell1.setCellValue("用户邮箱");
        SXSSFCell cell2 = row0.createCell(2, CellType.STRING);
        cell2.setCellValue("创建时间");

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直对齐方式
        cellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平对齐方式
        // 内容
        for (int i = 0; i < 1000000; i++) {
            SXSSFRow row = sheet1.createRow(i + 1);

            SXSSFCell contentCell0 = row.createCell(0, CellType.STRING);
            contentCell0.setCellValue(System.currentTimeMillis());

            SXSSFCell contentCell1 = row.createCell(1, CellType.STRING);
            contentCell1.setCellValue((i+1)+"@qq.com");

            SXSSFCell contentCell2 = row.createCell(2,CellType.NUMERIC);
            contentCell2.setCellStyle(cellStyle);
            contentCell2.setCellValue(new Date());
        }
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        return workbook;
    }

    @GetMapping("/export/website")
    @ResponseBody
    public void exportWebsiteFile(HttpServletResponse response){

        SXSSFWorkbook workbook = getWorkbook();

        try (ServletOutputStream outputStream = response.getOutputStream()) {

            response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment;filename=" + new String("导出.xlsx".getBytes(), StandardCharsets.ISO_8859_1));
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/export/website/zip")
    @ResponseBody
    public void exportWebsiteFileZip(HttpServletResponse response){

        SXSSFWorkbook workbook = getWorkbook();

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment;filename=" + new String("导出.zip".getBytes(), StandardCharsets.ISO_8859_1));


            zos.putNextEntry(new ZipEntry("导出.xlsx"));

            workbook.write(zos);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
