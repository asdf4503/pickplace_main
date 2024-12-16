package com.pickplace.server;

import com.pickplace.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OpenAIController {

    @Autowired
    private OpenAIService openAIService;

    @GetMapping("/")
    public String showForm() {
        return "askForm";
    }

    @PostMapping("/ask")
    public String askPredefinedQuestion(Model model) {
        String predefinedQuestion = "국내 모든 지역 중 하나의 지역을 골라주고, '--도 --시' 형식으로 정확한 지역명과 함께 그 지역에 대한 1줄 정도의 간단한 소개를 알려줘. 출력 형식은 '지역명- 설명'으로 해줘";
        String answer = openAIService.askQuestion(predefinedQuestion);

        // 답변에서 지역명과 설명 분리 (보다 안정적으로 처리)
        String location = "";
        String description = "";

        // 특정 형식이 올바르게 전달되었는지 확인
        if (answer.contains("- ")) {
            String[] parts = answer.split("- ", 2); // 첫 번째 '-'만 기준으로 분리
            location = parts[0].trim();
            description = parts.length > 1 ? parts[1].trim() : "설명이 없습니다.";
        } else {
            location = answer.trim(); // '-'가 없으면 전체를 지역으로 간주
            description = "설명이 제대로 전달되지 않았습니다.";
        }

        model.addAttribute("question", predefinedQuestion);
        model.addAttribute("answer", answer); // 원본 답변
        model.addAttribute("location", location);
        model.addAttribute("description", description);

        return "answer";
    }


    //answer의 답변으로 여행 지역 검색
    @PostMapping("/Schedule")
    public String ScheduleQuestion(@RequestParam("location") String location, Model model) {
        // 여행 질문 생성
        String scheduleQuestion = "나는 " + location + "에 1박 2일 여행을 가려고 하고 컨셉은 힐링여행이야." + location + "에 대한 대표 관광지로 여행 일정을 만들어주는데 설명 없이 장소만 정확하게 나오게 해줘. 그리고 번호빼고 '- '의 형식으로 구분하여 제공해줘";
        String itinerary = openAIService.askQuestion(scheduleQuestion); // 여행 일정 생성
        model.addAttribute("scheduleQuestion", scheduleQuestion);
        model.addAttribute("itinerary", itinerary);

        // 전달된 location을 모델에 추가
        model.addAttribute("location", location);

        // 장소별 좌표 얻기
        List<Map<String, Double>> coordinates = new ArrayList<>(); // 위도, 경도를 담을 리스트

        // 장소 배열 분리 및 필터링
        String[] places = itinerary.split("- "); // '-' 구분자로 분리
        List<String> filteredPlaces = new ArrayList<>();
        for (String place : places) {
            String trimmedPlace = place.trim();
            if (!trimmedPlace.isEmpty()) { // 공백 또는 null 값 제외
                filteredPlaces.add(trimmedPlace);
            }
        }

        // 디버깅용 출력
        System.out.println("필터링된 장소 리스트:");
        for (String place : filteredPlaces) {
            System.out.println(place);
        }

        // 장소별 좌표 획득
        for (String place : filteredPlaces) {
            Map<String, Double> latLng = getLatLng(place.trim()); // Geocoding API 호출
            if (latLng != null) {
                coordinates.add(latLng); // 좌표 리스트에 추가
                System.out.println("장소: " + place + ", 위도: " + latLng.get("lat") + ", 경도: " + latLng.get("lng"));
            } else {
                System.out.println("장소: " + place + "의 좌표를 찾을 수 없습니다.");
            }
        }

        // 필터링된 장소 목록 및 좌표를 모델에 추가
        model.addAttribute("placesList", filteredPlaces); // 필터링된 장소 목록
        model.addAttribute("coordinates", coordinates); // 좌표 리스트를 모델에 추가

        return "Schedule"; // 일정 페이지로 이동
    }


    //구글맵 api를 사용하여 여행 지역의 위도와 경도를 찾는 함수
    private Map<String, Double> getLatLng(String place) {
        if (place == null || place.trim().isEmpty()) {
            return null; // 빈 값 또는 null일 경우 좌표를 찾지 않음
        }

        String apiKey = "AIzaSyAiPpnAj0XSUTz5QaALJ4iYWA0xEaGWIr8"; // 구글 API 키
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + place + "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JSONObject json = new JSONObject(response);
        if (json.getJSONArray("results").length() > 0) {
            JSONObject location = json.getJSONArray("results").getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location");
            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");
            return Map.of("lat", lat, "lng", lng);
        }
        return null;
    }

    @GetMapping("/totalMap")
    public String showMap() {
        return "Map";
    }
}