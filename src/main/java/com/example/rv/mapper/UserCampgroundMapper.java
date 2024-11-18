package com.example.rv.mapper;

import com.example.rv.pojo.Campground;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
