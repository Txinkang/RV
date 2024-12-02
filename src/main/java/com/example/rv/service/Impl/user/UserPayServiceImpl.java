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
        int transaction = payment.getPaymentTransactionType();
        Users queryUser = userMapper.findByUserId(userId);
        if (queryUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
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
                BigDecimal userBalance = new BigDecimal(String.valueOf(queryUser.getUserBalance()));
                BigDecimal totalPrice = BigDecimal.valueOf(vehiclesReservations.getVehicleReservationTotalPrice());
                if (userBalance.compareTo(totalPrice) < 0) {
                    return new Result(ResultCode.R_UserNoBalance);
                }
                //付款前，先签署合同
                long currentTime = System.currentTimeMillis();
                Timestamp currentTimestamp = new Timestamp(currentTime);
                Integer signedRowAffected = userVehicleMapper.userSignedContract(vehicleReservationId, currentTimestamp);
                if (signedRowAffected < 1) {
                    return new Result(ResultCode.R_SignedContractFailed);
                }
                Integer paymentId = userPayMapper.payForVehicle(userId, vehicleReservationId);
                if (paymentId == null) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                //扣用户钱
                Integer reduceBalance = userPayMapper.reduceUserBalanceById(userId, totalPrice);
                if (reduceBalance < 1) {
                    return new Result(ResultCode.R_ReduceBalanceFailed);
                }
                //给商家加钱
                int businessId = userVehicleMapper.findOwnerByVehicleId(vehiclesReservations.getVehicleReservationVehicleId());
                Integer addBalance = userPayMapper.rechargeByUserId(totalPrice, businessId);
                if (addBalance < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                Integer changeStatus = userPayMapper.changePaymentStatusById(paymentId);
                if (changeStatus < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                Integer changeReservationStatus = userVehicleMapper.changeStatusById(vehicleReservationId);
                if (changeReservationStatus < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
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
                BigDecimal campgroundUserBalance = new BigDecimal(String.valueOf(queryUser.getUserBalance()));
                BigDecimal campgroundTotalPrice = BigDecimal.valueOf(campgroundsReservations.getCampgroundReservationTotalPrice());
                if (campgroundUserBalance.compareTo(campgroundTotalPrice) < 0) {
                    return new Result(ResultCode.R_UserNoBalance);
                }
                //付款前，先签署合同
                long campCurrentTime = System.currentTimeMillis();
                Timestamp campCurrentTimestamp = new Timestamp(campCurrentTime);
                Integer campSigned = userCampgroundMapper.userSignedContract(campgroundReservationId, campCurrentTimestamp);
                if (campSigned < 1) {
                    return new Result(ResultCode.R_SignedContractFailed);
                }
                Integer campPaymentId = userPayMapper.payForCamp(userId, campgroundReservationId);
                if (campPaymentId == null) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                //扣用户钱
                Integer campReduceBalance = userPayMapper.reduceUserBalanceById(userId, campgroundTotalPrice);
                if (campReduceBalance < 1) {
                    return new Result(ResultCode.R_ReduceBalanceFailed);
                }
                //给商家加钱
                int campBusinessId = userCampgroundMapper.findOwnerByCampId(campgroundsReservations.getCampgroundReservationCampgroundId());
                Integer campAddBalance = userPayMapper.rechargeByUserId(campgroundTotalPrice, campBusinessId);
                if (campAddBalance < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                Integer campChangeStatus = userPayMapper.changePaymentStatusById(campPaymentId);
                if (campChangeStatus < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
                Integer changeCampReservationStatus = userCampgroundMapper.changeStatusById(campgroundReservationId);
                if (changeCampReservationStatus < 1) {
                    return new Result(ResultCode.R_UpdateDbFailed);
                }
            }
        }
        return new Result(ResultCode.R_Ok);
    }
}
