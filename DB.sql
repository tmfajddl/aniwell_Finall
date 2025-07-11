DROP DATABASE IF EXISTS `aniwell`;
CREATE DATABASE `aniwell`;
USE `aniwell`;


-- 게시판 테이블
CREATE TABLE board
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    `code`     CHAR(50) NOT NULL UNIQUE COMMENT 'notice(공지사항), free(자유), QnA(질의응답)...',
    `name`     CHAR(20) NOT NULL UNIQUE COMMENT '게시판 이름',
    delStatus  TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제 여부 (0=삭제 전, 1=삭제 후)',
    delDate    DATETIME COMMENT '삭제 날짜'
);

-- 회원 테이블
CREATE TABLE MEMBER
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME  NOT NULL,
    updateDate DATETIME  NOT NULL,
    loginId    CHAR(30)  NOT NULL,
    loginPw    CHAR(100) NOT NULL,
    address    TEXT      NOT NULL,
    authLevel  SMALLINT(2) NOT NULL DEFAULT 1 COMMENT '관리자 = 7, 회원 = 1, 수의사 = 3',
    NAME       CHAR(20)  NOT NULL,
    nickname   CHAR(20)  NOT NULL,
    cellphone  CHAR(20)  NOT NULL,
    email      CHAR(20)  NOT NULL,
    delStatus  TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '탈퇴 여부 (0=탈퇴 전, 1=탈퇴 후)',
    authName   CHAR(30)  NOT NULL COMMENT '일반 또는 수의사',
    delDate    DATETIME COMMENT '탈퇴 날짜'
);

SELECT * FROM MEMBER;

-- 반려동물 행동 분석 테이블
CREATE TABLE pet_behavior_analysis
(
    id            INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId      INT(10) NOT NULL COMMENT 'FK',
    petId         INT(10) NOT NULL COMMENT 'FK',
    analyzedAt    DATETIME      NOT NULL,
    mood          ENUM('행복', '불안', '분노', '슬픔', '놀람') NOT NULL,
    behaviorLabel VARCHAR(100)  NOT NULL COMMENT 'FK',
    confidence    DECIMAL(5, 2) NOT NULL,
    imageUrl      TEXT NULL
);

-- 반려동물 건강 로그 테이블
CREATE TABLE pet__health_log
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId       INT(10) NOT NULL COMMENT 'FK',
    logDate     DATETIME      NOT NULL COMMENT '로그 기록',
    foodWeight  DECIMAL(6, 2) NOT NULL,
    waterWeight DECIMAL(6, 2) NOT NULL,
    litterCount INT(10) NOT NULL,
    notes       TEXT NULL
);

-- 산책 모임 멤버 테이블
CREATE TABLE walk_crew_member
(
    id       INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    crewId   INT(10) NOT NULL COMMENT 'PK, FK',
    memberId INT(10) NOT NULL COMMENT 'PK, FK',
    joinedAt DATETIME NOT NULL
);

-- 북마크 테이블
CREATE TABLE bookmark
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK, UNIQUE(with articleId)',
    articleId INT(10) NOT NULL COMMENT 'FK, UNIQUE(with memberId)',
    regDate   DATETIME NOT NULL DEFAULT NOW(),
    UNIQUE (memberId, articleId)
);

-- 수의사 답변 테이블
CREATE TABLE vet_answer
(
    id       INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId INT(10) NOT NULL COMMENT 'FK',
    answer   TEXT NULL,
    answerAt DATETIME     NOT NULL DEFAULT NOW(),
    vetName  VARCHAR(100) NOT NULL
);

ALTER TABLE vet_answer
    ADD COLUMN qna_id INT(10) UNSIGNED NOT NULL COMMENT '질문 ID (Qna 테이블 FK)';

-- 반려동물 추천 장소 테이블
CREATE TABLE pet_recommendation
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK',
    TYPE      ENUM('병원', '용품') NOT NULL COMMENT '병원, 용품 등',
    NAME      VARCHAR(100) NOT NULL,
    address   TEXT         NOT NULL,
    phone     VARCHAR(50)  NOT NULL,
    mapUrl    TEXT         NOT NULL COMMENT '카카오 맵 크롤링',
    createdAt DATETIME     NOT NULL DEFAULT NOW()
);

-- 반려동물 예방 접종 테이블
CREATE TABLE pet_vaccination
(
    id            INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId         INT(10) NOT NULL COMMENT 'FK',
    vaccineName   VARCHAR(100) NULL,
    injectionDate DATE         NOT NULL,
    nextDueDate   DATE         NULL COMMENT '다음 접종 자동계산 기입(같은 접종이름, 업데이트 되도록)',
    vetName       VARCHAR(100)  NULL,
    notes         TEXT NULL
);

