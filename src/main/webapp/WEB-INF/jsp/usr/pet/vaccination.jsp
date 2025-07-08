<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>FullCalendar 백신 일정</title>

    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.js"></script>

    <style>
        #calendar {
            max-width: 800px;
            margin: 40px auto;
        }
    </style>
</head>
<body>

<h1>📅 백신 일정 캘린더</h1>
<div id="calendar"></div>

<!-- ✅ 백엔드 데이터 → JS로 파싱 -->
<script>
    const vaccinationEvents = JSON.parse('${eventsJson}');
</script>

<!-- ✅ FullCalendar 설정 -->
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const calendarEl = document.getElementById('calendar');

        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            events: vaccinationEvents
        });

        calendar.render();
    });
</script>

</body>
</html>
