<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>내 주변 펫 장소 찾기</title>
  <script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=dfd275f49b78960a0458d6f6294cbde2&libraries=services"></script>
  <style>
    #map {
      width: 100%; height: 500px;
      border: 2px solid #f3c2d2;
      border-radius: 12px;
    }
    #filterBtns {
      margin: 16px 0; display: flex; flex-wrap: wrap; gap: 8px;
    }
    #filterBtns button {
      background: #ffe0e0; border: none; border-radius: 8px;
      padding: 6px 12px; cursor: pointer; font-weight: bold;
    }
    #placesList { margin-top: 10px; padding: 0; list-style: none; }
    #placesList li {
      margin-bottom: 10px; padding: 10px; background: #fff8f8;
      border-left: 6px solid #ffc4c4; border-radius: 6px;
      box-shadow: 1px 1px 5px rgba(0,0,0,0.05); cursor: pointer;
    }
    #placesList li:hover { background: #ffe9e9; }
    .infoCard {
      font-size: 14px; padding: 10px; background: #fff4f4;
      border-radius: 10px; box-shadow: 2px 2px 8px rgba(0,0,0,0.2);
      width: 220px;
    }
  </style>
</head>
<body>
<h2>🐾 내 주변 펫 장소</h2>
<div id="filterBtns">
  <button onclick="searchPlaces('애견용품')">🐶 애견용품</button>
  <button onclick="searchPlaces('동물병원')">🏥 동물병원</button>
  <button onclick="searchPlaces('애견카페')">☕ 애견카페</button>
  <button onclick="searchPlaces('펫샵')">🐾 펫샵</button>
  <button onclick="searchPlaces('펫호텔')">🏨 펫호텔</button>
</div>
<div id="map"></div>
<ul id="placesList"></ul>
<script>
  let map, markers = [], searchResults = [], currentLocation;
  let currentPage = 1, itemsPerPage = 5;
  const pawMarkerImage = new kakao.maps.MarkerImage('/img/paw-marker2.png', new kakao.maps.Size(64, 64), { offset: new kakao.maps.Point(32, 64) });
  const infowindow = new kakao.maps.InfoWindow({ zIndex: 3 });

  window.onload = function () {
    map = new kakao.maps.Map(document.getElementById('map'), {
      center: new kakao.maps.LatLng(37.5665, 126.9780), level: 3
    });
    navigator.geolocation.getCurrentPosition(pos => {
      currentLocation = new kakao.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
      map.setCenter(currentLocation);
      new kakao.maps.Circle({
        center: currentLocation, radius: 10,
        strokeWeight: 2, strokeColor: '#ff69b4', strokeOpacity: 0.8,
        fillColor: '#ffb6c1', fillOpacity: 0.7, map: map
      });
      searchPlaces('애견용품');
    }, () => alert("📍 위치 정보를 가져올 수 없습니다."));
  };

  function clearMarkers() { markers.forEach(m => m.setMap(null)); markers = []; }

  function searchPlaces(keyword) {
    if (!currentLocation) return;
    clearMarkers();
    document.getElementById('placesList').innerHTML = '';
    const ps = new kakao.maps.services.Places();
    ps.keywordSearch(keyword, function (data, status) {
      if (status !== kakao.maps.services.Status.OK || data.length === 0) {
        document.getElementById('placesList').innerHTML = "<li style='color:gray;text-align:center;'>🔍 해당 장소가 없습니다.</li>";
        return;
      }
      searchResults = data;
      currentPage = 1;
      renderList();
    }, { location: currentLocation, radius: 5000 });
  }

  function renderList() {
    clearMarkers();
    const listEl = document.getElementById('placesList');
    listEl.innerHTML = '';
    const start = (currentPage - 1) * itemsPerPage;
    const pageItems = searchResults.slice(start, start + itemsPerPage);
    pageItems.forEach(place => {
      const pos = new kakao.maps.LatLng(place.y, place.x);
      const marker = new kakao.maps.Marker({ map: map, position: pos, title: place.place_name, image: pawMarkerImage });
      markers.push(marker);
      kakao.maps.event.addListener(marker, 'click', () => {
        infowindow.setContent("<div class='infoCard'><strong>" + place.place_name + "</strong><br>" + (place.road_address_name || place.address_name) + "<br>☎ " + (place.phone || "없음") + "</div>");
        infowindow.open(map, marker);
      });
      const li = document.createElement('li');
      li.innerHTML = "<strong>" + place.place_name + "</strong><br>" + (place.road_address_name || place.address_name) + "<br>📞 " + (place.phone || "없음");
      li.onclick = () => { map.panTo(pos); kakao.maps.event.trigger(marker, 'click'); };
      listEl.appendChild(li);
    });
    renderPagination();
  }

  function renderPagination() {
    const listEl = document.getElementById('placesList');
    const totalPages = Math.ceil(searchResults.length / itemsPerPage);
    const pagination = document.createElement('div');
    pagination.style.textAlign = 'center'; pagination.style.marginTop = '12px';
    for (let i = 1; i <= totalPages; i++) {
      const pageBtn = document.createElement('button');
      pageBtn.innerText = i;
      Object.assign(pageBtn.style, {
        margin: '0 6px', padding: '6px 12px', border: 'none', borderRadius: '12px',
        backgroundColor: (i === currentPage) ? '#ffb6c1' : '#ffeef2',
        color: (i === currentPage) ? 'white' : '#333', fontWeight: 'bold', cursor: 'pointer'
      });
      pageBtn.onmouseover = () => pageBtn.style.transform = 'scale(1.1)';
      pageBtn.onmouseout = () => pageBtn.style.transform = 'scale(1)';
      pageBtn.onclick = () => { currentPage = i; renderList(); };
      pagination.appendChild(pageBtn);
    }
    listEl.appendChild(pagination);
  }
</script>
</body>
</html>