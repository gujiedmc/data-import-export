package com.gujiedmc.study.dataimportexport.poi.demo;

import com.gujiedmc.study.dataimportexport.common.HttpDownloadUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * xlsx少量数据导入导出
 *
 * @author admin
 * @date 2019/12/12
 */
@RequestMapping("/poi/xssf")
@Controller
public class PoiXssfDemo {

    /**
     * 本地导入数据
     */
    @GetMapping("/import/local")
    public void importLocal(@RequestParam(defaultValue = "xssf.xlsx") String path) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(new File(path));
            this.readWorkbook(workbook);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传导入excel
     */
    @PostMapping("/import/website")
    public void importWebsite(@RequestBody MultipartFile file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            this.readWorkbook(workbook);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传导入excel
     */
    @PostMapping("/import/website/zip")
    public void importWebsiteZip(@RequestBody MultipartFile file) {
        try {
            ZipInputStream zis = new ZipInputStream(file.getInputStream());
            ZipEntry zipEntry = null;
            while ((zipEntry = zis.getNextEntry()) != null){
                String name = zipEntry.getName();
                if (!zipEntry.isDirectory()){

                    byte[] buffer = new byte[1024];
                    int len = 0;

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while ((len = zis.read(buffer,0,buffer.length)) != -1){
                        bos.write(buffer,0,len);
                    }

                    XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bos.toByteArray()));
                    this.readWorkbook(workbook);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出数据到本地，并压缩
     *
     * @param path 导出地址
     */
    @GetMapping("/export/local")
    public void exportLocal(@RequestParam(defaultValue = "xssf.xlsx") String path) {

        XSSFWorkbook workbook = getWorkbook();
        try (FileOutputStream fos = new FileOutputStream(path)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出数据到本地，并压缩
     */
    @GetMapping("/export/local/zip")
    public void exportLocalZip(@RequestParam(defaultValue = "xssf.xlsx") String path) throws IOException {

        XSSFWorkbook workbook = getWorkbook();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);

        // 压缩，不能直接通过workbook.write到ZipOutputStream流中，会出现Stream closed异常
        File file = new File(path);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file + ".zip"));
        zos.putNextEntry(new ZipEntry(file.getName()));
        bos.writeTo(zos);
        zos.close();

        System.out.println("导出成功");
    }

    /**
     * 导出文件，直接下载
     */
    @GetMapping("/export/website")
    @ResponseBody
    public void exportWebsiteFile(HttpServletResponse response) {
        // 获取表格
        XSSFWorkbook workbook = getWorkbook();
        // 获取http响应流
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            // 设置响应头
            HttpDownloadUtils.setDownloadHeader(response, "导出.xlsx");
            // 导出
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出文件，直接下载，并压缩
     */
    @GetMapping("/export/website/zip")
    @ResponseBody
    public void exportWebsiteFileZip(HttpServletResponse response) {
        // 获取表格
        XSSFWorkbook workbook = getWorkbook();
        // 获取http响应流，并封装响应流为压缩流
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            // 添加压缩包
            zos.putNextEntry(new ZipEntry("导出.xlsx"));
            // 设置响应头
            HttpDownloadUtils.setDownloadHeader(response, "导出.zip");
            // 导出
            workbook.write(zos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成表格数据，xssf
     */
    private XSSFWorkbook getWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCreationHelper creationHelper = workbook.getCreationHelper();

        XSSFSheet sheet1 = workbook.createSheet("表格一");

        // 标题行
        XSSFRow row0 = sheet1.createRow(0);
        XSSFCell cell0 = row0.createCell(0, CellType.STRING);
        cell0.setCellValue("用户id");
        XSSFCell cell1 = row0.createCell(1, CellType.STRING);
        cell1.setCellValue("用户邮箱");
        XSSFCell cell2 = row0.createCell(2, CellType.STRING);
        cell2.setCellValue("创建时间");

        // 内容
        for (int i = 0; i < 3; i++) {
            XSSFRow row = sheet1.createRow(i + 1);

            XSSFCell contentCell0 = row.createCell(0, CellType.STRING);
            contentCell0.setCellValue(System.currentTimeMillis());

            XSSFCell contentCell1 = row.createCell(1, CellType.STRING);
            contentCell1.setCellValue((i + 1) + "@qq.com");

            XSSFCell contentCell2 = row.createCell(2, CellType.NUMERIC);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直对齐方式
            cellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平对齐方式
            contentCell2.setCellStyle(cellStyle);
            contentCell2.setCellValue(new Date());
        }
        return workbook;
    }

    /**
     * 读取表格数据
     */
    private void readWorkbook(XSSFWorkbook workbook) {
        for (Sheet rows : workbook) {
            for (Row row : rows) {
                if (row.getRowNum() > 0) {
                    StringBuffer sb = new StringBuffer();
//                    row.getCell(0).setCellType(CellType.STRING);
                    sb.append("用户ID：").append(new DecimalFormat("0").format(row.getCell(0).getNumericCellValue()));
                    sb.append(",用户邮箱：").append(row.getCell(1).getStringCellValue());
                    sb.append(",创建时间：").append(row.getCell(2).getDateCellValue());
                    System.out.println(sb.toString());
                }
            }
        }
    }
}
