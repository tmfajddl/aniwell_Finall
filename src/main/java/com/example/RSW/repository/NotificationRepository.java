package com.example.RSW.repository;

import com.example.RSW.vo.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationRepository {

    List<Notification> findByMemberIdOrderByRegDateDesc(@Param("memberId") int memberId);

    boolean existsByMemberIdAndTitleAndLink(@Param("memberId") int memberId, @Param("title") String title, @Param("link") String link);

    int save(Notification notification);

    Optional<Notification> findById(int notificationId);

    int countUnreadByMemberId(@Param("memberId") int memberId);

    void updateAllAsReadByMemberId(@Param("memberId") int memberId);

    void insert(Notification notification);

    void deleteById(@Param("id") int id,  @Param("memberId") int memberId);

    int updateRead(@Param("memberId") int memberId, @Param("notificationId") int notificationId);

    int deleteByLinkAndTitle(@Param("memberId") int memberId, @Param("link") String link, @Param("title") String title);

    boolean existsByMemberIdAndTypeAndLink(@Param("memberId") int memberId, @Param("type") String type, @Param("link") String link);
}
