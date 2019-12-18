package com.gujiedmc.study.dataimportexport.common;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * http文件下载工具类型
 *
 * @author admin
 * @date 2019/12/13
 */
public class HttpDownloadUtils {

    public static void setDownloadHeader(HttpServletResponse response, String fileName){
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        String newName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
        response.addHeader("Content-Disposition","attachment;filename=" + newName);
    }
}
