package com.example.rv.mapper.business;

import com.example.rv.pojo.Vehicles;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusinessVehicleMapper {
    @Insert("insert into vehicles (vehicle_owner_id,vehicle_type,vehicle_location,vehicle_description," +
            "vehicle_price,vehicle_picture,vehicle_status)" +
            "values(#{vehicle.vehicleOwnerId},#{vehicle.vehicleType},#{vehicle.vehicleLocation}," +
            "#{vehicle.vehicleDescription},#{vehicle.vehiclePrice},#{pictureNamesJson},3)")
    Integer addVehicle(Vehicles vehicle, String pictureNamesJson);
}
