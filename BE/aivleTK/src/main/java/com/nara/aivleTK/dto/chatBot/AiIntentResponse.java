package com.nara.aivleTK.dto.chatBot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AiIntentResponse {
    private String region;
    private String keyword;
    private String type;
    private Long minPrice;
    private Long maxPrice;
    private String agency;
}
