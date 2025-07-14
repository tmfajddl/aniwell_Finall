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
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: 'SUIT', sans-serif;
      display: flex;
      height: 100vh;
    }
    #map {
      width: 70%;
      height: 100%;
      border-right: 2px solid #ffc0cb;
    }
    #sidebar {
      width: 30%;
      padding: 16px;
      overflow-y: auto;
      background: #fff8f8;
    }
    #filterBtns {
      margin-bottom: 12px;
    }
    #filterBtns button {
      background: #ffdede;
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
      background: #ffc4c4;
    }
    .place-item {
      padding: 12px;
      margin-bottom: 10px;
      background: #fff;
      border-left: 5px solid #ffb6c1;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
    }
    .place-item:hover {
      background: #ffeef0;
    }
    .selected {
      background: #ffe0e9;
      outline: 2px solid #ff7f9f;
    }
    .place-details {
      background: #fff0f4;
      border: 1px solid #ffc2d2;
      border-radius: 6px;
      padding: 10px;
      margin-top: 8px;
    }
    .fav-btn {
      position: absolute;
      top: 10px;
      right: 10px;
      background: none;
      border: none;
      font-size: 18px;
      cursor: pointer;
    }
    .type-label {
      display: inline-block;
      font-size: 12px;
      background-color: #ffd4da;
      color: #a03340;
      border-radius: 4px;
      padding: 2px 6px;
      margin-left: 6px;
      font-weight: bold;
      vertical-align: middle;
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
            style="padding: 6px 10px; border-radius: 6px; background: #ffc2d2; font-weight: bold; border: none;">
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
        focusPlace(idx);
        map.panTo(pos);
      };

      placeListEl.appendChild(item);

      kakao.maps.event.addListener(marker, "click", function () {
        focusPlace(idx);
        map.panTo(pos);
        document.getElementById("place-" + idx).scrollIntoView({ behavior: "smooth", block: "center" });
      });
    });
  }

  function toggleFavorite(event, index) {
    event.stopPropagation();
    console.log("하트 클릭됨: " + index);
    const place = searchResults[index];
    const isFav = favoriteNames.has(place.place_name);
    const btn = event.target;

    const typeInfo = "미정";

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
                      toggleFavoriteDetail(item, place);
                      map.panTo(pos);
                    };

                    placeListEl.appendChild(item);

                    kakao.maps.event.addListener(marker, "click", function () {
                      item.scrollIntoView({ behavior: "smooth", block: "center" });
                      item.click(); // 👉 리스트 아이템 클릭 효과 그대로 재활용
                      map.panTo(pos);
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
    const type = place && place.type ? place.type : "미정"; // ✅ null 방지

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
                location.reload();
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



</script>
</body>
</html>
