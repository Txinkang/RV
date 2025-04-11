package com.example.rv.service.Impl.user;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.user.UserCampgroundMapper;
import com.example.rv.mapper.user.UserMapper;
import com.example.rv.mapper.user.UserPayMapper;
import com.example.rv.mapper.user.UserVehicleMapper;
import com.example.rv.pojo.*;
import com.example.rv.service.UserPayService;
import com.example.rv.service.common.EmailService;
import com.example.rv.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserPayServiceImpl implements UserPayService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserPayMapper userPayMapper;
    @Autowired
    private UserVehicleMapper userVehicleMapper;
    @Autowired
    private UserCampgroundMapper userCampgroundMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public Result recharge(BigDecimal amount) {
        BigDecimal decimalAmount = new BigDecimal(String.valueOf(amount));
        if (decimalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new Result(ResultCode.R_ParamError);
        }
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer userId = (Integer) userMap.get("id");
        if (userId == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer checkUser = userMapper.checkUserByUserId(userId);
        if (checkUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        Integer rowAffected = userPayMapper.rechargeByUserId(amount, checkUser);
        if (rowAffected > 0){
            // 发送邮件通知充值成功
            Users queryUser = userMapper.findByUserId(userId);
            if (queryUser == null){
                return new Result(ResultCode.R_UserNotFound);
            }
            String email = queryUser.getUserEmail();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("充值成功通知");
            message.setText("您已成功充值，充值金额为: " + amount + "\n" +
                    "感谢您的支持，祝您旅途愉快！");
            message.setFrom("2912528586@qq.com");
            mailSender.send(message);
        }
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result payment(Payments payment) {
        if (payment == null) {
            return new Result(ResultCode.R_ParamError);
        }
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer userId = (Integer) userMap.get("id");
        if (userId == null) {
            return new Result(ResultCode.R_Error);
        }
        Users queryUser = userMapper.findByUserId(userId);
        if (queryUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        int transaction = payment.getPaymentTransactionType();
        switch (transaction) {
            //0是车辆，1是营地
            case 0 -> {
                int vehicleReservationId = payment.getPaymentVehicleReservationId();
                if (vehicleReservationId <= 0) {
                    return new Result(ResultCode.R_ParamError);
                }
                VehiclesReservations vehiclesReservations = userVehicleMapper.findReservationById(vehicleReservationId);
                if (vehiclesReservations == null) {
                    return new Result(ResultCode.R_ReservationNotFound);
                }
                //查看预定是否超时，超时就取消
                long vehicleCurrentTime = System.currentTimeMillis();
                Timestamp vehicleCurrentTimestamp = new Timestamp(vehicleCurrentTime);
                if (vehicleCurrentTimestamp.after(vehiclesReservations.getVehicleReservationEndDate())) {
                    int vehicleId = vehiclesReservations.getVehicleReservationVehicleId();
                    int vehicleReservationStatus = 1;
                    int vehicleStatus = 0;
                    //这边自动取消失败不用怕，还有个接口专门取消预定的
                    userVehicleMapper.cancelReservationById(vehicleReservationId, vehicleId, vehicleReservationStatus, vehicleStatus);
                    return new Result(ResultCode.R_ReservationTimeout);
                }
                //查看余额是否充足
                BigDecimal vehicleUserBalance = new BigDecimal(String.valueOf(queryUser.getUserBalance()));
                BigDecimal vehicleTotalPrice = BigDecimal.valueOf(vehiclesReservations.getVehicleReservationTotalPrice());
                if (vehicleUserBalance.compareTo(vehicleTotalPrice) < 0) {
                    return new Result(ResultCode.R_UserNoBalance);
                }
                //付款前，先签署合同
                Integer signedRowAffected = userVehicleMapper.userSignedContract(vehicleReservationId, vehicleCurrentTimestamp);
                if (signedRowAffected < 1) {
                    return new Result(ResultCode.R_SignedContractFailed);
                }
                //插入支付表
                payment.setPaymentUserId(userId);
                payment.setPaymentVehicleReservationId(vehicleReservationId);
                payment.setPaymentTransactionType(0);
                payment.setPaymentStatus(4);
                Integer vehicleInsertPayment = userPayMapper.payForVehicle(payment);
                if (vehicleInsertPayment < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                //完成支付
                int vehicleBusinessId = userVehicleMapper.findOwnerByVehicleId(vehiclesReservations.getVehicleReservationVehicleId());
                int paymentStatus = 0;
                int vehiclePaymentId = payment.getPaymentId();
                int vehicleReservationStatus = 2;
                Integer vehicleCompletePay = userPayMapper.completePayForVehicle(userId, vehicleBusinessId, vehicleTotalPrice, vehiclePaymentId, vehicleReservationId, paymentStatus, vehicleReservationStatus);
                if (vehicleCompletePay < 1) {
                    return new Result(ResultCode.R_PaymentFailed);
                }
                // 发送邮件通知付款成功
                String email = queryUser.getUserEmail();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("付款成功通知");
                message.setText("您已成功支付车辆预定费用，预定信息如下：\n" +
                        "预定ID: " + vehicleReservationId + "\n" +
                        "开始日期: " + vehiclesReservations.getVehicleReservationStartDate() + "\n" +
                        "结束日期: " + vehiclesReservations.getVehicleReservationEndDate() + "\n" +
                        "总金额: " + vehicleTotalPrice + "\n" +
                        "感谢您的支持，祝您旅途愉快！");
                message.setFrom("2912528586@qq.com");
                mailSender.send(message);
            }
            case 1 -> {
                int campgroundReservationId = payment.getPaymentCampgroundReservationId();
                if (campgroundReservationId <= 0) {
                    return new Result(ResultCode.R_ParamError);
                }
                CampgroundReservations campgroundsReservations = userCampgroundMapper.findReservationById(campgroundReservationId);
                if (campgroundsReservations == null) {
                    return new Result(ResultCode.R_ReservationNotFound);
                }
                //查看预定是否超时，超时就取消
                long campCurrentTime = System.currentTimeMillis();
                Timestamp campCurrentTimestamp = new Timestamp(campCurrentTime);
                if (campCurrentTimestamp.after(campgroundsReservations.getCampgroundReservationEndDate())) {
                    int campgroundId = campgroundsReservations.getCampgroundReservationCampgroundId();
                    int campReservationStatus = 1;
                    int campStatus = 0;
                    //这边自动取消失败不用怕，还有个接口专门取消预定的
                    userCampgroundMapper.cancelReservationById(campgroundReservationId, campgroundId, campReservationStatus, campStatus);
                    return new Result(ResultCode.R_ReservationTimeout);
                }
                //查看余额是否充足
                BigDecimal campgroundUserBalance = new BigDecimal(String.valueOf(queryUser.getUserBalance()));
                BigDecimal campgroundTotalPrice = BigDecimal.valueOf(campgroundsReservations.getCampgroundReservationTotalPrice());
                if (campgroundUserBalance.compareTo(campgroundTotalPrice) < 0) {
                    return new Result(ResultCode.R_UserNoBalance);
                }
                //付款前，先签署合同
                Integer campSigned = userCampgroundMapper.userSignedContract(campgroundReservationId, campCurrentTimestamp);
                if (campSigned < 1) {
                    return new Result(ResultCode.R_SignedContractFailed);
                }
                //插入支付表
                payment.setPaymentUserId(userId);
                payment.setPaymentCampgroundReservationId(campgroundReservationId);
                payment.setPaymentTransactionType(1);
                payment.setPaymentStatus(4);
                Integer campInsertPayment = userPayMapper.payForCamp(payment);
                if (campInsertPayment < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                //完成支付
                int campBusinessId = userCampgroundMapper.findOwnerByCampId(campgroundsReservations.getCampgroundReservationCampgroundId());
                int paymentStatus = 0;
                int campPaymentId = payment.getPaymentId();
                int CampReservationStatus = 2;
                Integer campCompletePay = userPayMapper.completePayForCamp(userId, campBusinessId, campgroundTotalPrice, campPaymentId, campgroundReservationId, paymentStatus, CampReservationStatus);
                if (campCompletePay < 1) {
                    return new Result(ResultCode.R_PaymentFailed);
                }
                // 发送邮件通知付款成功
                String email = queryUser.getUserEmail();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("付款成功通知");
                message.setText("您已成功支付营地预定费用，预定信息如下：\n" +
                        "预定ID: " + campgroundReservationId + "\n" +
                        "开始日期: " + campgroundsReservations.getCampgroundReservationStartDate() + "\n" +
                        "结束日期: " + campgroundsReservations.getCampgroundReservationEndDate() + "\n" +
                        "总金额: " + campgroundTotalPrice + "\n" +
                        "感谢您的支持，祝您旅途愉快！");
                message.setFrom("2912528586@qq.com");
                mailSender.send(message);
            }
        }
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result refund(Map<String,Integer> paramMap) {
        if (paramMap == null){
            return new Result(ResultCode.R_ParamError);
        }
        int reservationId = paramMap.get("reservationId");
        int transactionType = paramMap.get("transactionType");
        if (reservationId < 1 || transactionType < 0){
            return new Result(ResultCode.R_ParamError);
        }
        switch (transactionType) {
            //0是车辆，1是营地
            case 0 -> {
                Payments queryPayment = userPayMapper.findPaymentByVRId(reservationId);
                if (queryPayment == null){
                    return new Result(ResultCode.R_PaymentNotFound);
                }
                VehiclesReservations vehiclesReservations = userVehicleMapper.findReservationById(queryPayment.getPaymentVehicleReservationId());
                if (vehiclesReservations == null) {
                    return new Result(ResultCode.R_ReservationNotFound);
                }
                //到达开始时间后，则不让退款
                long vehicleCurrentTime = System.currentTimeMillis();
                Timestamp vehicleTimestamp = new Timestamp(vehicleCurrentTime);
                LocalDateTime dateTime = vehiclesReservations.getVehicleReservationStartDate().toLocalDateTime();
                Timestamp previousTimestamp = Timestamp.valueOf(dateTime);
                if (vehicleTimestamp.after(previousTimestamp)) {
                    return new Result(ResultCode.R_ExceedRefundTime);
                }
                //退款   改变金额，改变各种status
                int paymentId = queryPayment.getPaymentId();
                int payment_status = 2;
                int vehicleReservationStatus = 3;
                int vehicleStatus = 0;
                BigDecimal refundPrice = BigDecimal.valueOf(vehiclesReservations.getVehicleReservationTotalPrice());
                int vehicleId = vehiclesReservations.getVehicleReservationVehicleId();
                int vehicleReservationId = vehiclesReservations.getVehicleReservationId();
                int userId = queryPayment.getPaymentUserId();
                int businessId = userVehicleMapper.findOwnerByVehicleId(vehicleId);
                if (businessId < 1) {
                    return new Result(ResultCode.R_Error);
                }
                Integer refund = userPayMapper.refundVehicleByPaymentId(paymentId, vehicleId, vehicleReservationId, userId, businessId, payment_status, vehicleStatus, vehicleReservationStatus, refundPrice);
                if (refund == null || refund < 5){
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                // 发送邮件通知退款成功
                Users queryUser = userMapper.findByUserId(userId);
                if (queryUser == null){ 
                    return new Result(ResultCode.R_UserNotFound);
                }
                String email = queryUser.getUserEmail();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("退款成功通知");
                message.setText("您已成功退款，退款金额为: " + refundPrice + "\n" +
                        "感谢您的支持，祝您旅途愉快！");
                message.setFrom("2912528586@qq.com");
                mailSender.send(message);
            }
            case 1 -> {
                Payments queryPayment = userPayMapper.findPaymentByCRId(reservationId);
                if (queryPayment == null){
                    return new Result(ResultCode.R_PaymentNotFound);
                }
                CampgroundReservations campgroundReservations = userCampgroundMapper.findReservationById(queryPayment.getPaymentCampgroundReservationId());
                if (campgroundReservations == null) {
                    return new Result(ResultCode.R_ReservationNotFound);
                }
                //到达开始时间后，则不让退款
                long campCurrentTime = System.currentTimeMillis();
                Timestamp campTimestamp = new Timestamp(campCurrentTime);
                LocalDateTime dateTime = campgroundReservations.getCampgroundReservationStartDate().toLocalDateTime();
                Timestamp previousTimestamp = Timestamp.valueOf(dateTime);
                if (campTimestamp.after(previousTimestamp)) {
                    return new Result(ResultCode.R_ExceedRefundTime);
                }
                //退款   改变金额，改变各种status
                int paymentId = queryPayment.getPaymentId();
                int payment_status = 2;
                int campReservationStatus = 3;
                int campStatus = 0;
                BigDecimal refundPrice = BigDecimal.valueOf(campgroundReservations.getCampgroundReservationTotalPrice());
                int campId = campgroundReservations.getCampgroundReservationCampgroundId();
                int campReservationId = campgroundReservations.getCampgroundReservationId();
                int userId = queryPayment.getPaymentUserId();
                int businessId = userCampgroundMapper.findOwnerByCampId(campId);
                if (businessId < 1) {
                    return new Result(ResultCode.R_Error);
                }
                Integer refund = userPayMapper.refundCampByPaymentId(paymentId, campId, campReservationId, userId, businessId, payment_status, campStatus, campReservationStatus, refundPrice);
                if (refund == null || refund < 5){
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                // 发送邮件通知退款成功
                Users queryUser = userMapper.findByUserId(userId);
                if (queryUser == null){ 
                    return new Result(ResultCode.R_UserNotFound);
                }
                String email = queryUser.getUserEmail();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("退款成功通知");
                message.setText("您已成功退款，退款金额为: " + refundPrice + "\n" +
                        "感谢您的支持，祝您旅途愉快！");
                message.setFrom("2912528586@qq.com");
                mailSender.send(message);
            }
            default -> {
                return new Result(ResultCode.R_Fail);
            }
        }
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result checkOrder() {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer userId = (Integer) userMap.get("id");
        if (userId == null) {
            return new Result(ResultCode.R_Error);
        }
        Users queryUser = userMapper.findByUserId(userId);
        if (queryUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        //查询需要的字段，整合到一起返回
        List<Payments> queryPayments = userPayMapper.findPaymentByUserId(userId);
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Payments payment : queryPayments){
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("payment_id",payment.getPaymentId());
            responseMap.put("payment_transaction_type",payment.getPaymentTransactionType());
            responseMap.put("payment_status",payment.getPaymentStatus());
            if (payment.getPaymentTransactionType() == 0) {
                VehiclesReservations vehiclesReservations = userVehicleMapper.findReservationById(payment.getPaymentVehicleReservationId());
                responseMap.put("start_date",vehiclesReservations.getVehicleReservationStartDate());
                responseMap.put("end_date",vehiclesReservations.getVehicleReservationEndDate());
                responseMap.put("total_price",vehiclesReservations.getVehicleReservationTotalPrice());
            } else if (payment.getPaymentTransactionType() == 1) {
                CampgroundReservations campgroundReservations = userCampgroundMapper.findReservationById(payment.getPaymentCampgroundReservationId());
                responseMap.put("start_date",campgroundReservations.getCampgroundReservationStartDate());
                responseMap.put("end_date",campgroundReservations.getCampgroundReservationEndDate());
                responseMap.put("total_price",campgroundReservations.getCampgroundReservationTotalPrice());
            }
            responseList.add(responseMap);
        }
        return new Result(ResultCode.R_Ok,responseList);
    }

    @Override
    public Result generateInvoice(Map<String, Object> paramMap) {
        if (paramMap == null){
            return new Result(ResultCode.R_ParamError);
        }
        Integer paymentId = (Integer) paramMap.get("paymentId");
        double paymentTotalPrice = (double) paramMap.get("paymentTotalPrice");
        if (paymentId < 1 || paymentTotalPrice <= 0){
            return new Result(ResultCode.R_ParamError);
        }
        Invoices queryInvoice = userPayMapper.findInvoiceByPaymentId(paymentId);
        if (queryInvoice != null){
            return new Result(ResultCode.R_InvoiceAlreadyExist);
        }
        Payments payments = userPayMapper.findPaymentByPaymentId(paymentId);
        if (payments == null){
            return new Result(ResultCode.R_PaymentNotFound);
        }
        if (payments.getPaymentStatus() != 0){
            return new Result(ResultCode.R_PaymentNotSuccess);
        }
        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Integer rowAffected = userPayMapper.generetedInvoice(paymentId,invoiceNumber,paymentTotalPrice);
        return new Result(rowAffected > 0 ?ResultCode.R_Ok:ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result checkInvoice(Invoices invoices) {
        if (invoices == null || invoices.getInvoicePaymentId() < 1){
            return new Result(ResultCode.R_Error);
        }
        Invoices queryInvoice = userPayMapper.findInvoiceByPaymentId(invoices.getInvoicePaymentId());
        if (queryInvoice == null){
            return new Result(ResultCode.R_PaymentNotGenerateInvoice);
        }
        return new Result(ResultCode.R_Ok,queryInvoice);
    }

}
