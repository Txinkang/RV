package com.example.rv.mapper.user;

import com.example.rv.pojo.Vehicles;
import com.example.rv.pojo.VehiclesReservations;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface UserVehicleMapper {
    //如果没传status，传入vehicle实体类的时候会默认赋值0，会影响查询结果。就写了两个查询语句，一个有status，一个没有。
    @Select("select * from vehicles where (vehicle_status = #{vehicleStatus} )" +
            "AND (vehicle_type = #{vehicleType} or #{vehicleType} is null) " +
            "AND (vehicle_location = #{vehicleLocation} or #{vehicleLocation} is null) " +
            "AND (vehicle_price = #{vehiclePrice} or #{vehiclePrice} = 0 )")
    List<Vehicles> findVehicleHasStatus(Vehicles vehicle);

    @Select("select * from vehicles where " +
            "(vehicle_type = #{vehicleType} or #{vehicleType} is null) " +
            "AND (vehicle_location = #{vehicleLocation} or #{vehicleLocation} is null) " +
            "AND (vehicle_price = #{vehiclePrice} or #{vehiclePrice} = 0 )")
    List<Vehicles> findVehicleNoStatus(Vehicles vehicle);

    @Select("select * from vehicles where vehicle_id = #{vehicleId}")
    Vehicles findVehicleByVehicleId(int vehicleId);

    @Select("select * from vehicles_reservations where vehicle_reservation_id = #{reservationsId}" )
    VehiclesReservations findReservationById(Integer reservationsId);

    @Select("select * from vehicles_reservations where (vehicle_reservation_renter_id = #{renterId}) " +
            "AND (vehicle_reservation_status in (0,2))")
    VehiclesReservations checkReservationByRenterId(Integer renterId);

    @Insert("insert into vehicles_reservations (vehicle_reservation_vehicle_id,vehicle_reservation_renter_id," +
            "vehicle_reservation_start_date,vehicle_reservation_end_date,vehicle_reservation_contract_details," +
            "vehicle_reservation_status,vehicle_reservation_total_price) " +
            "values(#{vehiclesReservations.vehicleReservationVehicleId},#{renterId},#{vehiclesReservations.vehicleReservationStartDate}," +
            "#{vehiclesReservations.vehicleReservationEndDate},1,0,#{vehiclesReservations.vehicleReservationTotalPrice})")
    Integer reserveVehicle(VehiclesReservations vehiclesReservations, Integer renterId);

    @Update("update vehicles set vehicle_status = #{status} where vehicle_id = #{vehicleId}")
    Integer updateVehicleStatusByVehicleId(int vehicleId, int status);
    @Update("update vehicles_reservations set vehicle_reservation_signed_at = #{currentTimestamp} where vehicle_reservation_id = #{vehicleReservationId}")
    Integer userSignedContract(int vehicleReservationId, Timestamp currentTimestamp);

    @Update("UPDATE vehicles_reservations, vehicles " +
            "SET vehicles_reservations.vehicle_reservation_status = #{vehicleReservationStatus}, " +
            "    vehicles.vehicle_status = #{vehicleStatus} " +
            "WHERE vehicles_reservations.vehicle_reservation_id = #{vehicleReservationId} " +
            "  AND vehicles.vehicle_id = #{vehicleId};")
    Integer cancelReservationById(int vehicleReservationId, int vehicleId, int vehicleReservationStatus, int vehicleStatus);

    @Select("select vehicle_owner_id from vehicles where vehicle_id = #{vehicleId}")
    int findOwnerByVehicleId(int vehicleId);

    @Update("update vehicles set vehicle_location = #{location} where vehicle_id = #{vehicleId}")
    Integer updateVehicleLocation(int vehicleId, String location);
}
