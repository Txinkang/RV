package com.example.rv.mapper.user;

import com.example.rv.pojo.Payments;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserPayMapper {
    @Update("update users set user_balance = user_balance + CAST(#{amount} AS DECIMAL(18, 2)) where user_id = #{checkUser}")
    Integer rechargeByUserId(BigDecimal amount, Integer checkUser);

    @Insert("insert into payments (payment_user_id,payment_vehicle_reservation_id,payment_transaction_type,payment_status) " +
            "values (#{userId}, #{vehicleReservationId}, 0, 4)")
    @Options(useGeneratedKeys = true, keyProperty = "payment_id", keyColumn = "payment_id")
    Integer payForVehicle(Integer userId, int vehicleReservationId);
    @Update("update users set user_balance = user_balance - CAST(#{totalPrice} AS DECIMAL(18, 2)) where user_id = #{userId}")
    Integer reduceUserBalanceById(Integer userId, BigDecimal totalPrice);

    @Update("update payments set payment_status = 0 where payment_id = #{paymentId}")
    Integer changePaymentStatusById(Integer paymentId);
    @Insert("insert into payments (payment_user_id,payment_campground_reservation_id,payment_transaction_type,payment_status) " +
            "values (#{paymentUserId}, #{paymentCampgroundReservationId}, #{paymentTransactionType}, #{paymentStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "paymentId", keyColumn = "payment_id")
    Integer payForCamp(Payments payment);

    @Update("update users u1, users u2, payments p, campground_reservations cr " +
            "set u1.user_balance = u1.user_balance - CAST(#{campgroundTotalPrice} AS DECIMAL(18, 2)), " +
            "u2.user_balance = u2.user_balance + CAST(#{campgroundTotalPrice} AS DECIMAL(18, 2)), " +
            "p.payment_status = #{paymentStatus}, " +
            "cr.campground_reservation_status = #{campReservationStatus} " +
            "where u1.user_id = #{userId} " +
            "and u2.user_id = #{campBusinessId} " +
            "and p.payment_id = #{campPaymentId} " +
            "and cr.campground_reservation_id = #{campgroundReservationId}")
    Integer completePayForCamp(Integer userId, int campBusinessId, BigDecimal campgroundTotalPrice,
                               int campPaymentId, int campgroundReservationId,
                               int paymentStatus, int campReservationStatus);

    @Insert("insert into payments (payment_user_id,payment_vehicle_reservation_id,payment_transaction_type,payment_status) " +
            "values (#{paymentUserId}, #{paymentVehicleReservationId}, #{paymentTransactionType}, #{paymentStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "paymentId", keyColumn = "payment_id")
    Integer payForVehicle(Payments payment);

    @Update("update users u1, users u2, payments p, vehicles_reservations vr " +
            "set u1.user_balance = u1.user_balance - CAST(#{vehicleTotalPrice} AS DECIMAL(18, 2)), " +
            "u2.user_balance = u2.user_balance + CAST(#{vehicleTotalPrice} AS DECIMAL(18, 2)), " +
            "p.payment_status = #{paymentStatus}, " +
            "vr.vehicles_reservation_status = #{vehicleReservationStatus} " +
            "where u1.user_id = #{userId} " +
            "and u2.user_id = #{vehicleBusinessId} " +
            "and p.payment_id = #{vehiclePaymentId} " +
            "and vr.vehicles_reservation_id = #{vehicleReservationId}")
    Integer completePayForVehicle(Integer userId, int vehicleBusinessId, BigDecimal vehicleTotalPrice, int vehiclePaymentId, int vehicleReservationId, int paymentStatus, int vehicleReservationStatus);
}
