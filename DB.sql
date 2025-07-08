DROP DATABASE IF EXISTS `aniwell`;
CREATE DATABASE `aniwell`;
USE `aniwell`;


-- 게시판 테이블
CREATE TABLE board (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  regDate DATETIME NOT NULL,
  updateDate DATETIME NOT NULL,
  `code` CHAR(50) NOT NULL UNIQUE COMMENT 'notice(공지사항), free(자유), QnA(질의응답)...',
  `name` CHAR(20) NOT NULL UNIQUE COMMENT '게시판 이름',
  delStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제 여부 (0=삭제 전, 1=삭제 후)',
  delDate DATETIME COMMENT '삭제 날짜'
);

-- 회원 테이블
CREATE TABLE MEMBER (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  regDate DATETIME NOT NULL,
  updateDate DATETIME NOT NULL,
  loginId CHAR(30) NOT NULL,
  loginPw CHAR(100) NOT NULL,
  address TEXT NOT NULL,
  authLevel SMALLINT(2) NOT NULL DEFAULT 1 COMMENT '관리자 = 7, 회원 = 1, 수의사 = 3',
  NAME CHAR(20) NOT NULL,
  nickname CHAR(20) NOT NULL,
  cellphone CHAR(20) NOT NULL,
  email CHAR(20) NOT NULL,
  delStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '탈퇴 여부 (0=탈퇴 전, 1=탈퇴 후)',
  authName CHAR(30) NOT NULL COMMENT '일반 또는 수의사',
  delDate DATETIME COMMENT '탈퇴 날짜'
);

-- 반려동물 행동 분석 테이블
CREATE TABLE pet_behavior_analysis (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  memberId INT(10) NOT NULL COMMENT 'FK',
  petId INT(10) NOT NULL COMMENT 'FK',
  analyzedAt DATETIME NOT NULL,
  mood ENUM('행복', '불안', '분노', '슬픔', '놀람') NOT NULL,
  behaviorLabel VARCHAR(100) NOT NULL COMMENT 'FK',
  confidence DECIMAL(5,2) NOT NULL,
  imageUrl TEXT NULL
);

-- 반려동물 건강 로그 테이블
CREATE TABLE pet__health_log (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  petId INT(10) NOT NULL COMMENT 'FK',
  logDate DATETIME NOT NULL COMMENT '로그 기록',
  foodWeight DECIMAL(6,2) NOT NULL,
  waterWeight DECIMAL(6,2) NOT NULL,
  litterCount INT(10) NOT NULL,
  notes TEXT NULL
);

-- 산책 모임 멤버 테이블
CREATE TABLE walk_crew_member (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  crewId INT(10) NOT NULL COMMENT 'PK, FK',
  memberId INT(10) NOT NULL COMMENT 'PK, FK',
  joinedAt DATETIME NOT NULL
);

-- 북마크 테이블
CREATE TABLE bookmark (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  memberId INT(10) NOT NULL COMMENT 'FK, UNIQUE(with articleId)',
  articleId INT(10) NOT NULL COMMENT 'FK, UNIQUE(with memberId)',
  regDate DATETIME NOT NULL DEFAULT NOW(),
  UNIQUE (memberId, articleId)
);

-- 수의사 답변 테이블
CREATE TABLE vet_answer (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  memberId INT(10) NOT NULL COMMENT 'FK',
  answer TEXT NULL,
  answerAt DATETIME NOT NULL DEFAULT NOW(),
  vetName VARCHAR(100) NOT NULL
);

-- 반려동물 추천 장소 테이블
CREATE TABLE pet_recommendation (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  memberId INT(10) NOT NULL COMMENT 'FK',
  TYPE ENUM('병원', '용품') NOT NULL COMMENT '병원, 용품 등',
  NAME VARCHAR(100) NOT NULL,
  address TEXT NOT NULL,
  phone VARCHAR(50) NOT NULL,
  mapUrl TEXT NOT NULL COMMENT '카카오 맵 크롤링',
  createdAt DATETIME NOT NULL DEFAULT NOW()
);

-- 반려동물 예방 접종 테이블
CREATE TABLE pet_vaccination (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  petId INT(10) NOT NULL COMMENT 'FK',
  vaccineName VARCHAR(100) NULL,
  injectionDate DATE NOT NULL,
  nextDueDate DATE NOT NULL COMMENT '다음 접종 자동계산 기입(같은 접종이름, 업데이트 되도록)',
  vetName VARCHAR(100) NOT NULL,
  notes TEXT NULL
);

-- 산책 모임 테이블
CREATE TABLE walk_crew (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  descriptoin TEXT NOT NULL,
  district_id INT NOT NULL COMMENT 'FK → district(id)',
  leaderId INT(10) NOT NULL,
  createdAt DATETIME NOT NULL DEFAULT NOW()
);

