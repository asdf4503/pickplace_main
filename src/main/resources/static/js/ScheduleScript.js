function initMap() {
    // Thymeleaf에서 전달된 coordinates JSON 데이터
    const coordinates = JSON.parse(document.getElementById("coordinates").textContent);
    console.log("Coordinates received:", coordinates); // 데이터가 잘 전달되는지 확인

    // 데이터 검증
    if (!coordinates || coordinates.length === 0) {
        console.error("No coordinates found.");
        return;
    }

    // 지도 초기화 (첫 번째 좌표를 중심으로 설정)
    const map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: coordinates[0].lat, lng: coordinates[0].lng },
        zoom: 10,
    });

    // 마커를 배열에 추가하고, 그 좌표들을 Polyline으로 이어주기
    const path = []; // 선을 그릴 좌표 배열
    coordinates.forEach(function (location, index) {
        // 각 장소에 마커 추가
        new google.maps.Marker({
            position: { lat: location.lat, lng: location.lng },
            map: map,
            title: "장소 " + (index + 1), // 마커에 제목 추가 (선택 사항)
        });

        // 선을 그리기 위한 좌표 추가
        path.push(new google.maps.LatLng(location.lat, location.lng));
    });

    // 마커들을 순차적으로 이어주는 Polyline 생성
    const polyline = new google.maps.Polyline({
        path: path,
        geodesic: true,
        strokeColor: "#FF0000", // 선의 색상
        strokeOpacity: 1.0, // 선의 불투명도
        strokeWeight: 2, // 선의 두께
    });

    polyline.setMap(map); // 지도의 Polyline에 추가
}