CREATE TABLE pet_analysis (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '분석 결과 ID',
  petId INT UNSIGNED NOT NULL COMMENT '반려동물 ID (FK)',
  imagePath VARCHAR(255) NOT NULL COMMENT '분석에 사용된 이미지 경로 또는 URL',
  emotionResult VARCHAR(50) NOT NULL COMMENT '감정 분석 결과 (예: happy, angry 등)',
  confidence FLOAT NOT NULL COMMENT '분석 결과의 신뢰도 (0.0 ~ 1.0)',
  analyzedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '분석 수행 일시',
  
  CONSTRAINT fk_pet_analysis_petId FOREIGN KEY (petId) REFERENCES pet(id) ON DELETE CASCADE
);

-- 산책 모임 테이블
CREATE TABLE walk_crew
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    descriptoin TEXT         NOT NULL,
    district_id INT          NOT NULL COMMENT 'FK → district(id)',
    leaderId    INT(10) NOT NULL,
    createdAt   DATETIME     NOT NULL DEFAULT NOW()
);

-- BLE 기반 반려동물 활동 테이블
CREATE TABLE pet_ble_activity
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId       INT(10) NOT NULL COMMENT 'FK',
    zoneName    VARCHAR(100) NOT NULL COMMENT '화장실 , 밥그릇, 물그릇, 침대',
    enteredAt   DATETIME     NOT NULL COMMENT '구역 진입 시간',
    exitedAt    DATETIME     NOT NULL COMMENT '구역 나간 시간',
    durationSec INT          NOT NULL COMMENT '구역 머문 시간',
    rssi        INT(10) NOT NULL
);

-- 반려동물 테이블
CREATE TABLE pet
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK',
    NAME      VARCHAR(50)   NOT NULL,
    species   ENUM('강아지', '고양이') NOT NULL,
    breed     VARCHAR(100) NULL COMMENT '자식 낳았는지 안 낳았는지',
    gender    ENUM('수컷', '암컷') NOT NULL,
    birthDate DATE          NOT NULL,
    weight    DECIMAL(5, 2) NOT NULL
);

ALTER TABLE pet ADD COLUMN photo VARCHAR(255);

-- 게시글 테이블
CREATE TABLE article
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME  NOT NULL DEFAULT NOW(),
    updateDate DATETIME  NOT NULL DEFAULT NOW(),
    title      CHAR(100) NOT NULL,
    `body`     TEXT      NOT NULL
);

--  memberId 추가
ALTER TABLE article
    ADD COLUMN memberId INT(10) UNSIGNED NOT NULL AFTER updateDate;

--  boardId 추가
ALTER TABLE article
    ADD COLUMN boardId INT(10) NOT NULL AFTER `memberId`;

-- hit 추가
ALTER TABLE article
    ADD COLUMN hitCount INT(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `body`;

--  article 테이블에 reactionPoint(좋아요) 컬럼 추가
ALTER TABLE article
    ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE article
    ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;


-- 지역 정보 테이블
-- 시(city) → 구(district) → 동(dong) 구조의 행정동 정보를 저장
CREATE TABLE district
(
    id       INT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK, 고유 ID',
    city     VARCHAR(50) NOT NULL COMMENT '시/도 이름 (예: 서울특별시, 대전광역시)',
    district VARCHAR(50) NOT NULL COMMENT '구/군 이름 (예: 서구, 중구)',
    dong     VARCHAR(50) NOT NULL COMMENT '동네 이름 (예: 갈마동, 둔산동)'
);


-- 댓글 테이블
CREATE TABLE reply
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate     DATETIME NOT NULL,
    updateDate  DATETIME NOT NULL,
    memberId    INT(10) UNSIGNED NOT NULL,
    relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
    relId       INT(10) NOT NULL COMMENT '관련 데이터 번호',
    `body`      TEXT     NOT NULL
);

-- QnA 테이블
CREATE TABLE Qna
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId   INT(10) UNSIGNED NOT NULL COMMENT '질문 작성자 (회원)',
    title      VARCHAR(100) NOT NULL, -- 질문 제목
    `body`     TEXT         NOT NULL, -- 질문 내용 or 답변 본문
    isSecret   BOOLEAN DEFAULT FALSE, -- 비공개 여부
    isFromUser BOOLEAN DEFAULT FALSE, -- 사용자 등록 질문 여부
    isAnswered BOOLEAN DEFAULT FALSE, -- 관리자 답변 여부
    orderNo    INT(10) NOT NULL DEFAULT 0 COMMENT '출력 순서',
    regDate    DATETIME     NOT NULL,
    updateDate DATETIME     NOT NULL,
    isActive   BOOLEAN DEFAULT TRUE   -- 노출 여부 (숨김 처리 가능)
);

