package com.gujiedmc.study.dataimportexport.service;

import com.github.pagehelper.PageInfo;
import com.gujiedmc.study.dataimportexport.common.User;

import javax.servlet.http.HttpServletResponse;

/**
 * @author duyinchuan
 * @date 2019/12/16
 */
public interface UserService {

    PageInfo<User> page(int page, int size);

    void downloadList(HttpServletResponse response);
}
