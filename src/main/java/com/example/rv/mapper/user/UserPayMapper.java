package com.example.rv.mapper.user;

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
            "values (#{userId}, #{campgroundReservationId}, 1, 4)")
    @Options(useGeneratedKeys = true, keyProperty = "payment_id", keyColumn = "payment_id")
    Integer payForCamp(Integer userId, int campgroundReservationId);
}
