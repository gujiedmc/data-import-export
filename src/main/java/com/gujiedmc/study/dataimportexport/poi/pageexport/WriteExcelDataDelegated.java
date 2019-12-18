package com.gujiedmc.study.dataimportexport.poi.pageexport;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * excel单行写处理
 *
 * @author admin
 * @date 2019/12/16
 */
@FunctionalInterface
public interface WriteExcelDataDelegated {

    /**
     * 返回添加的条数
     */
    Long writeExcelData(Workbook workbook, Sheet sheet, Integer startRow, Integer endRow, Integer currentPage, Integer pageSize);
}
