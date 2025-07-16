<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>장소 상세 팝업</title>
    <style>
        .sidebar {
            position: fixed;
            top: 0;
            right: -420px;
            width: 400px;
            height: 100%;
            background-color: #fff8f8;
            box-shadow: -3px 0 10px rgba(0, 0, 0, 0.2);
            padding: 20px;
            overflow-y: auto;
            z-index: 1000;
            transition: right 0.3s ease;
        }
        .sidebar.open {
            right: 0;
        }
        .close-btn {
            position: absolute;
            top: 10px;
            right: 10px;
            background: #ffdddd;
            border: none;
            padding: 5px 10px;
            cursor: pointer;
        }
        .open-btn {
            position: fixed;
            top: 20px;
            right: 20px;
            background: #f2f2f2;
            padding: 8px 12px;
            border-radius: 8px;
            border: 1px solid #ccc;
            cursor: pointer;
        }
        .photo-grid {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }
        .photo-grid img {
            width: calc(50% - 4px);
            height: auto;
            border-radius: 6px;
        }
        #loadMoreBtn {
            display: block;
            margin: 15px auto;
            padding: 8px 16px;
            background-color: #ffd8d8;
            border: none;
            border-radius: 8px;
            cursor: pointer;
        }
    </style>
</head>
<body>

<!-- 🔘 열기 버튼 -->
<button class="open-btn" onclick="openSidebar()">📍 상세 보기</button>

<!-- 📍 상세 정보 사이드 패널 -->
<div id="detailSidebar" class="sidebar">
    <button class="close-btn" onclick="closeSidebar()">❌ 닫기</button>

    <h2>📍 상세 정보</h2>
    <p><strong>운영 상태:</strong> ${status}</p>
    <p><strong>영업 시간:</strong> ${openHour}</p>
    <p><strong>주소:</strong> ${address}</p>

    <h3>📸 장소 사진</h3>
    <div id="photoContainer" class="photo-grid"></div>
    <button id="loadMoreBtn" onclick="loadMore()">더보기</button>

    <script>
        const photos = [
            <c:forEach var="photo" items="${photoUrls}" varStatus="i">
            "${photo}"<c:if test="${!i.last}">,</c:if>
            </c:forEach>
        ];
        let visibleCount = 0;
        const photoContainer = document.getElementById("photoContainer");
        const loadMoreBtn = document.getElementById("loadMoreBtn");

        function loadMore() {
            const nextPhotos = photos.slice(visibleCount, visibleCount + 4);
            nextPhotos.forEach(src => {
                const img = document.createElement("img");
                img.src = src;
                photoContainer.appendChild(img);
            });
            visibleCount += 4;

            if (visibleCount >= photos.length) {
                loadMoreBtn.style.display = "none";
            }
        }

        function openSidebar() {
            document.getElementById("detailSidebar").classList.add("open");
            if (visibleCount === 0) loadMore(); // 최초 로딩
        }

        function closeSidebar() {
            document.getElementById("detailSidebar").classList.remove("open");
        }
    </script>
</div>

</body>
</html>
