package com.gujiedmc.study.dataimportexport.poi.pageexport;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * poi工具
 *
 * @author admin
 * @date 2019/12/16
 */
@Slf4j
public class PoiUtil {

    /**
     * 每个sheet存储的记录数
     */
    public static final Integer PER_SHEET_ROW_COUNT = 50000;

    /**
     * 每次向EXCEL写入的记录数(查询每页数据大小)。
     * 这里为逻辑简单，需要将{@link #PER_SHEET_ROW_COUNT}设置为每次写入数量的整数倍，避免一次分页在两个sheet表
     */
    public static final Integer PER_WRITE_ROW_COUNT = 10000;

    /**
     * 每个sheet的写入次数
     */
    public static final Integer PER_SHEET_WRITE_COUNT = PER_SHEET_ROW_COUNT / PER_WRITE_ROW_COUNT;

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";


    /**
     * 将日期转换为字符串
     *
     * @param date   DATE日期
     * @param format 转换格式
     * @return 字符串日期
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    /**
     * 初始化EXCEL(sheet个数和标题)
     *
     * @param totalRowCount 总记录数
     * @param titles        标题集合
     * @return XSSFWorkbook对象
     */
    public static SXSSFWorkbook initExcel(Integer totalRowCount, String[] titles) {

        // 在内存当中保持 100 行 , 超过的数据放到硬盘中在内存当中保持 100 行 , 超过的数据放到硬盘中
        SXSSFWorkbook wb = new SXSSFWorkbook(100);

        Integer sheetCount = ((totalRowCount % PER_SHEET_ROW_COUNT == 0) ?
                (totalRowCount / PER_SHEET_ROW_COUNT) : (totalRowCount / PER_SHEET_ROW_COUNT + 1));

        // 根据总记录数创建sheet并分配标题
        for (int i = 0; i < sheetCount; i++) {
            SXSSFSheet sheet = wb.createSheet("sheet" + (i + 1));
            SXSSFRow headRow = sheet.createRow(0);

            for (int j = 0; j < titles.length; j++) {
                SXSSFCell headRowCell = headRow.createCell(j);
                headRowCell.setCellValue(titles[j]);
            }
        }

        return wb;
    }


    /**
     * 下载EXCEL到本地指定的文件夹
     *
     * @param wb         EXCEL对象SXSSFWorkbook
     * @param exportPath 导出路径
     */
    public static void downLoadExcelToLocalPath(SXSSFWorkbook wb, String exportPath) {
        FileOutputStream fops = null;
        try {
            fops = new FileOutputStream(exportPath);
            wb.write(fops);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != wb) {
                try {
                    wb.dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != fops) {
                try {
                    fops.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 下载EXCEL到浏览器
     *
     * @param wb       EXCEL对象XSSFWorkbook
     * @param response
     * @param fileName 文件名称
     * @throws IOException
     */
    public static void downLoadExcelToWebsite(SXSSFWorkbook wb, HttpServletResponse response, String fileName) throws IOException {

        response.setHeader("Content-disposition", "attachment; filename="
                + new String((fileName + ".xlsx").getBytes("utf-8"), "ISO8859-1"));//设置下载的文件名

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            wb.write(outputStream);
        } finally {
            if (null != wb) {
                try {
                    wb.dispose();
                } catch (Exception e) {
                    log.error("文件导出异常",e);
                }
            }
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.error("文件导出异常",e);
                }
            }
        }
    }


    /**
     * 导出Excel到本地指定路径
     *
     * @param totalRowCount           总记录数
     * @param titles                  标题
     * @param exportPath              导出路径
     * @param writeExcelDataDelegated 向EXCEL写数据/处理格式的委托类 自行实现
     * @throws Exception
     */
    public static final void exportExcelToLocalPath(Integer totalRowCount, String[] titles, String exportPath, WriteExcelDataDelegated writeExcelDataDelegated) throws Exception {

        log.info("开始导出：" + formatDate(new Date(), YYYY_MM_DD_HH_MM_SS));

        // 初始化EXCEL
        SXSSFWorkbook wb = PoiUtil.initExcel(totalRowCount, titles);

        // 调用委托类分批写数据
        int sheetCount = wb.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            SXSSFSheet eachSheet = wb.getSheetAt(i);

            for (int j = 1; j <= PER_SHEET_WRITE_COUNT; j++) {

                int currentPage = i * PER_SHEET_WRITE_COUNT + j;
                int pageSize = PER_WRITE_ROW_COUNT;
                int startRowCount = (j - 1) * PER_WRITE_ROW_COUNT + 1;
                int endRowCount = startRowCount + pageSize - 1;


                writeExcelDataDelegated.writeExcelData(wb, eachSheet, startRowCount, endRowCount, currentPage, pageSize);

            }
        }


        // 下载EXCEL
        PoiUtil.downLoadExcelToLocalPath(wb, exportPath);

        log.info("导出完成：" + formatDate(new Date(), YYYY_MM_DD_HH_MM_SS));
    }


    /**
     * 导出Excel到浏览器
     *
     * @param response
     * @param totalRowCount           总记录数
     * @param fileName                文件名称
     * @param titles                  标题
     * @param writeExcelDataDelegated 向EXCEL写数据/处理格式的委托类 自行实现
     * @throws Exception
     */
    public static final void exportExcelToWebsite(HttpServletResponse response, Integer totalRowCount, String fileName, String[] titles, WriteExcelDataDelegated writeExcelDataDelegated) throws Exception {

        log.info("开始导出：" + formatDate(new Date(), YYYY_MM_DD_HH_MM_SS));

        // 初始化EXCEL
        SXSSFWorkbook wb = PoiUtil.initExcel(totalRowCount, titles);


        // 调用委托类分批写数据
        int sheetCount = wb.getNumberOfSheets();
        // 数据库分页参数
        int writeRow = 0; // 已写行数
        int writePage = 1;  // 当前页
        int pageSize = PER_WRITE_ROW_COUNT;  // 分页大小
        for (int i = 0; i < sheetCount; i++) {
            SXSSFSheet eachSheet = wb.getSheetAt(i);
            // 向每一张sheet填充数据
            for (int j = 1; j <= PER_SHEET_WRITE_COUNT; j++) {
                // 是否写满
                if (writeRow >= totalRowCount) {
                    break;
                }
                int startRowCount = (j - 1) * PER_WRITE_ROW_COUNT + 1;
                int endRowCount = startRowCount + pageSize - 1;

                writeExcelDataDelegated.writeExcelData(wb, eachSheet, startRowCount, endRowCount, writePage, pageSize);
                writeRow += pageSize;
                writePage ++;
            }
        }


        // 下载EXCEL
        PoiUtil.downLoadExcelToWebsite(wb, response, fileName);

        log.info("导出完成：" + formatDate(new Date(), YYYY_MM_DD_HH_MM_SS));
    }
}