-- BLE 기반 반려동물 활동 테이블
CREATE TABLE pet_ble_activity (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  petId INT(10) NOT NULL COMMENT 'FK',
  zoneName VARCHAR(100) NOT NULL COMMENT '화장실 , 밥그릇, 물그릇, 침대',
  enteredAt DATETIME NOT NULL COMMENT '구역 진입 시간',
  exitedAt DATETIME NOT NULL COMMENT '구역 나간 시간',
  durationSec INT NOT NULL COMMENT '구역 머문 시간',
  rssi INT(10) NOT NULL
);

-- 반려동물 테이블
CREATE TABLE pet (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  memberId INT(10) NOT NULL COMMENT 'FK',
  NAME VARCHAR(50) NOT NULL,
  species ENUM('강아지', '고양이') NOT NULL,
  breed VARCHAR(100) NULL COMMENT '자식 낳았는지 안 낳았는지',
  gender ENUM('수컷', '암컷') NOT NULL,
  birthDate DATE NOT NULL,
  weight DECIMAL(5,2) NOT NULL
);

-- 게시글 테이블
CREATE TABLE article (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  regDate DATETIME NOT NULL DEFAULT NOW(),
  updateDate DATETIME NOT NULL DEFAULT NOW(),
  title CHAR(100) NOT NULL,
  `body` TEXT NOT NULL
);

-- 지역 정보 테이블
-- 시(city) → 구(district) → 동(dong) 구조의 행정동 정보를 저장

CREATE TABLE district (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK, 고유 ID',  
  city VARCHAR(50) NOT NULL COMMENT '시/도 이름 (예: 서울특별시, 대전광역시)',  
  district VARCHAR(50) NOT NULL COMMENT '구/군 이름 (예: 서구, 중구)',  
  dong VARCHAR(50) NOT NULL COMMENT '동네 이름 (예: 갈마동, 둔산동)'
);


##예시용 코드-----------------------------------------------------

INSERT INTO board SET regDate = NOW(), updateDate = NOW(), CODE = 'notice', NAME = '공지사항';
INSERT INTO board SET regDate = NOW(), updateDate = NOW(), CODE = 'crew', NAME = '크루모집';
INSERT INTO board SET regDate = NOW(), updateDate = NOW(), CODE = 'qna', NAME = '질의응답';

INSERT INTO MEMBER
SET regDate = NOW(), updateDate = NOW(),
    loginId = 'admin', loginPw = '1234', address = '서울시 중구',
    authLevel = 7, NAME = '관리자', nickname = 'admin',
    cellphone = '010-1234-5678', email = 'admin@example.com',
    authName = '관리자';

INSERT INTO MEMBER
SET regDate = NOW(), updateDate = NOW(),
    loginId = 'vet1', loginPw = 'abcd', address = '대전시 서구',
    authLevel = 3, NAME = '홍수의', nickname = '수의사홍',
    cellphone = '010-2222-3333', email = 'vet@example.com',
    authName = '수의사';

INSERT INTO MEMBER
SET regDate = NOW(), updateDate = NOW(),
    loginId = 'user1', loginPw = 'userpw', address = '청주시 상당구',
    authLevel = 1, NAME = '홍길동', nickname = '길동이',
    cellphone = '010-9999-8888', email = 'user@example.com',
    authName = '일반';

INSERT INTO district SET city = '서울특별시', district = '강남구', dong = '역삼동';
INSERT INTO district SET city = '대전광역시', district = '서구', dong = '둔산동';
INSERT INTO district SET city = '부산광역시', district = '해운대구', dong = '우동';


INSERT INTO pet (memberId, NAME, species, breed, gender, birthDate, weight) VALUES
(1, '콩이', '강아지', '말티즈', '암컷', '2021-05-10', 3.5),
(2, '루비', '고양이', '러시안블루', '암컷', '2020-03-15', 4.2),
(3, '밤비', '강아지', '푸들', '수컷', '2019-11-01', 5.1),
(1, '나비', '고양이', '코리안숏헤어', '수컷', '2022-08-30', 3.8),
(4, '초코', '강아지', '시츄', '암컷', '2018-07-12', 4.0),
(5, '하양이', '고양이', '페르시안', '암컷', '2023-02-25', 2.6);


INSERT INTO article (regDate, updateDate, title, `body`) VALUES
(NOW(), NOW(), '강아지 예방접종 중요성', '강아지도 사람처럼 예방접종이 필요합니다.'),
(NOW(), NOW(), '고양이 발정기 대처법', '고양이의 발정기 행동과 대처 방법을 알려드립니다.'),
(NOW(), NOW(), '반려동물과 산책하기 좋은 장소', '서울에서 강아지와 산책하기 좋은 공원 소개.');


INSERT INTO pet_vaccination (petId, vaccineName, injectionDate, nextDueDate, vetName, notes) VALUES
(1, '혼합백신', '2024-06-01', '2025-06-01', '서울동물병원 김수진', '정기 접종 완료'),
(2, '광견병백신', '2024-03-20', '2025-03-20', '펫케어동물병원 이준호', '다음 접종 예약 필요'),
(3, '장염백신', '2024-07-01', '2025-07-01', '행복동물병원 박서연', '컨디션 양호');


##예시용 코드-----------------------------------------------------
