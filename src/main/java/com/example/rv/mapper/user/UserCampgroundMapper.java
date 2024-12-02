package com.example.rv.mapper.user;

import com.example.rv.pojo.Campground;
import com.example.rv.pojo.CampgroundReservations;
import com.example.rv.pojo.VehiclesReservations;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface UserCampgroundMapper {
    //如果没传status，传入campground实体类的时候会默认赋值0，会影响查询结果。就写了两个查询语句，一个有status，一个没有。
    @Select("select * from campground where (campground_status = #{campgroundStatus} )" +
            "AND (campground_name = #{campgroundName} or #{campgroundName} is null) " +
            "AND (campground_location = #{campgroundLocation} or #{campgroundLocation} is null) " +
            "AND (campground_price = #{campgroundPrice} or #{campgroundPrice} = 0 )")
    List<Campground> findCampHasStatus(Campground campground);

    @Select("select * from campground where " +
            "(campground_name = #{campgroundName} or #{campgroundName} is null) " +
            "AND (campground_location = #{campgroundLocation} or #{campgroundLocation} is null) " +
            "AND (campground_price = #{campgroundPrice} or #{campgroundPrice} = 0 )")
    List<Campground> findCampNoStatus(Campground campground);

    @Insert("insert into campground_reservations (campground_reservation_campground_id,campground_reservation_renter_id," +
            "campground_reservation_start_date,campground_reservation_end_date,campground_reservation_contract_details," +
            "campground_reservation_status,campground_reservation_total_price) " +
            "values(#{campgroundReservations.campgroundReservationCampgroundId},#{renterId},#{campgroundReservations.campgroundReservationStartDate}," +
            "#{campgroundReservations.campgroundReservationEndDate},2,0,#{campgroundReservations.campgroundReservationTotalPrice})")
    Integer reserveCamp(CampgroundReservations campgroundReservations, Integer renterId);

    @Update("update campground set campground_status = #{campStatus} where campground_id = #{campgroundId}")
    Integer updateCampStatusByCampId(int campgroundId, int campStatus);

    @Select("select * from campground where campground_id = #{campgroundId}")
    Campground findCampByCampId(int campgroundId);

    @Select("select * from campground_reservations where (campground_reservation_renter_id = #{renterId}) " +
            "AND (campground_reservation_status = 0)")
    CampgroundReservations checkReservationByRenterId(Integer renterId);
    @Select("select * from campground_reservations where campground_reservation_id = #{campgroundReservationId}" )
    CampgroundReservations findReservationById(int campgroundReservationId);
    @Update("update campground_reservations set campground_reservation_signed_at = #{campCurrentTimestamp} where campground_reservation_id = #{campgroundReservationId}")
    Integer userSignedContract(int campgroundReservationId, Timestamp campCurrentTimestamp);
    @Select("select campground_owner_id from campground where campground_id = #{campgroundReservationCampgroundId}")
    int findOwnerByCampId(int campgroundReservationCampgroundId);
    @Update("update campground_reservations set campground_reservation_status = 2 where campground_reservation_id = #{campgroundReservationId}")
    Integer changeStatusById(int campgroundReservationId);
}
