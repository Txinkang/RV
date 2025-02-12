package com.example.rv.mapper.admin;

import com.example.rv.pojo.Admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper {
    @Select("select * from admin where admin_account = #{adminAccount}")
    Admin checkAdminByAccount(String adminAccount);

    // 获取车辆总数
    @Select("SELECT COUNT(*) FROM vehicles")
    Integer getVehicleCount();

    // 获取营地总数
    @Select("SELECT COUNT(*) FROM campground")
    Integer getCampCount();

    // 获取订单总数
    @Select("SELECT COUNT(*) FROM payments")
    Integer getPaymentCount();

    // 获取已完成订单数量(status=0)
    @Select("SELECT COUNT(*) FROM payments WHERE payment_status = #{status}")
    Integer getPaymentCountByStatus(Integer status);


}
