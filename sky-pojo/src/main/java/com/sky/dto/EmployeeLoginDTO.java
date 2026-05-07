package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data   //员工登录时传递的数据模型
public class EmployeeLoginDTO implements Serializable {

    private String username;

    private String password;

}
