package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.WalkCrew;

@Mapper
public interface DistrictRepository {

	List<String> getDistinctCities();

	List<String> getDistrictsByCity(String city);

	List<String> getDongsByDistrict(String district);
}