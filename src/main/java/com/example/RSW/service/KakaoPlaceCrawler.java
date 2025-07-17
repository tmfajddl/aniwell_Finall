package com.example.RSW.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KakaoPlaceCrawler {

    public static Map<String, Object> crawlPlace(String url) {
        Map<String, Object> result = new HashMap<>();

        // ✅ Chrome 옵션 설정 (화면 안 뜨게)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // 창 안 띄움
        options.addArguments("--disable-gpu"); // GPU 비활성화 (윈도우용)
        options.addArguments("--no-sandbox");  // 샌드박스 사용 안 함 (리눅스용)
        options.addArguments("--disable-dev-shm-usage"); // shared memory 문제 방지

        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(url);
            Thread.sleep(2000); // JS 렌더링 대기

            // ✅ 운영 상태
            String status = safeText(driver, By.cssSelector(".tit_detail"));
            if (status.isEmpty()) status = "정보 없음";

            // ✅ 영업 시간
            String openHour = safeText(driver, By.cssSelector(".txt_detail.add_mdot"));
            if (openHour.isEmpty() || openHour.matches(".*도보.*")) openHour = "정보 없음";

            // ✅ 주소
            String address = safeText(driver, By.cssSelector(
                    "#mainContent > div.main_detail.home > div.detail_cont > div.section_comm.section_defaultinfo > div > div:nth-child(2) > div > div > span"
            ));
            if (address.isEmpty() || address.matches(".*\\d{2,4}-\\d{2,4}-\\d{4}.*")) address = "정보 없음";

            // ✅ 사진 탭 클릭
            try {
                WebElement photoTab = driver.findElement(By.cssSelector("a[href='#photoview']"));
                photoTab.click();
                Thread.sleep(1500); // 사진 탭 로딩 대기
            } catch (Exception e) {
                System.out.println("사진 탭 클릭 실패: " + e.getMessage());
            }

            // ✅ 사진 크롤링
            List<String> photoUrls = new ArrayList<>();
            try {
                Thread.sleep(2000);
                List<WebElement> imgElements = driver.findElements(By.cssSelector(".view_photolist li a.link_photo img"));

                for (WebElement img : imgElements) {
                    String src = img.getAttribute("src");
                    if (src == null || src.isEmpty()) {
                        src = img.getAttribute("data-src"); // lazy-load 대응
                    }

                    if (src != null && !src.isEmpty()) {
                        if (src.startsWith("//")) src = "https:" + src;
                        photoUrls.add(src);
                    }
                }

                System.out.println("✅ 이미지 개수: " + photoUrls.size());
            } catch (Exception e) {
                System.out.println("❌ 이미지 크롤링 실패: " + e.getMessage());
            }

            result.put("status", status);
            result.put("openHour", openHour);
            result.put("address", address);
            result.put("photoUrls", photoUrls);

        } catch (Exception e) {
            result.put("error", "크롤링 실패: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return result;
    }

    private static String safeText(WebDriver driver, By selector) {
        try {
            WebElement el = driver.findElement(selector);
            return el.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
