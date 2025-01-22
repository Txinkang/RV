package com.example.rv.mapper.business;

import com.example.rv.pojo.Vehicles;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BusinessVehicleMapper {
    @Insert("insert into vehicles (vehicle_owner_id,vehicle_type,vehicle_location,vehicle_description," +
            "vehicle_price,vehicle_picture,vehicle_status)" +
            "values(#{vehicle.vehicleOwnerId},#{vehicle.vehicleType},#{vehicle.vehicleLocation}," +
            "#{vehicle.vehicleDescription},#{vehicle.vehiclePrice},#{pictureNamesJson},3)")
    Integer addVehicle(Vehicles vehicle, String pictureNamesJson);

    @Select("select * from vehicles where vehicle_owner_id = #{ownerId}")
    List<Vehicles> checkVehicleList(Integer ownerId);

    @Select("select * from vehicles where vehicle_id = #{vehicleId}")
    Vehicles checkVehicleByvehicleId(int vehicleId);

    @Update("update vehicles set vehicle_type = #{vehicleType}, vehicle_location = #{vehicleLocation}, vehicle_description = #{vehicleDescription}, vehicle_price = #{vehiclePrice}, vehicle_picture = #{vehiclePicture}, vehicle_status = #{vehicleStatus} where vehicle_id = #{vehicleId}")
    Integer updateVehicle(Vehicles vehicle);

    @Delete("delete from vehicles where vehicle_id = #{vehicleId}")
    Integer deleteVehicle(int vehicleId);

    @Insert("insert into vehicle_maintenance (vehicle_maintenance_vehicle_id, vehicle_maintenance_maintenance_details, vehicle_maintenance_status) values (#{vehicleId}, #{maintenanceDetails}, 0)")
    Integer addVehicleMaintenance(Integer vehicleId, String maintenanceDetails);

    @Update("update vehicles set vehicle_status = #{vehicleStatus} where vehicle_id = #{vehicleId}")
    Integer updateVehicleStatus(Integer vehicleId, Integer vehicleStatus);
}
