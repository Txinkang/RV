package com.example.rv.mapper.business;

import com.example.rv.pojo.Campground;
import com.example.rv.pojo.CampgroundMaintenance;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Select("select * from campground where campground_owner_id = #{ownerId}")
    List<Campground> checkCampgroundList(Integer ownerId);

    @Update("update campground set campground_name = #{campgroundName}, campground_location = #{campgroundLocation}," +
            "campground_facility_details = #{campgroundFacilityDetails}, campground_price = #{campgroundPrice}," +
            "campground_picture = #{campgroundPicture}, campground_status = #{campgroundStatus} where campground_id = #{campgroundId}")
    Integer updateCampground(Campground campground);

    @Select("select * from campground where campground_id = #{campgroundId}")
    Campground checkCampBycampgroundId(Integer campgroundId);

    @Delete("delete from campground where campground_id = #{campgroundId}")
    Integer deleteCampground(Integer campgroundId);

    @Insert("insert into campground_maintenance (campground_maintenance_campground_id, campground_maintenance_details, campground_maintenance_status) values (#{campgroundId}, #{maintenanceDetails}, 0)")
    Integer addCampgroundMaintenance(Integer campgroundId, String maintenanceDetails);

    @Update("update campground set campground_status = #{campgroundStatus} where campground_id = #{campgroundId}")
    Integer updateCampgroundStatus(Integer campgroundId, Integer campgroundStatus);

    @Update("update campground_maintenance set campground_maintenance_status = #{campgroundMaintenanceStatus} where campground_maintenance_id = #{campgroundMaintenanceId}")
    Integer updateCampgroundMaintenanceStatus(Integer campgroundMaintenanceId, Integer campgroundMaintenanceStatus);

    @Select("select * from campground_maintenance where campground_maintenance_campground_id = #{campgroundId} and campground_maintenance_status = #{maintenanceStatus}")
    CampgroundMaintenance checkCampgroundMaintenance(Integer campgroundId, Integer maintenanceStatus);

}
