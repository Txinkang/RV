package com.example.rv.mapper.user;

import com.example.rv.pojo.Invoices;
import com.example.rv.pojo.Payments;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserPayMapper {
    @Update("update users set user_balance = user_balance + CAST(#{amount} AS DECIMAL(18, 2)) where user_id = #{checkUser}")
    Integer rechargeByUserId(BigDecimal amount, Integer checkUser);

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
            "vr.vehicle_reservation_status = #{vehicleReservationStatus} " +
            "where u1.user_id = #{userId} " +
            "and u2.user_id = #{vehicleBusinessId} " +
            "and p.payment_id = #{vehiclePaymentId} " +
            "and vr.vehicle_reservation_id = #{vehicleReservationId}")
    Integer completePayForVehicle(Integer userId, int vehicleBusinessId, BigDecimal vehicleTotalPrice, int vehiclePaymentId, int vehicleReservationId, int paymentStatus, int vehicleReservationStatus);

    @Update("update users u1, users u2, payments p, vehicles_reservations vr, vehicles v " +
            "set u1.user_balance = u1.user_balance - CAST(#{refundPrice} AS DECIMAL(18, 2)), " +
            "u2.user_balance = u2.user_balance + CAST(#{refundPrice} AS DECIMAL(18, 2)), " +
            "p.payment_status = #{paymentStatus}, " +
            "vr.vehicle_reservation_status = #{vehicleReservationStatus}, " +
            "v.vehicle_status = #{vehicleStatus} " +
            "where u1.user_id = #{businessId} " +
            "and u2.user_id = #{userId} " +
            "and p.payment_id = #{paymentId} " +
            "and vr.vehicle_reservation_id = #{vehicleReservationId} " +
            "and v.vehicle_id = #{vehicleId}")
    Integer refundVehicleByPaymentId(Integer paymentId, int vehicleId, int vehicleReservationId, int userId, int businessId, int paymentStatus, int vehicleStatus, int vehicleReservationStatus, BigDecimal refundPrice);

    @Update("update users u1, users u2, payments p, campground_reservations cr, campground c " +
            "set u1.user_balance = u1.user_balance - CAST(#{refundPrice} AS DECIMAL(18, 2)), " +
            "u2.user_balance = u2.user_balance + CAST(#{refundPrice} AS DECIMAL(18, 2)), " +
            "p.payment_status = #{paymentStatus}, " +
            "cr.campground_reservation_status = #{campReservationStatus}, " +
            "c.campground_status = #{campStatus} " +
            "where u1.user_id = #{businessId} " +
            "and u2.user_id = #{userId} " +
            "and p.payment_id = #{paymentId} " +
            "and cr.campground_reservation_id = #{campReservationId} " +
            "and c.campground_id = #{campId}")
    Integer refundCampByPaymentId(Integer paymentId, int campId, int campReservationId, int userId, int businessId, int paymentStatus, int campStatus, int campReservationStatus, BigDecimal refundPrice);

    @Select("select * from payments where payment_vehicle_reservation_id = #{reservationId}")
    Payments findPaymentByVRId(int reservationId);

    @Select("select * from payments where payment_campground_reservation_id = #{reservationId}")
    Payments findPaymentByCRId(int reservationId);

    @Select("select * from payments where payment_user_id = #{userId}")
    List<Payments> findPaymentByUserId(Integer userId);

    @Select("select * from payments where payment_id = #{paymentId}")
    Payments findPaymentByPaymentId(Integer paymentId);

    @Insert("insert into invoices (invoice_payment_id, invoice_number, invoice_total_amount) " +
            "values(#{paymentId}, #{invoiceNumber}, #{paymentTotalPrice})")
    Integer generetedInvoice(Integer paymentId, String invoiceNumber, double paymentTotalPrice);

    @Select("select * from invoices where invoice_payment_id = #{paymentId}")
    Invoices findInvoiceByPaymentId(Integer paymentId);
}
