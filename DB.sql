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
  vetName VARCHAR(100),
  notes TEXT NULL
);

-- 산책 모임 테이블
CREATE TABLE walk_crew (
  id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  descriptoin TEXT NOT NULL,
  AREA VARCHAR(100) NOT NULL,
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

 -- 감정 분석 이미지 저
 
CREATE TABLE pet_analysis (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '분석 결과 ID',
  petId INT UNSIGNED NOT NULL COMMENT '반려동물 ID (FK)',
  imagePath VARCHAR(255) NOT NULL COMMENT '분석에 사용된 이미지 경로 또는 URL',
  emotionResult VARCHAR(50) NOT NULL COMMENT '감정 분석 결과 (예: happy, angry 등)',
  confidence FLOAT NOT NULL COMMENT '분석 결과의 신뢰도 (0.0 ~ 1.0)',
  analyzedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '분석 수행 일시',
  
  CONSTRAINT fk_pet_analysis_petId FOREIGN KEY (petId) REFERENCES pet(id) ON DELETE CASCADE
);

-- 백신 종류 및 주기 테이블
CREATE TABLE vaccine_schedule (
  vaccineName VARCHAR(100) PRIMARY KEY,
  intervalMonths INT NOT NULL COMMENT '백신 주기 (개월 단위)',
  type ENUM('Initial', 'Annual') NOT NULL COMMENT '초기 예방접종 또는 연간 접종 구분',
  description TEXT NULL
);


##예시용 코드-----------------------------------------------------

INSERT INTO board SET regDate = NOW(), updateDate = NOW(), CODE = 'notice', NAME = '공지사항';
INSERT INTO board SET regDate = NOW(), updateDate = NOW(), CODE = 'crew', NAME = '크루모집';
INSERT INTO board SET regDate = NOW(), updateDate = NOW(), CODE = 'qna', NAME = '질의응답';

INSERT INTO `MEMBER`
SET regDate = NOW(), updateDate = NOW(),
    loginId = 'admin', loginPw = '1234', address = '서울시 중구',
    authLevel = 7, NAME = '관리자', nickname = 'admin',
    cellphone = '010-1234-5678', email = 'admin@example.com',
    authName = '관리자';

INSERT INTO `MEMBER`
SET regDate = NOW(), updateDate = NOW(),
    loginId = 'vet1', loginPw = 'abcd', address = '대전시 서구',
    authLevel = 3, NAME = '홍수의', nickname = '수의사홍',
    cellphone = '010-2222-3333', email = 'vet@example.com',
    authName = '수의사';

INSERT INTO `MEMBER`
SET regDate = NOW(), updateDate = NOW(),
    loginId = 'user1', loginPw = 'userpw', address = '청주시 상당구',
    authLevel = 1, NAME = '홍길동', nickname = '길동이',
    cellphone = '010-9999-8888', email = 'user@example.com',
    authName = '일반';

INSERT INTO district SET city = '서울특별시', district = '강남구', dong = '역삼동';
INSERT INTO district SET city = '대전광역시', district = '서구', dong = '둔산동';
INSERT INTO district SET city = '부산광역시', district = '해운대구', dong = '우동';

-- 🐶 홍길동(user1)의 반려동물 강아지 '초코'
INSERT INTO pet
SET memberId = 3, name = '초코', species = '강아지', breed = '말티즈',
    gender = '수컷', birthDate = '2022-03-15', weight = 4.2;

-- 🐱 홍길동(user1)의 반려동물 고양이 '나비'
INSERT INTO pet
SET memberId = 3, name = '나비', species = '고양이', breed = '코리안숏헤어',
    gender = '암컷', birthDate = '2023-01-10', weight = 3.5;

-- 💉 초코(강아지)의 종합백신 접종 기록 (다음 예정 포함)
INSERT INTO pet_vaccination
SET petId = 1, vaccineName = '종합백신', injectionDate = '2025-06-01', nextDueDate = '2025-09-01',
    vetName = '홍수의', notes = '건강 양호, 이상 없음';

-- 💉 초코의 광견병 접종 기록
INSERT INTO pet_vaccination
SET petId = 1, vaccineName = '광견병', injectionDate = '2025-06-15', nextDueDate = '2026-06-15',
    vetName = '홍수의', notes = '경과 관찰 필요';

-- 💉 나비(고양이)의 종합백신 접종 기록
INSERT INTO pet_vaccination
SET petId = 2, vaccineName = '종합백신', injectionDate = '2025-05-20', nextDueDate = '2025-08-20',
    vetName = '홍수의', notes = '입원 치료 이력 있음';
    
    -- 🐶 petId = 1 (예: 강아지)
INSERT INTO pet_analysis (petId, imagePath, emotionResult, confidence, analyzedAt)
VALUES 
(1, '/images/pets/1_01.jpg', 'happy', 0.95, NOW()),
(1, '/images/pets/1_02.jpg', 'relaxed', 0.88, NOW());

-- 🐱 petId = 2 (예: 고양이)
INSERT INTO pet_analysis (petId, imagePath, emotionResult, confidence, analyzedAt)
VALUES 
(2, '/images/pets/2_01.jpg', 'angry', 0.82, NOW()),
(2, '/images/pets/2_02.jpg', 'sad', 0.74, NOW()),
(2, '/images/pets/2_03.jpg', 'happy', 0.91, NOW());

-- 백신 종류 및 주기 데이터 삽입
INSERT INTO vaccine_schedule (vaccineName, intervalMonths, type, description) VALUES
('Rabies', 12, 'Initial', '인간에게 감염될 수 있는 치명적인 바이러스 예방'),
('Parvovirus', 12, 'Initial', '파보 바이러스에 의한 위장관 질환 예방'),
('Distemper', 12, 'Initial', '강아지의 심각한 바이러스성 질병 예방'),
('Feline Distemper', 12, 'Initial', '고양이의 심각한 바이러스성 질병 예방'),
('Feline Leukemia', 12, 'Initial', '고양이의 면역 체계를 약화시키는 바이러스 예방'),
('Leptospirosis', 12, 'Annual', '물과 흙을 통해 퍼지는 세균 감염 예방'),
('Bordetella', 12, 'Annual', '기침과 관련된 바이러스 예방'),
('Feline Panleukopenia', 12, 'Annual', '고양이의 위장관 질환과 관련된 바이러스 예방'),
('FIP', 12, 'Annual', '고양이의 배액 질환과 관련된 질병 예방');
##예시용 코드-----------------------------------------------------
select *
from pet_vaccination;

select *
from pet;

select *
from vaccine_schedule;

select *
from `member`;




DELIMITER $$

CREATE TRIGGER auto_set_next_due_date
BEFORE INSERT ON pet_vaccination
FOR EACH ROW
BEGIN
  DECLARE v_interval INT;

  -- 백신 이름에 맞는 주기 가져오기
  SELECT intervalMonths INTO v_interval
  FROM vaccine_schedule
  WHERE vaccineName = NEW.vaccineName;

  -- 접종일을 기준으로 다음 접종일 계산 (주기 더하기)
  SET NEW.nextDueDate = DATE_ADD(NEW.injectionDate, INTERVAL v_interval MONTH);
END $$

DELIMITER ;
