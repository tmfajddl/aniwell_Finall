package com.example.RSW.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.RSW.vo.ResultData;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	@Autowired
	private JavaMailSender sender;

	@Value("${custom.emailFrom}")
	private String emailFrom;
	@Value("${custom.emailFromName}")
	private String emailFromName;

	private static class MailHandler {
		private final JavaMailSender sender;
		private final MimeMessage message;
		private final MimeMessageHelper messageHelper;

		public MailHandler(JavaMailSender sender) throws MessagingException {
			this.sender = sender;
			this.message = this.sender.createMimeMessage();
			this.messageHelper = new MimeMessageHelper(message, true, "UTF-8");
		}

		public void setFrom(String mail, String name) throws UnsupportedEncodingException, MessagingException {
			messageHelper.setFrom(mail, name);
		}

		public void setTo(String mail) throws MessagingException {
			messageHelper.setTo(mail);
		}

		public void setSubject(String subject) throws MessagingException {
			messageHelper.setSubject(subject);
		}

		public void setText(String text) throws MessagingException {
			messageHelper.setText(text, true);
		}

		// ✅ 예외를 삼키지 말고 그대로 던지기
		public void send() {
			sender.send(message);
		}
	}

	public ResultData send(String email, String title, String body) {
		try {
			// From 값 기초 검증
			if (emailFrom == null || emailFrom.isBlank()) {
				return ResultData.from("F-0", "발신자(From) 주소가 비어 있습니다. custom.emailFrom 확인");
			}

			MailHandler mail = new MailHandler(sender);
			mail.setFrom(emailFrom.trim(), emailFromName);
			mail.setTo(email);
			mail.setSubject(title);
			mail.setText(body);
			mail.send(); // 실패 시 예외가 여기까지 전달됨

			return ResultData.from("S-1", "메일이 발송되었습니다.");
		} catch (MailAuthenticationException e) {
			e.printStackTrace();
			return ResultData.from("F-AUTH", "메일 서버 인증 실패 (username=apikey / password=API_KEY / Single Sender 검증 확인)");
		} catch (MailSendException e) {
			e.printStackTrace();
			return ResultData.from("F-SEND", "메일 전송 실패 (수신자 주소/네트워크/포트 확인)");
		} catch (MessagingException e) {
			e.printStackTrace();
			return ResultData.from("F-MSG", "메일 구성 중 오류 (MessagingException)");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ResultData.from("F-ENC", "인코딩 오류");
		} catch (Exception e) {
			e.printStackTrace();
			return ResultData.from("F-UNK", "알 수 없는 오류");
		}
	}
}
