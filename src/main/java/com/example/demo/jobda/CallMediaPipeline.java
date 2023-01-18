package com.example.demo.jobda;

import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

@Slf4j
public class CallMediaPipeline {

    private MediaPipeline pipeline;
    private WebRtcEndpoint beforeWebRtcEP;
    private WebRtcEndpoint afterWebRtcEP;

    public CallMediaPipeline(KurentoClient kurento) {
        try {
            this.pipeline = kurento.createMediaPipeline();
            this.beforeWebRtcEP = new WebRtcEndpoint.Builder(pipeline).build();
            this.afterWebRtcEP = new WebRtcEndpoint.Builder(pipeline).build();

            this.beforeWebRtcEP.connect(this.afterWebRtcEP);
            this.afterWebRtcEP.connect(this.beforeWebRtcEP);
        } catch (Throwable t) {
            log.info("파이프라인 연결 에러");
            if (this.pipeline != null) {
                pipeline.release();
            }
        }
    }

    public WebRtcEndpoint getBeforeWebRtcEP() {
        return beforeWebRtcEP;
    }

    public WebRtcEndpoint getAfterWebRtcEP() {
        return afterWebRtcEP;
    }

}
