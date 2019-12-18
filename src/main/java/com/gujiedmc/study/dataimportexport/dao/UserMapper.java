package com.gujiedmc.study.dataimportexport.dao;

import com.gujiedmc.study.dataimportexport.common.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author duyinchuan
 * @date 2019/12/16
 */
@Mapper
public interface UserMapper {


    @Select("SELECT * FROM `user`")
    List<User> list();

    void insertList(@Param("users") List<User> users);
}
