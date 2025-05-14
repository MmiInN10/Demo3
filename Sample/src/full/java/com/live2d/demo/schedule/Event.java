package com.live2d.demo.schedule;


/**
 * Google Calendar API를 통해 불러오거나 저장할 이벤트 정보를 담는 클래스
 * 일정 목록(RecyclerView 등)에 표시하거나, 수정/삭제 시 활용
 */
public class Event {

    // Google Calendar의 고유 이벤트 ID입니다. 수정/삭제 시 이 ID로 해당 이벤트를 식별합니다.
    private String eventId;

    // 일정 제목. 사용자에게 보여지는 주요 정보.
    private String title;

    // 일정 시작 시간. ISO 8601 형식의 문자열(ex: 2025-04-09T10:00:00Z)로 저장
    private String start;

    // 일정 종료 시간.
    private String end;

    // 생성자
    public Event(String eventId, String title, String start, String end) {
        this.eventId = eventId;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    // Getter 및 Setter 메서드들
    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getStart() {
        return start;
    }
    public void setStart(String start) {
        this.start = start;
    }
    public String getEnd() {
        return end;
    }
    public void setEnd(String end) {
        this.end = end;
    }
}
