package com.example.RSW.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.repository.WalkCrewRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.WalkCrew;

@Service
public class DistrictService {

	private final DistrictRepository districtRepository;

	@Autowired
	public DistrictService(DistrictRepository districtRepository) {
		this.districtRepository = districtRepository;
	}

	public List<String> getCities() {
		return districtRepository.getDistinctCities();
	}

	public List<String> getDistricts(String city) {
		return districtRepository.getDistrictsByCity(city);
	}

	public List<String> getDongs(String district) {
		return districtRepository.getDongsByDistrict(district);
	}
}