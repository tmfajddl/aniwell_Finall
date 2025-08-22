package com.example.RSW.repository;

import com.example.RSW.vo.LitterEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LitterEventRepository {
    int insert(LitterEvent e);

    List<LitterEvent> findRecentByPet(@Param("petId") Long petId,
                                      @Param("limit") int limit);
}
