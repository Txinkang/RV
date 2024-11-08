package com.example.rv.mapper;

import com.example.rv.pojo.Users;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("select * from users where user_name=#{userName}")
    Users findByUserName(String userName);

    @Select("select user_name from users where user_name=#{userName}")
    Users checkUserByUserName(String userName);

    @Select("select user_phone_number from users where user_phone_number=#{userPhoneNumber}")
    Users checkUserByUserPhoneNumber(String userPhoneNumber);

    @Select("select user_email from users where user_email=#{userEmail}")
    Users checkUserByUserEmail(String userEmail);

    @Insert("insert into users (user_name,user_password,user_email,user_phone_number)"+
    "values(#{userName},#{userPassword},#{userEmail},#{userPhoneNumber})")
    Integer addUser(Users user);

    @Select("select * from users where user_id=#{userId}")
    Users findByUserId(Integer userId);

    @Update("update users set " +
            "user_name = case when #{user.userName} != '' then #{user.userName} else user_name end," +
            "user_email = case when #{user.userEmail} != '' then #{user.userEmail} else user_email end," +
            "user_phone_number = case when #{user.userPhoneNumber} != '' then #{user.userPhoneNumber} else user_phone_number end " +
            "where user_id = #{userId}")
    Integer updateUserByUserId(Users user, Integer userId);

    @Update("update users set user_password = #{newPassword} where user_id = #{userId}")
    Integer updatePasswordByUserId(String newPassword, Integer userId);

    @Update("update users set user_role = 1 where user_id = #{userId}")
    Integer updateUserRoleByUserId(Integer userId);

    @Select("select * from users where user_phone_number=#{phoneNum}")
    Users findUserByUserPhoneNumber(String phoneNum);
}
