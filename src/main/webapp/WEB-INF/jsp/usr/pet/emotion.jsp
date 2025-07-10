<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>감정분석 팝업창</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body {
            background: #f5f5f5;
            font-family: 'SUIT', sans-serif;
            margin: 0;
            padding: 40px;
        }

        .popup-container {
            display: flex;
            background: #ffffff;
            border-radius: 20px;
            max-width: 900px;
            margin: auto;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        .left-box {
            width: 50%;
            padding: 40px;
            background-color: #fffde9;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .left-box img {
            max-width: 100%;
            border-radius: 12px;
            border: 3px solid #f3df87;
        }

        .right-box {
            width: 50%;
            padding: 40px;
            background-color: #f9f9f9;
        }

        h2 {
            font-size: 22px;
            color: #333;
            margin-bottom: 20px;
        }

        .btn-submit {
            background: #f3df87;
            padding: 10px 20px;
            border: none;
            border-radius: 6px;
            font-size: 16px;
            cursor: pointer;
            margin-bottom: 20px;
        }

        .species-select {
            display: flex;
            gap: 10px;
            margin-bottom: 10px;
        }

        .species-btn {
            padding: 8px 14px;
            border: 1px solid #ccc;
            border-radius: 6px;
            background-color: #eee;
            cursor: pointer;
            font-weight: bold;
        }

        .species-btn.active {
            background-color: #f3df87;
            border-color: #f3df87;
        }

        #resultText {
            margin-top: 20px;
            font-size: 18px;
            font-weight: bold;
            text-align: center;
        }

        canvas {
            max-width: 100%;
            margin-top: 20px;
        }
    </style>
</head>
<body>

<div class="popup-container">
    <!-- 왼쪽 이미지 -->
    <div class="left-box">
        <div id="preview"></div>
    </div>

    <!-- 오른쪽 결과 -->
    <div class="right-box">
        <h2>반려동물 감정 분석 결과</h2>

        <!-- ✅ 종 선택 버튼 -->
        <div class="species-select">
            <button type="button" class="species-btn active" data-species="고양이">🐱 고양이</button>
            <button type="button" class="species-btn" data-species="강아지">🐶 강아지</button>
        </div>

        <!-- ✅ 분석 폼 -->
        <form id="analysisForm" enctype="multipart/form-data">
            <input type="hidden" name="petId" value="${param.petId}" />
            <input type="hidden" name="species" id="speciesInput" value="고양이" />
            <input type="file" name="imageFile" id="imageFile" accept="image/*" required>
            <button type="submit" class="btn-submit">감정 보기</button>
        </form>

        <div id="resultText"></div>
        <canvas id="emotionChart" width="300" height="300"></canvas>
    </div>
</div>

<script>
    let emotionChart = null;

    // ✅ 종 선택 버튼 동작
    $(".species-btn").on("click", function () {
        $(".species-btn").removeClass("active");
        $(this).addClass("active");
        const selected = $(this).data("species");
        $("#speciesInput").val(selected);
    });

    // 이미지 미리보기
    $("#imageFile").on("change", function () {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                $("#preview").html('<img src="' + e.target.result + '"/>');
            };
            reader.readAsDataURL(file);
        }
    });

    // 분석 요청
    $("#analysisForm").on("submit", function (e) {
        e.preventDefault();
        const formData = new FormData(this);
        $.ajax({
            type: "POST",
            url: "/usr/pet/analysis/do",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                console.log("응답:", data);
                $("#preview").html("<img src='" + data.imagePath + "' />");

                const probs = data.probabilities;
                const labels = Object.keys(probs);
                const values = Object.values(probs).map(v => parseFloat(v.toFixed(2)));

                // 가장 높은 감정
                let maxIdx = values.indexOf(Math.max(...values));
                let maxLabel = labels[maxIdx];
                let maxValue = values[maxIdx];

                // 감정 이모지 + 한글 표시
                const labelMap = {
                    "happy": "😊 행복",
                    "relaxed": "😌 평온",
                    "angry": "😠 화남",
                    "sad": "😿 슬픔",
                    "scared": "😨 두려움"
                };
                let displayLabel = labelMap[maxLabel] || maxLabel;

                // 결과 텍스트 출력 (가장 높은 감정만)
                $("#resultText").html("가장 높은 감정: " + displayLabel + " (" + maxValue + "%)");

                // 차트 출력
                if (emotionChart) emotionChart.destroy();
                const ctx = document.getElementById('emotionChart').getContext('2d');
                emotionChart = new Chart(ctx, {
                    type: 'pie',
                    data: {
                        labels: labels,
                        datasets: [{
                            data: values,
                            backgroundColor: ['#f9c74f', '#90be6d', '#f8961e', '#43aa8b', '#577590'],
                        }]
                    },
                    options: {
                        plugins: {
                            legend: { position: 'bottom' },
                            title: { display: true, text: '감정 비율 분석' }
                        }
                    }
                });
            },
            error: function () {
                alert("감정 분석 실패!");
            }
        });
    });
</script>

</body>
</html>
