package com.example.RSW.service;

import com.example.RSW.repository.VetCertificateRepository;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.VetCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class VetCertificateService {

    @Autowired
    private VetCertificateRepository vetCertificateRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private NotificationService notificationService;

    // 인증서 등록
    public ResultData registerCertificate(VetCertificate vetCertificate) {
        vetCertificateRepository.insert(vetCertificate);
        return ResultData.from("S-1", "수의사 인증서가 등록되었습니다.");
    }

    // 전체 인증서 조회 (관리자용)
    public List<VetCertificate> getAllCertificates() {
        return vetCertificateRepository.findAll();
    }

    // 특정 회원의 인증서 조회
    public VetCertificate getCertificateByMemberId(int memberId) {
        return vetCertificateRepository.findByMemberId(memberId);
    }

    // 승인 상태 변경 (0: 대기, 1: 승인, 2: 거절)
    public ResultData updateApprovalStatus(int id, int approved) {
        vetCertificateRepository.updateApprovalStatus(id, approved);
        return ResultData.from("S-1", "승인 상태가 변경되었습니다.");
    }

    // 인증서 삭제
    public ResultData deleteCertificate(int id) {
        vetCertificateRepository.deleteById(id);
        return ResultData.from("S-1", "인증서가 삭제되었습니다.");
    }

    public ResultData deleteCertificateWithFile(VetCertificate cert) {
        // 1. 파일 삭제
        String baseDir = "C:/upload/vet_certificates/";
        String fullPath = baseDir + cert.getFilePath();

        File file = new File(fullPath);
        if (file.exists()) {
            file.delete();
        }

        // 2. DB 삭제
        vetCertificateRepository.deleteById(cert.getId());

        return ResultData.from("S-1", "인증서 및 파일이 삭제되었습니다.");
    }

    public void updateApprovalStatusByMemberId(int memberId, int approved) {
        VetCertificate cert = vetCertificateRepository.findByMemberId(memberId);
        if (cert != null) {
            vetCertificateRepository.updateApprovalStatus(cert.getId(), approved);
        }
    }

    // 관리자에게 알림을 보내는 메서드
    private void sendNotificationToAdmins(int vetMemberId) {
        // 수의사 이름 가져오기
        Member vetMember = memberService.getMemberById(vetMemberId);
        String vetName = vetMember.getName(); // 수의사 이름

        // 관리자 목록 가져오기
        List<Member> admins = memberService.getAdmins(); // 관리자 목록 가져오기

        // 알림 전송
        for (Member admin : admins) {
            String title = vetName + "님이 인증서를 등록하였습니다."; // 알림 제목
            String link = "/adm/member/list?memberId=" + vetMemberId; // 인증서 등록 상세 페이지 링크
            notificationService.addNotification(admin.getId(), vetMemberId, "VET_CERT_UPLOAD", title, link);
        }
    }

}
