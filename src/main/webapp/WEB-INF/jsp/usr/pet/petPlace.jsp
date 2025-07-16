<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %> <%-- fn:length, fn:contains 등 --%>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>내 주변 펫 장소</title>
  <script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=dfd275f49b78960a0458d6f6294cbde2&libraries=services"></script>
  <style>
    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      font-family: 'SUIT', sans-serif;
      display: flex;
      height: 100vh;
      background: #fef6d9; /* 밝은 베이지 */
    }

    /* 지도 영역 */
    #map {
      width: 70%;
      height: 100%;
      border-right: 2px solid #f9d368; /* 연노랑 포인트 */
    }

    /* 사이드바 */
    #sidebar {
      width: 30%;
      padding: 16px;
      overflow-y: auto;
      background: white; /* 연한 베이지 배경 */
    }

    /* 필터 버튼 영역 */
    #filterBtns {
      margin-bottom: 12px;
    }
    #filterBtns button {
      background: #ffefb0;
      border: none;
      border-radius: 6px;
      padding: 6px 10px;
      margin-right: 6px;
      margin-bottom: 6px;
      font-weight: bold;
      cursor: pointer;
      transition: all 0.2s;
    }
    #filterBtns button:hover {
      background: #ffe38a;
    }

    /* 장소 리스트 */
    .place-item {
      padding: 12px;
      margin-bottom: 10px;
      background: #fff;
      border-left: 5px solid #f9d368;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      box-shadow: 0 0 10px #aaa;
    }
    .place-item:hover {
      background: #fff9db;
    }
    .selected {
      background: #fff6c1;
      outline: 2px solid #f2c700;
    }

    /* 상세 정보 */
    .place-details {
      background: white;
      border: 1px solid #ffe38a;
      border-radius: 6px;
      padding: 10px;
      margin-top: 8px;
    }

    /* 즐겨찾기 버튼 */
    .fav-btn {
      position: absolute;
      top: 10px;
      right: 10px;
      background: none;
      border: none;
      font-size: 18px;
      cursor: pointer;
    }

    /* 장소 유형 라벨 */
    .type-label {
      display: inline-block;
      font-size: 12px;
      background-color: #fff4c2;
      color: #7a5100;
      border-radius: 4px;
      padding: 2px 6px;
      margin-left: 6px;
      font-weight: bold;
      vertical-align: middle;
    }

    /* 팝업창 */
    .popup {
      position: fixed;
      top: 5%;
      right: 31%;
      width: 400px;
      min-height: 90%;
      overflow-y: auto;
      background-color: white;
      border: 2px solid #f9d368;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 0 10px #aaa;
      z-index: 999;
      display: none;
    }
    .popup.visible {
      display: block;
    }
    .popup .close-btn {
      float: right;
      background: #fff4c2;
      border: none;
      font-size: 16px;
      padding: 4px 10px;
      cursor: pointer;
      border-radius: 4px;
    }

    /* 사진 그리드 */
    .photo-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
      margin-top: 10px;
    }
    .photo-grid img {
      width: 100%;
      border-radius: 6px;
      box-shadow: 0 0 5px rgba(0,0,0,0.1);
    }

    /* 더보기 버튼 */
    #loadMoreBtn {
      background: #fff1b3;
      border: none;
      padding: 6px 10px;
      border-radius: 5px;
      cursor: pointer;
      margin: 10px auto;
      display: block;
    }

    /* 로딩 스피너 */
    .spinner {
      width: 48px;
      height: 48px;
      border: 5px solid #ffefb0;
      border-top: 5px solid #ffc400;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

  </style>
</head>
<body>
<div id="map"></div>

<div id="sidebar">
  <h3>📍 내 주변 펫 장소</h3>
  <div style="margin-bottom: 12px;">
    <input type="text" id="customKeyword" placeholder="검색어 입력 (예: 코코펫카페)"
           style="width: 70%; padding: 6px; border-radius: 6px; border: 1px solid #ccc;"
           onkeypress="if(event.key === 'Enter') searchCustomKeyword()">
    <button onclick="searchCustomKeyword()"
            style="padding: 6px 10px; border-radius: 6px; background: #fff1b3; font-weight: bold; border: none;">
      🔍 검색
    </button>
  </div>

  <div id="filterBtns">
    <button onclick="searchPlaces('애견용품')">🐶 애견용품</button>
    <button onclick="searchPlaces('동물병원')">🏥 동물병원</button>
    <button onclick="searchPlaces('애견카페')">☕ 애견카페</button>
    <button onclick="searchPlaces('공원')">🐾 공원</button>
    <button onclick="searchPlaces('펫호텔')">🏨 펫호텔</button>
    <button onclick="showFavoritesOnly()">🌟 즐겨찾기만 보기</button>
  </div>
  <div id="placeList"></div>
</div>
  <div id="placePopup" class="popup hidden">
    <button class="close-btn" onclick="closePopup()">❌닫기</button>

    <div id="popupLoading" style="text-align:center; margin-top:30px;">
      <div class="spinner"></div>
      <p>정보를 불러오는 중입니다...</p>
    </div>

    <div id="popupContent" style="display: none;">
      <h2><p><strong>📍</strong> <span id="popupName"></span></p></h2>
      <p><strong>운영 상태:</strong> <span id="popupStatus"></span></p>
      <p><strong>영업 시간:</strong> <span id="popupHour"></span></p>
      <p><strong>전화번호:</strong> <span id="popupPhone"></span></p>
      <p><strong>주소:</strong> <span id="popupAddress"></span></p>

      <h3>📸 장소 사진</h3>
      <div id="popupPhotos" class="photo-grid"></div>
      <button id="loadMoreBtn" onclick="loadMorePhotos()">더보기</button>
    </div>
  </div>


<script>
  const memberId = ${memberId};

  const favoriteNames = new Set([
    <c:forEach var="name" items="${favoriteNames}" varStatus="status">
    "${name}"<c:if test="${!status.last}">,</c:if>
    </c:forEach>
  ]);

  const favoritePlaces = [
    <c:forEach var="p" items="${favoriteplaces}" varStatus="status">
    {
      name: "${p.name}",
      address: "${p.address}",
      phone: "${p.phone}",
      type: "${p.type}",
      mapUrl: "${p.mapUrl}"
    }<c:if test="${!status.last}">,</c:if>
    </c:forEach>
  ];



  let map, currentLocation, markers = [], searchResults = [], currentType = "";

  const placeListEl = document.getElementById("placeList");
  const pawMarkerImage = new kakao.maps.MarkerImage(
          "/img/paw-marker2.png",
          new kakao.maps.Size(64, 64),
          { offset: new kakao.maps.Point(32, 64) }
  );

  window.onload = function () {
    map = new kakao.maps.Map(document.getElementById("map"), {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 3
    });

    navigator.geolocation.getCurrentPosition(function(pos) {
      currentLocation = new kakao.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
      map.setCenter(currentLocation);
      searchPlaces("애견용품");
    }, function() {
      alert("📍 위치 정보를 가져올 수 없습니다.");
    });
  };

  function clearMarkers() {
    markers.forEach(function(m) { m.setMap(null); });
    markers = [];
  }

  function searchPlaces(keyword) {
    currentType = keyword;
    const ps = new kakao.maps.services.Places();
    ps.keywordSearch(keyword, function(data, status) {
      if (status !== kakao.maps.services.Status.OK) {
        placeListEl.innerHTML = "<p>🔍 장소를 찾을 수 없습니다.</p>";
        return;
      }
      searchResults = data;
      renderPlaceList();
    }, { location: currentLocation, radius: 5000 });
  }

  function renderPlaceList() {
    clearMarkers();
    placeListEl.innerHTML = "";

    searchResults.forEach(function(place, idx) {
      const pos = new kakao.maps.LatLng(place.y, place.x);
      const isFav = favoriteNames.has(place.place_name);

      const marker = new kakao.maps.Marker({
        map: map,
        position: pos,
        title: place.place_name,
        image: pawMarkerImage
      });
      markers.push(marker);

      const item = document.createElement("div");
      item.className = "place-item";
      item.id = "place-" + idx;
      item.innerHTML =
              "<strong>" + place.place_name + "</strong><br>" +
              (place.road_address_name || place.address_name) + "<br>" +
              "📞 " + (place.phone || "없음") +
              "<button class='fav-btn' onclick='toggleFavorite(event, " + idx + ")'>" + (isFav ? "❤️" : "🤍") + "</button>";

      item.onclick = function () {
        map.panTo(pos);
        openPopup(place);
      };

      placeListEl.appendChild(item);

      kakao.maps.event.addListener(marker, "click", function () {
        map.panTo(pos);
        openPopup(place);
        item.scrollIntoView({ behavior: "smooth", block: "center" });
      });
    });
  }


  function toggleFavorite(event, index) {
    event.stopPropagation();
    console.log("하트 클릭됨: " + index);
    const place = searchResults[index];
    const isFav = favoriteNames.has(place.place_name);
    const btn = event.target;

    const typeInfo = currentType || "미정";


    const params =
            "memberId=" + memberId +
            "&type=" + encodeURIComponent(typeInfo) + // ✅ 여기에 '미정' 처리 포함됨
            "&name=" + encodeURIComponent(place.place_name) +
            "&address=" + encodeURIComponent(place.road_address_name || place.address_name) +
            "&phone=" + encodeURIComponent(place.phone || "없음") +
            "&mapUrl=" + encodeURIComponent(place.place_url);


    fetch('/usr/pet/recommend/toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params
    })
            .then(function(res) { return res.text(); })
            .then(function(result) {
              if (result === "added") {
                favoriteNames.add(place.place_name);
                btn.textContent = "❤️";
              } else if (result === "removed") {
                favoriteNames.delete(place.place_name);
                btn.textContent = "🤍";
              }
            });
  }

  function focusPlace(index) {
    const item = document.getElementById("place-" + index);
    const existingDetail = document.getElementById("details-" + index);

    if (item.classList.contains("selected")) {
      item.classList.remove("selected");
      if (existingDetail) existingDetail.remove();
      return;
    }

    const prevSelected = document.querySelector(".place-item.selected");
    if (prevSelected) {
      prevSelected.classList.remove("selected");
      const prevDetail = prevSelected.nextElementSibling;
      if (prevDetail && prevDetail.classList.contains("place-details")) prevDetail.remove();
    }

    const place = searchResults[index];
    item.classList.add("selected");

    const detail = document.createElement("div");
    detail.className = "place-details";
    detail.id = "details-" + index;
    detail.innerHTML =
            "<strong>" + place.place_name + "</strong><br>" +
            "<p>🏠 " + (place.road_address_name || place.address_name) + "</p>" +
            "<p>📞 " + (place.phone || "없음") + "</p>" +
            "<p><a href='" + place.place_url + "' target='_blank'>🔗 카카오에서 보기</a></p>";

    item.insertAdjacentElement("afterend", detail);
  }

  function showFavoritesOnly() {
    clearMarkers();
    placeListEl.innerHTML = "";

    fetch('/usr/pet/recommend/list?memberId=' + memberId)
            .then(res => res.json())
            .then(favorites => {
              if (!favorites.length) {
                placeListEl.innerHTML = "<p style='padding: 20px; text-align: center; color: #999;'>즐겨찾기한 장소가 없습니다.</p>";
                return;
              }

              favorites.forEach((place, idx) => {
                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(place.address, function(result, status) {
                  if (status === kakao.maps.services.Status.OK) {
                    const pos = new kakao.maps.LatLng(result[0].y, result[0].x);

                    const marker = new kakao.maps.Marker({
                      map: map,
                      position: pos,
                      title: place.name,
                      image: pawMarkerImage
                    });
                    markers.push(marker);

                    const item = document.createElement("div");
                    item.className = "place-item";
                    item.id = "fav-place-" + idx;
                    item.innerHTML =
                            "<strong>" + place.name + "</strong>" +
                            "<span class='type-label'>" + place.type + "</span><br>" +
                            place.address + "<br>" +
                            "📞 " + place.phone +
                            "<button class='fav-btn' onclick='removeFavorite(event, \"" + place.name + "\")'>❤️</button>";

                    item.onclick = function () {
                      map.panTo(pos);
                      place.place_url = place.mapUrl;  // ✅ Kakao API와 동일하게 필드 맞춤
                      place.place_name = place.name;
                      openPopup(place);
                    };

                    placeListEl.appendChild(item);

                    kakao.maps.event.addListener(marker, "click", function () {
                      item.scrollIntoView({ behavior: "smooth", block: "center" });
                      map.panTo(pos);
                      place.place_url = place.mapUrl; // ✨ 추가
                      place.place_name = place.name;
                      openPopup(place);
                    });
                  }
                });
              });
            });
  }


  function toggleFavoriteDetail(item, place) {
    const detailEl = item.nextElementSibling;

    if (item.classList.contains("selected")) {
      item.classList.remove("selected");
      if (detailEl && detailEl.classList.contains("place-details")) detailEl.remove();
      return;
    }

    document.querySelectorAll(".place-item").forEach(el => el.classList.remove("selected"));
    document.querySelectorAll(".place-details").forEach(el => el.remove());

    const detail = document.createElement("div");
    detail.className = "place-details";
    detail.innerHTML =
            "<strong>" + place.name + "</strong><br>" +
            "<p>🏠 " + place.address + "</p>" +
            "<p>📞 " + place.phone + "</p>" +
            "<p><a href='" + place.mapUrl + "' target='_blank'>🔗 카카오에서 보기</a></p>";

    item.insertAdjacentElement("afterend", detail);
    item.classList.add("selected");
  }



  function removeFavorite(event, name) {
    event.stopPropagation();

    const place = favoritePlaces.find(p => p.name === name);
    const type = place && place.type ? place.type : "미정";

    const params = "memberId=" + memberId +
            "&name=" + encodeURIComponent(name) +
            "&type=" + encodeURIComponent(type);

    fetch('/usr/pet/recommend/toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params
    })
            .then(res => res.text())
            .then(result => {
              if (result === "removed") {
                // 1. 즐겨찾기 목록에서 삭제
                const itemEl = event.target.closest(".place-item");
                const detailEl = itemEl.nextElementSibling;
                if (detailEl && detailEl.classList.contains("place-details")) {
                  detailEl.remove();
                }
                itemEl.remove();

                // 2. favoriteNames Set에서도 삭제
                favoriteNames.delete(name);

                // 3. 전체 장소 리스트에서 해당 장소가 있을 경우 하트를 업데이트
                const allItems = document.querySelectorAll(".place-item");
                allItems.forEach(el => {
                  const strong = el.querySelector("strong");
                  const btn = el.querySelector(".fav-btn");
                  if (strong && btn && strong.textContent === name) {
                    btn.textContent = "🤍";
                  }
                });

                // 4. 즐겨찾기 모두 삭제되면 안내 문구 표시
                const remainingFavorites = document.querySelectorAll(".place-item");
                if (remainingFavorites.length === 0) {
                  placeListEl.innerHTML =
                          "<p style='padding: 20px; text-align: center; color: #999;'>즐겨찾기한 장소가 없습니다.</p>";
                }
              }
            });
  }




  function searchCustomKeyword() {
    const keyword = document.getElementById("customKeyword").value.trim();
    if (!keyword) {
      alert("검색어를 입력해주세요!");
      return;
    }
    currentType = keyword; // 즐겨찾기 등록용 type에도 반영
    const ps = new kakao.maps.services.Places();
    ps.keywordSearch(keyword, function(data, status) {
      if (status !== kakao.maps.services.Status.OK) {
        placeListEl.innerHTML = "<p>❌ 검색 결과가 없습니다.</p>";
        return;
      }
      searchResults = data;
      renderPlaceList();
    }, { location: currentLocation, radius: 5000 });
  }

  let currentPhotoList = [];
  let currentPhotoIndex = 0;

  function openPopup(place) {
    const popup = document.getElementById("placePopup");
    const popupLoading = document.getElementById("popupLoading");
    const popupContent = document.getElementById("popupContent");

    // 초기 상태 설정
    popupLoading.style.display = "block";
    popupContent.style.display = "none";
    popup.classList.add("visible");
    popup.classList.remove("hidden");

    fetch("/usr/pet/test?url=" + encodeURIComponent(place.place_url))
            .then(function (res) { return res.json(); })
            .then(function (data) {
              // 👉 데이터 표시
              document.getElementById("popupName").textContent = place.place_name;
              document.getElementById("popupStatus").textContent = data.status || "정보 없음";
              document.getElementById("popupHour").textContent = data.openHour || "정보 없음";
              document.getElementById("popupAddress").textContent =
                      (!data.address || data.address === "정보 없음")
                              ? (place.road_address_name || place.address_name || "주소 없음")
                              : data.address;
              document.getElementById("popupPhone").textContent = place.phone || "없음";

              // 👉 사진 표시
              const grid = document.getElementById("popupPhotos");
              grid.innerHTML = "";

              currentPhotoList = data.photoUrls || [];
              currentPhotoIndex = 0;

              if (currentPhotoList.length === 0) {
                // 사진이 없는 경우 텍스트 출력
                grid.innerHTML = "<p style='color:#888; font-size:14px;'>사진이 없습니다.</p>";
                document.getElementById("loadMoreBtn").style.display = "none";
              } else {
                const count = Math.min(4, currentPhotoList.length);
                for (let i = 0; i < count; i++) {
                  const img = document.createElement("img");
                  img.src = currentPhotoList[i];
                  grid.appendChild(img);
                }
                currentPhotoIndex = count;

                const moreBtn = document.getElementById("loadMoreBtn");
                moreBtn.style.display = currentPhotoIndex >= currentPhotoList.length ? "none" : "block";
              }

              // ✅ 로딩 끝, 내용 표시
              popupLoading.style.display = "none";
              popupContent.style.display = "block";
            })
            .catch(function (err) {
              console.error("❌ 장소 상세 정보 요청 실패:", err);
              popupLoading.innerHTML = "<p style='color:red;'>정보를 불러오는 데 실패했습니다.</p>";
            });
  }



  function loadMorePhotos() {
    var grid = document.getElementById("popupPhotos");
    var count = Math.min(4, currentPhotoList.length - currentPhotoIndex);
    for (var i = 0; i < count; i++) {
      var img = document.createElement("img");
      img.src = currentPhotoList[currentPhotoIndex];
      grid.appendChild(img);
      currentPhotoIndex++;
    }

    if (currentPhotoIndex >= currentPhotoList.length) {
      document.getElementById("loadMoreBtn").style.display = "none";
    }
  }

  function closePopup() {
    const popup = document.getElementById("placePopup");
    const popupLoading = document.getElementById("popupLoading");
    const popupContent = document.getElementById("popupContent");

    popup.classList.remove("visible");
    popupContent.style.display = "none";
    popupLoading.style.display = "none";
    document.getElementById("popupPhotos").innerHTML = "";
  }


</script>
</body>
</html>
