package com.gujiedmc.study.dataimportexport.poi.pageexport;

import com.gujiedmc.study.dataimportexport.common.User;
import com.gujiedmc.study.dataimportexport.dao.UserMapper;
import com.gujiedmc.study.dataimportexport.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 导出
 *
 * @author admin
 * @date 2019/12/16
 */
@RestController
@RequestMapping("/poi/export/page")
public class PoiController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @GetMapping("/user/list")
    public List<User> userList(){
        return userMapper.list();
    }

    @GetMapping("/user/create")
    public String createUser(@RequestParam Integer num){


        int count = num/10000;

        for (Integer integer = count; integer > 0; integer--) {

            List<User> users = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                User user = new User()
                        .setCreateTime(new Date())
                        .setEmail(UUID.randomUUID().toString());
                users.add(user);
            }
            userMapper.insertList(users);
        }


        return "success";
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response){
        userService.downloadList(response);
    }
}
