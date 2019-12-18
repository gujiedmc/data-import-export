package com.gujiedmc.study.dataimportexport.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户
 *
 * @author admin
 * @date 2019/12/16
 */
@Data
@Accessors(chain = true)
public class User {

    private Long id;

    private String email;

    private Date createTime;
}
