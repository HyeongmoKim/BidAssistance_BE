package com.nara.aivleTK.dto.chatBot;

import com.nara.aivleTK.domain.Bid;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PythonChatRequest {
        private String type;
        private String query;
        private Object payload;
        private String thread_id;

        // 편의용 생성자 (질문만 보낼 때)
        public PythonChatRequest(String query, String thread_id) {
            this.type = "query";
            this.query = query;
            this.thread_id = thread_id;
            this.payload = null;
        }
    }

