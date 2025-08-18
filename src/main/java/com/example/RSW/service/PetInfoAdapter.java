package com.example.RSW.service;


import com.example.RSW.dto.ExplainRequest;
import com.example.RSW.vo.Pet;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class PetInfoAdapter {

    /**
     * 펫 정보를 리포트용 DTO로 변환.
     * @param pet     DB의 Pet VO
     * @param refDate 기준 날짜(문서일자 등). null이면 오늘 날짜 기준으로 나이 계산
     */
    public static ExplainRequest.Pet toExplainPet(Pet pet, LocalDate refDate) {
        if (pet == null) {
            return new ExplainRequest.Pet(null, null, null, null);
        }
        Integer ageYears = calcAgeYears(pet.getBirthDate(), refDate);
        String sex = normalizeSex(pet.getGender());
        // species는 그대로 사용(영문/국문 그대로). breed/weight는 다음 단계에서 확장 가능.
        return new ExplainRequest.Pet(
                safe(pet.getName()),
                safe(pet.getSpecies()),
                ageYears,
                sex
        );
    }

    /** birthDate로 만 나이(정수, 년) 계산 */
    public static Integer calcAgeYears(Date birthDate, LocalDate ref) {
        if (birthDate == null) return null;
        LocalDate birth = Instant.ofEpochMilli(birthDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate base = (ref != null) ? ref : LocalDate.now();
        if (birth.isAfter(base)) return 0; // 미래 생일 방지
        long years = ChronoUnit.YEARS.between(birth, base);
        return (int) years;
    }

    /** 성별 문자열을 M/F 로 정규화 (null-safe) */
    public static String normalizeSex(String gender) {
        if (gender == null) return null;
        String g = gender.trim().toLowerCase();
        // 흔한 표기들 대응
        if (g.matches("m|male|남|수컷|boy|♂|m\\.?")) return "M";
        if (g.matches("f|female|여|암컷|girl|♀|f\\.?")) return "F";
        // “중성화” 같은 정보는 다음 단계에서 별도 표기로 달아줄 수 있음
        return gender; // 원문 유지
    }

    private static String safe(String s){ return (s == null || s.isBlank()) ? null : s; }
}
