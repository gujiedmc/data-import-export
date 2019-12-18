package com.gujiedmc.study.dataimportexport.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gujiedmc.study.dataimportexport.common.User;
import com.gujiedmc.study.dataimportexport.dao.UserMapper;
import com.gujiedmc.study.dataimportexport.poi.pageexport.PoiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author admin
 * @date 2019/12/16
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageInfo<User> page(int page, int size) {
        PageHelper.startPage(page, size);

        List<User> list = userMapper.list();

        PageInfo<User> userPageInfo = new PageInfo<>(list);

        return userPageInfo;
    }

    @Override
    public void downloadList(HttpServletResponse response) {
        PageInfo<User> page = this.page(1, 1);
        long total = page.getTotal();
        String[] titles = {"ID", "email", "createTime"};
        try {
            PoiUtil.exportExcelToWebsite(response, (int) total, "导出", titles,
                    (workbook, sheet, startRow, endRow, currentPage, pageSize) -> {
                        // 查询数据
                        PageInfo<User> pageInfo = this.page(currentPage, pageSize);
                        List<User> userList = pageInfo.getList();

                        if (!CollectionUtils.isEmpty(userList)) {

                            // 插入指定行的数据
                            CreationHelper creationHelper = workbook.getCreationHelper();
                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
                            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直对齐方式
                            cellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平对齐方式

                            for (int i = startRow; i <= endRow; i++) {
                                if ((i - startRow) >= userList.size()) {
                                    break;
                                }
                                User user = userList.get(i - startRow);
                                Row row = sheet.createRow(i);
                                row.createCell(0, CellType.STRING).setCellValue(user.getId());
                                row.createCell(1, CellType.STRING).setCellValue(user.getEmail());
                                Cell cell3 = row.createCell(2, CellType.NUMERIC);
                                cell3.setCellValue(user.getCreateTime());
                                cell3.setCellStyle(cellStyle);

                            }
                        }
                        return (long) userList.size();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
