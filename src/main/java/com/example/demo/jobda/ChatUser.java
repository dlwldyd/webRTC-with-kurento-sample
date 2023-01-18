package com.example.demo.jobda;

import lombok.*;
import org.kurento.client.WebRtcEndpoint;

@Getter
@Setter
@Builder
public class ChatUser {

    private String id;

    private String offer;

    private String answer;

    private WebRtcEndpoint webRtcEndpoint;

}
