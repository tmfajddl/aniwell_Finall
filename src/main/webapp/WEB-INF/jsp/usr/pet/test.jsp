<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>내 주변 반려동물 장소 찾기</title>

  <!-- ✅ defer 꼭 추가!! -->
  <script defer src="//dapi.kakao.com/v2/maps/sdk.js?appkey=2d0a4915b9a73b2430b2094ff3ecfc23&libraries=services"></script>

  <style>
    #map {
      width: 100%;
      height: 500px;
    }
    #placesList {
      margin-top: 10px;
      list-style: none;
      padding: 0;
    }
    #placesList li {
      margin-bottom: 8px;
      padding: 4px 8px;
      background: #f9f9f9;
      border-left: 4px solid #ffc0cb;
    }
  </style>
</head>
<body>

<h2>📍 내 주변 반려동물 장소</h2>
<div id="map"></div>
<ul id="placesList"></ul>

<!-- ✅ window.onload 안에 전체 코드 넣기 -->
<script>
  window.onload = function () {
    // kakao 객체가 존재하는지 확인
    if (typeof kakao === "undefined") {
      alert("❗ 카카오맵 API가 아직 로드되지 않았습니다. 인터넷 연결이나 도메인 등록을 확인하세요.");
      return;
    }

    navigator.geolocation.getCurrentPosition(function (position) {
      const lat = position.coords.latitude;
      const lon = position.coords.longitude;

      const mapContainer = document.getElementById('map');
      const mapOption = {
        center: new kakao.maps.LatLng(lat, lon),
        level: 3
      };
      const map = new kakao.maps.Map(mapContainer, mapOption);

      const marker = new kakao.maps.Marker({
        map: map,
        position: new kakao.maps.LatLng(lat, lon),
        title: "현재 위치"
      });

      const ps = new kakao.maps.services.Places();
      const keywords = ['애견용품', '펫샵', '동물병원', '애견카페', '펫호텔'];
      const listEl = document.getElementById('placesList');

      keywords.forEach(function (keyword) {
        ps.keywordSearch(keyword, function (data, status) {
          if (status === kakao.maps.services.Status.OK) {
            data.forEach(function (place) {
              const placePosition = new kakao.maps.LatLng(place.y, place.x);
              new kakao.maps.Marker({
                map: map,
                position: placePosition,
                title: place.place_name
              });

              const li = document.createElement('li');
              li.innerHTML = "<strong>" + place.place_name + "</strong><br>" + (place.road_address_name || place.address_name);
              listEl.appendChild(li);
            });
          }
        }, {
          location: new kakao.maps.LatLng(lat, lon),
          radius: 5000
        });
      });

    }, function () {
      alert("위치 정보를 가져올 수 없습니다. 위치 권한을 허용해주세요.");
    });
  };
</script>

</body>
</html>
