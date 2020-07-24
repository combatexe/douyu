package com.lei2j.douyu.web.controller;

import com.lei2j.douyu.core.constant.ValidatorConstants;
import com.lei2j.douyu.core.controller.BaseController;
import com.lei2j.douyu.service.UserService;
import com.lei2j.douyu.web.response.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Created by lei2j on 2018/12/16.
 */
@RestController
@RequestMapping("/auth/user")
@Validated
public class UserController extends BaseController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Response register(@RequestParam("email")@Pattern(regexp= ValidatorConstants.REGEX_EMAIL,message="邮箱格式不正确") String email,
                             @RequestParam("captcha")@Pattern(regexp = ValidatorConstants.REGEX_CAPTCHA,message = "验证码格式不正确") String captcha,
                             @RequestParam("password")@Pattern(regexp = ValidatorConstants.REGEX_PASSWORD,message = "密码不符合要求") String password){
        return Response.ok();
    }

    @PostMapping("/login")
    public Response login(@RequestBody@RequestParam("username")@NotBlank String username,
                          @RequestBody@RequestParam("password")@NotBlank String password){
        System.out.println("username:"+username);
        System.out.println("password:"+password);
        return Response.ok();
    }

    @RequestMapping("/test")
    public Response test() {
        return Response.ok();
    }

}
