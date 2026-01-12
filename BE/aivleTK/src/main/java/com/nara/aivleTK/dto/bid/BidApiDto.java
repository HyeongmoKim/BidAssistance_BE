package com.nara.aivleTK.dto.bid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nara.aivleTK.domain.Bid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // ★ 중요: 정의하지 않은 필드는 무시함 (에러 방지)
public class BidApiDto {

    // === [ 1. JSON 필드 매핑 ] ===
    // 공공데이터포털 문서를 기반으로 매핑했습니다.

    @JsonProperty("bidNtceNo") // 입찰공고번호 (예: 20200100008)
    private String bidNtceNo;

    @JsonProperty("bidNtceOrd") // 입찰공고차수 (예: 00) - 고유 ID 만들 때 같이 쓰면 좋음
    private String bidNtceOrd;

    @JsonProperty("bidNtceNm") // 입찰공고명
    private String name;

    @JsonProperty("dminsttNm") // 수요기관명
    private String organization;

    @JsonProperty("bidBeginDt")
    private String startDateStr;

    @JsonProperty("bidClseDt")
    private String endDateStr;

    @JsonProperty("opengDt")
    private String openDateStr;

    @JsonProperty("presmptPrce") // 추정가격 (쉼표 포함 문자열일 수 있음)
    private String priceStr;

    @JsonProperty("ntceSpecDocUrl1") // 공고상세링크
    private String bidReportUrl;

    @JsonProperty("ntceSpecFileNm1")
    private String bidFileName;

    @JsonProperty("bidNtceDtlUrl")
    private String bidURL;

    // 지역 정보는 보통 '발주기관' 위치나 별도 필드(constPlceNm:공사현장)에 있는데,
    // 일단 수요기관명이나 공사현장명을 지역으로 쓸지 결정해야 합니다.
    // 여기서는 '공사현장'이 있다면 그걸 매핑합니다. 없으면 null.
    @JsonProperty("constPlceNm")
    private String region;


    // === [ 2. DTO -> Entity 변환 메서드 ] ===
    public Bid toEntity() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String realIdCombined = this.bidNtceNo + "-" + this.bidNtceOrd;

        return Bid.builder()
                // [왼쪽: Entity 변수명] = [오른쪽: DTO 값]
                .bidRealId(realIdCombined)
                .name(this.name)
                .organization(this.organization)
                .region(this.region)

                // ★ 작성하신 Entity 변수명이 bidURL 이므로 대소문자 정확히 매치
                .bidURL(this.bidURL)
                .bidFileName(this.bidFileName)
                .bidReportURL(this.bidReportUrl) // API에서 리포트 URL은 안 주는 경우가 많아 일단 뺌

                .startDate(parseDate(this.startDateStr, formatter))
                .endDate(parseDate(this.endDateStr, formatter))
                .openDate(parseDate(this.openDateStr, formatter))

                // ★ Entity가 BigInteger 타입이므로 그에 맞춰 변환
                .price(parsePriceBigInt(this.priceStr))
                .build();
    }

    // (추가) BigInteger 변환 도우미 메서드
    private java.math.BigInteger parsePriceBigInt(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return java.math.BigInteger.ZERO;
        try {
            // 쉼표 제거 후 BigInteger로 변환
            return new java.math.BigInteger(priceStr.replaceAll(",", "").trim());
        } catch (Exception e) {
            return java.math.BigInteger.ZERO;
        }
    }
    // 날짜 파싱 헬퍼 메서드
    private LocalDateTime parseDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            // 포맷이 다를 경우 로그를 찍거나 null 반환
            return null;
        }
    }

    // 가격 파싱 헬퍼 메서드 (쉼표 제거 등)
    private Long parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0L;
        try {
            // "12,345,000" -> "12345000" -> Long 변환
            return Long.parseLong(priceStr.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}