-- 자주 묻는 질문 구분(1= 자주 묻는 질문, 0 = 일반 질문)
ALTER TABLE qna
    ADD COLUMN isFaq TINYINT(1) NOT NULL DEFAULT 0;


UPDATE Qna SET isSecret = 0 WHERE isSecret NOT IN (0, 1);

UPDATE Qna SET isSecret = 1 WHERE id IN (1, 2, 3); -- 비밀글
UPDATE Qna SET isSecret = 0 WHERE id IN (4, 5, 6, 11); -- 공개글

SELECT * FROM Qna;

-- reply 테이블 생성
CREATE TABLE reply
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate     DATETIME NOT NULL,
    updateDate  DATETIME NOT NULL,
    memberId    INT(10) UNSIGNED NOT NULL,
    relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
    relId       INT(10) NOT NULL COMMENT '관련 데이터 번호',
    `body`      TEXT     NOT NULL
);

--  reply 테이블에 좋아요 관련 컬럼 추가
ALTER TABLE reply ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE reply ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;

-- reactionPoint 테이블 생성

CREATE TABLE reactionPoint
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate     DATETIME NOT NULL,
    updateDate  DATETIME NOT NULL,
    memberId    INT(10) UNSIGNED NOT NULL,
    relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
    relId       INT(10) NOT NULL COMMENT '관련 데이터 번호',
    `point`     INT(10) NOT NULL
);


-- 수의사 인증서 테이블
CREATE TABLE vet_certificate
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId   INT(10) UNSIGNED NOT NULL COMMENT '회원 ID (FK)',
    fileName   VARCHAR(255)     NOT NULL COMMENT '업로드된 원본 파일명',
    filePath   VARCHAR(500)     NOT NULL COMMENT '서버 저장 경로',
    uploadedAt DATETIME         NOT NULL DEFAULT NOW() COMMENT '업로드 일시',
    approved   TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '승인 여부 (0=대기, 1=승인, 2=거절)',

    -- FK 연결
    FOREIGN KEY (memberId) REFERENCES MEMBER(id) ON DELETE CASCADE
);

-- 백신 종류 및 주기 테이블
CREATE TABLE vaccine_schedule (
  vaccineName VARCHAR(100) PRIMARY KEY,
  intervalMonths INT NOT NULL COMMENT '백신 주기 (개월 단위)',
  type ENUM('Initial', 'Annual') NOT NULL COMMENT '초기 예방접종 또는 연간 접종 구분',
  description TEXT NULL
  )
  
  CREATE TABLE calendar_event (
  id INT AUTO_INCREMENT PRIMARY KEY,
  memberId INT NOT NULL,                  
  petId INT NULL,                        
  eventDate DATE NOT NULL,               
  content TEXT NOT NULL,                 
  createdAt DATETIME DEFAULT NOW()       
);

ALTER TABLE calendar_event ADD COLUMN title VARCHAR(100) NOT NULL AFTER petId;

DELIMITER $$

CREATE TRIGGER auto_set_next_due_date
BEFORE INSERT ON pet_vaccination
FOR EACH ROW
BEGIN
  DECLARE v_interval INT DEFAULT NULL;

  -- 백신 이름에 맞는 주기 가져오기
  SELECT intervalMonths INTO v_interval
  FROM vaccine_schedule
  WHERE vaccineName = NEW.vaccineName
  LIMIT 1;

  -- 주기값과 날짜가 null이 아닐 때만 설정
  IF v_interval IS NOT NULL AND NEW.injectionDate IS NOT NULL THEN
    SET NEW.nextDueDate = DATE_ADD(NEW.injectionDate, INTERVAL v_interval MONTH);
  ELSE
    SET NEW.nextDueDate = NULL;
  END IF;
END $$

DELIMITER ;


##예시용
코드-----------------------------------------------------

INSERT INTO board SET regDate = NOW(), updateDate = NOW(), CODE = 'notice', NAME = '공지사항';
INSERT INTO board
SET regDate = NOW(), updateDate = NOW(), CODE = 'crew', NAME = '크루모집';
INSERT INTO board
SET regDate = NOW(), updateDate = NOW(), CODE = 'qna', NAME = '질의응답';

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

INSERT INTO district
SET city = '서울특별시', district = '강남구', dong = '역삼동';
INSERT INTO district
SET city = '대전광역시', district = '서구', dong = '둔산동';
INSERT INTO district
SET city = '부산광역시', district = '해운대구', dong = '우동';


