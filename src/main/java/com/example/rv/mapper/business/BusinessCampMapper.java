package com.example.rv.mapper.business;

import com.example.rv.pojo.Campground;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BusinessCampMapper {
    @Insert("insert into campground (campground_owner_id,campground_name,campground_location,campground_facility_details," +
            "campground_price,campground_picture,campground_status)" +
            "values(#{campground.campgroundOwnerId},#{campground.campgroundName},#{campground.campgroundLocation}," +
            "#{campground.campgroundFacilityDetails},#{campground.campgroundPrice},#{pictureNamesJson},3)")
    Integer addCampground(Campground campground, String pictureNamesJson);

    @Select("select campground_name from campground where campground_name = #{campgroundName}")
    String checkCampBycampgroundName(String campgroundName);
    @Select("select campground_location from campground where campground_location = #{campgroundLocation}")
    String checkCampBycampgroundLocation(String campgroundLocation);

}
