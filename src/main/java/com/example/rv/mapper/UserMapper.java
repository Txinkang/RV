package com.example.rv.mapper;

import com.example.rv.pojo.Users;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
    int addUser(Users user);
}
