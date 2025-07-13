package com.example.RSW.controller;

import com.example.RSW.service.NotificationService;
import com.example.RSW.vo.Notification;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/usr/notifications") // 사용자 알림 기능 컨트롤러
public class UsrNotificationController {

    @Autowired
    private Rq rq;

    @Autowired
    private NotificationService notificationService;

    // 기본 접근 시 목록 페이지로 리다이렉트
    @GetMapping("")
    public String redirectToList() {
        return "redirect:/usr/notifications/list";
    }

    // 최근 알림 5개 (드롭다운용 AJAX)
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentNotifications() {
        if (!rq.isLogined()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        int memberId = rq.getLoginedMemberId();
        List<Notification> list = notificationService.getRecentNotifications(memberId);
        return ResponseEntity.ok(list);
    }

    // 전체 알림 목록 페이지
    @GetMapping("/list")
    public String showNotificationList(HttpServletRequest request, HttpServletResponse response, Model model) {
        if (!rq.isLogined()) {
            // 비로그인 시 alert 후 창 닫기
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>alert('로그인이 필요합니다.'); window.close();</script>");
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        int memberId = rq.getLoginedMemberId();
        List<Notification> notifications = notificationService.getNotificationsByMemberId(memberId);
        model.addAttribute("notifications", notifications);

        return "usr/notification/list"; // JSP 경로
    }

    // 개별 알림 읽음 처리 (AJAX)
    @PostMapping("/markAsRead")
    @ResponseBody
    public ResultData markAsRead(@RequestParam int notificationId) {
        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }

        int memberId = rq.getLoginedMemberId();
        boolean success = notificationService.markAsRead(memberId, notificationId);

        if (success) {
            return ResultData.from("S-1", "읽음 처리되었습니다.");
        } else {
            return ResultData.from("F-1", "읽음 처리 실패 또는 권한 없음.");
        }
    }

    // 읽지 않은 알림 개수 조회 (드롭다운 배지용)
    @GetMapping("/unreadCount")
    @ResponseBody
    public ResultData getUnreadCount() {
        if (!rq.isLogined()) {
            return ResultData.from("S-1", "로그인 필요 없음", "count", 0);
        }

        int count = notificationService.getUnreadCount(rq.getLoginedMemberId());
        return ResultData.from("S-1", "성공", "count", count);
    }

    // 전체 알림 읽음 처리 (버튼용)
    @PostMapping("/markAllAsRead")
    @ResponseBody
    public ResultData markAllAsRead() {
        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }

        notificationService.markAllAsRead(rq.getLoginedMemberId());
        return ResultData.from("S-1", "모든 알림을 읽음 처리했습니다.");
    }

    // 알림 읽음 처리 후 링크로 이동
    @GetMapping("/readAndGo")
    public String readAndRedirect(@RequestParam int id) {
        Notification n = notificationService.findById(id);

        // 본인의 알림인지 확인 후 읽음 처리 및 링크로 이동
        if (n != null && rq.isLogined() && rq.getLoginedMemberId() == n.getMemberId()) {
            notificationService.markAsRead(n.getMemberId(), n.getId());
            return "redirect:" + n.getLink();
        }

        return "redirect:/"; // 권한 없거나 잘못된 요청이면 홈으로
    }
}
