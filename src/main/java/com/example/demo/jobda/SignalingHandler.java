package com.example.demo.jobda;

import org.springframework.stereotype.Component;

@Component
public class SignalingHandler {

    public String handleOffer(CallMediaPipeline pipeline, String offer) {
        return pipeline.getAfterWebRtcEP().processOffer(offer);
    }

//    public String handleAnswer(CallMediaPipeline pipeline, String answerSdp) {
//        Sdp sdp = new Sdp("answer", answerSdp);
//        new Dto("answer", )
//    }
}
