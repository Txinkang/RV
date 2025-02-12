package com.example.rv.mapper.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.*;

import com.example.rv.pojo.Campground;
import com.example.rv.pojo.Vehicles;


@Mapper
public interface AdminAuditMapper {

    //根据状态查询 campground
    @Select("SELECT * FROM campground WHERE campground_status = #{campgroundStatus}")
    List<Campground> findCampgroundByStatus(Integer campgroundStatus);

    //根据id查询 campground
    @Select("SELECT * FROM campground WHERE campground_id = #{campgroundId}")
    Campground findCampgroundById(Integer campgroundId);

    //更新 campground 状态
    @Update("UPDATE campground SET campground_status = #{campgroundStatus}, campground_audit_failed_msg = #{auditFailedMsg} WHERE campground_id = #{campgroundId}")
    Integer updateCampgroundStatus(Integer campgroundId, Integer campgroundStatus, String auditFailedMsg);

    //根据状态查询 vehicles
    @Select("SELECT * FROM vehicles WHERE vehicle_status = #{vehicleStatus}")
    List<Vehicles> findVehicleByStatus(Integer vehicleStatus);

    //根据id查询 vehicles
    @Select("SELECT * FROM vehicles WHERE vehicle_id = #{vehicleId}")
    Vehicles findVehicleById(Integer vehicleId);

    //更新车辆状态
    @Update("UPDATE vehicles SET vehicle_status = #{vehicleStatus}, vehicle_audit_failed_msg = #{auditFailedMsg} WHERE vehicle_id = #{vehicleId}")
    Integer updateVehicleStatus(Integer vehicleId, Integer vehicleStatus, String auditFailedMsg);
}
