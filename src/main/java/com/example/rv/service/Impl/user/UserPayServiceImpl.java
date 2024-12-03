package com.example.rv.service.Impl.user;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.user.UserCampgroundMapper;
import com.example.rv.mapper.user.UserMapper;
import com.example.rv.mapper.user.UserPayMapper;
import com.example.rv.mapper.user.UserVehicleMapper;
import com.example.rv.pojo.CampgroundReservations;
import com.example.rv.pojo.Payments;
import com.example.rv.pojo.Users;
import com.example.rv.pojo.VehiclesReservations;
import com.example.rv.service.UserPayService;
import com.example.rv.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

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
                if (vehicleCurrentTimestamp.after(vehiclesReservations.getVehicleReservationEndDate())){
                    int vehicleId = vehiclesReservations.getVehicleReservationVehicleId();
                    int vehicleReservationStatus = 1;
                    int vehicleStatus = 0;
                    //这边自动取消失败不用怕，还有个接口专门取消预定的
                    userVehicleMapper.cancelReservationById(vehicleReservationId,vehicleId,vehicleReservationStatus,vehicleStatus);
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
                Integer insertPayment = userPayMapper.payForVehicle(payment);
                if (insertPayment < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                //完成支付
                int vehicleBusinessId = userVehicleMapper.findOwnerByCampId(vehiclesReservations.getVehicleReservationVehicleId());
                int paymentStatus = 0;
                int vehiclePaymentId = payment.getPaymentId();
                int vehicleReservationStatus = 2;
                Integer vehicleCompletePay = userPayMapper.completePayForVehicle(userId,vehicleBusinessId,vehicleTotalPrice,vehiclePaymentId,vehicleReservationId,paymentStatus,vehicleReservationStatus);
                if (vehicleCompletePay < 1){
                    return new Result(ResultCode.R_PaymentFailed);
                }
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
                if (campCurrentTimestamp.after(campgroundsReservations.getCampgroundReservationEndDate())){
                    int campgroundId = campgroundsReservations.getCampgroundReservationCampgroundId();
                    int campReservationStatus = 1;
                    int campStatus = 0;
                    //这边自动取消失败不用怕，还有个接口专门取消预定的
                    userCampgroundMapper.cancelReservationById(campgroundReservationId,campgroundId,campReservationStatus,campStatus);
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
                Integer insertPayment = userPayMapper.payForCamp(payment);
                if (insertPayment < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                //完成支付
                int campBusinessId = userCampgroundMapper.findOwnerByCampId(campgroundsReservations.getCampgroundReservationCampgroundId());
                int paymentStatus = 0;
                int campPaymentId = payment.getPaymentId();
                int CampReservationStatus = 2;
                Integer campCompletePay = userPayMapper.completePayForCamp(userId,campBusinessId,campgroundTotalPrice,campPaymentId,campgroundReservationId,paymentStatus,CampReservationStatus);
                if (campCompletePay < 1){
                    return new Result(ResultCode.R_PaymentFailed);
                }
            }
        }
        return new Result(ResultCode.R_Ok);
    }
}
