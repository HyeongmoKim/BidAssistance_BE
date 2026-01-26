package com.nara.aivleTK.dto.chatBot;

import com.nara.aivleTK.domain.Bid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PythonChatRequest {
    private String query;
    private String thread_id;
}