INSERT INTO pet (memberId, NAME, species, breed, gender, birthDate, weight)
VALUES (1, '콩이', '강아지', '말티즈', '암컷', '2021-05-10', 3.5),
       (2, '루비', '고양이', '러시안블루', '암컷', '2020-03-15', 4.2),
       (3, '밤비', '강아지', '푸들', '수컷', '2019-11-01', 5.1),
       (1, '나비', '고양이', '코리안숏헤어', '수컷', '2022-08-30', 3.8),
       (4, '초코', '강아지', '시츄', '암컷', '2018-07-12', 4.0),
       (5, '하양이', '고양이', '페르시안', '암컷', '2023-02-25', 2.6);


INSERT INTO article (regDate, updateDate, memberId, boardId, title, `body`)
VALUES (NOW(), NOW(), 1, 1, '강아지 예방접종 중요성', '강아지도 사람처럼 예방접종이 필요합니다.'),
       (NOW(), NOW(), 1, 1, '고양이 발정기 대처법', '고양이의 발정기 행동과 대처 방법을 알려드립니다.'),
       (NOW(), NOW(), 1, 1, '반려동물과 산책하기 좋은 장소', '서울에서 강아지와 산책하기 좋은 공원 소개.');



INSERT INTO pet_vaccination (petId, vaccineName, injectionDate, nextDueDate, vetName, notes)
VALUES (1, '혼합백신', '2024-06-01', '2025-06-01', '서울동물병원 김수진', '정기 접종 완료'),
       (2, '광견병백신', '2024-03-20', '2025-03-20', '펫케어동물병원 이준호', '다음 접종 예약 필요'),
       (3, '장염백신', '2024-07-01', '2025-07-01', '행복동물병원 박서연', '컨디션 양호');


INSERT INTO article
(boardId, memberId, title, BODY, regDate, updateDate)
VALUES
    (1, 1, '공지사항 테스트', '공지사항 내용입니다.', NOW(), NOW());

INSERT INTO pet (memberId, NAME, species, breed, gender, birthDate, weight)
VALUES (10, '보리', '강아지', '시바견', '수컷', '2022-01-01', 6.3);


INSERT INTO Qna (memberId, title, BODY, isSecret, isFromUser, isAnswered, orderNo, regDate, updateDate, isActive)
VALUES
-- 1번: 예방접종 질문
(1, '강아지는 언제부터 예방접종을 시작해야 하나요?',
 '보통 생후 6~8주부터 시작하며, 이후 매년 추가 접종이 필요합니다.',
 FALSE, FALSE, TRUE, 1, NOW(), NOW(), TRUE),

-- 2번: 고양이 중성화
(1, '고양이 중성화 수술은 언제 하는 게 좋나요?',
 '암컷은 생후 6개월 전후, 수컷은 생후 5~6개월에 하는 것이 일반적입니다.',
 FALSE, FALSE, TRUE, 2, NOW(), NOW(), TRUE),

-- 3번: 강아지 설사
(1, '강아지가 설사를 자주 하는데 병원에 데려가야 하나요?',
 '3일 이상 지속되거나 피가 섞이면 병원에 방문해야 합니다.',
 FALSE, FALSE, TRUE, 3, NOW(), NOW(), TRUE);
 
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


##예시용코드-----------------------------------------------------

SELECT * FROM article;

SELECT loginId, COUNT(*)
FROM MEMBER
GROUP BY loginId
HAVING COUNT(*) > 1;

-- admin 중복 제거 (1개만 남김)
DELETE FROM MEMBER
WHERE id NOT IN (
    SELECT MIN(id) FROM MEMBER WHERE loginId = 'admin'
);

-- user1 중복 제거
DELETE FROM MEMBER
WHERE id NOT IN (
    SELECT MIN(id) FROM MEMBER WHERE loginId = 'user1'
);

-- vet1 중복 제거
DELETE FROM MEMBER
WHERE id NOT IN (
    SELECT MIN(id) FROM MEMBER WHERE loginId = 'vet1'
);

SET SQL_SAFE_UPDATES = 0;

-- 비밀번호 sha로 변경

UPDATE MEMBER
SET loginPw = SHA2('abcd', 256)
WHERE loginId = 'vet1';

UPDATE MEMBER
SET loginPw = SHA2('userpw', 256)
WHERE loginId = 'user1';

UPDATE MEMBER
SET loginPw = SHA2('1234', 256)
WHERE loginId = 'admin';


-- 자주 묻는 질문 설정
UPDATE qna
SET isFaq = 1
WHERE id IN (1, 2, 3);


SELECT id, title, isFaq FROM qna ORDER BY id;



select *
from pet_vaccination;

select *
from pet;

select *
from vaccine_schedule;

select *
from `member`;

select *
from pet_analysis;

select *
from walk_crew;

