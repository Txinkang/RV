package com.example.rv.Response;

public enum ResultCode {
    //成功
    R_Ok(200, "操作成功"),
    //失败
    R_Fail(400, "操作失败"),
    //异常
    R_Error(500, "操作异常"),

    R_TimeOut(600,"超时"),
    //-----------------------系统内部错误(不应该出现但防止出现) 501-600---------------------
    R_WhyNull(501, "不可能NULL的数据为NULL了"),
    R_UpdateDbFailed(502, "修改数据库失败"),


    //-----------------------用户相关错误 401-500---------------------
    R_ParamError(401,"参数异常"),
    R_UserNameIsExist(402,"用户名已存在"),
    R_UserPhoneNumberIsExist(403, "手机号已注册"),
    R_UserEmailIsExist(404,"邮箱已被注册"),
    R_PasswordError(405,"密码错误"),
    R_UserNotFound(406,"该用户不存在"),
    R_OldPasswordError(407,"旧密码错误"),
    R_NewPasswordNotSame(408,"新密码不一致"),
    R_RoleAlreadyUpgrade(409,"权限早已升级"),
    R_CodeError(410,"验证码错误"),
    ;

    private int code;
    private String msg;
    ResultCode(int code,String msg){
        this.code=code;
        this.msg=msg;
    }
    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }
}
