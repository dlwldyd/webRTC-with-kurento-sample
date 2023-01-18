package com.example.demo.controller;

import com.example.demo.dto.Dto;
import com.example.demo.dto.Ice;
import com.example.demo.dto.RawDto;
import com.example.demo.dto.Sdp;
import com.example.demo.jobda.CallMediaPipeline;
import com.example.demo.jobda.ChatUser;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class MsgController {

    private final SimpMessagingTemplate template;

    private ChatUser before;

    private ChatUser after;

    private final CallMediaPipeline pipeline;

    public MsgController(KurentoClient kurento, SimpMessagingTemplate template) {
        this.template = template;
        this.pipeline = new CallMediaPipeline(kurento);
    }

    @MessageMapping("/no")
    @SendTo("/subs/no")
    public String no(String msg) {
        return msg;
    }

    @MessageMapping("/signaling")
    public void message(Dto dto) {
        if (dto.getType().equals("offer")) {
            if (before == null) {
                before = ChatUser.builder()
                        .id(dto.getFrom())
                        .offer(dto.getSdp())
                        .webRtcEndpoint(pipeline.getBeforeWebRtcEP())
                        .build();
            } else if (after == null) {
                after = ChatUser.builder()
                        .id(dto.getFrom())
                        .offer(dto.getSdp())
                        .webRtcEndpoint(pipeline.getAfterWebRtcEP())
                        .build();

                addIceCandidateFoundListener(pipeline);

                //answer 생성
                String beforeAnswerSdp = before.getWebRtcEndpoint().processOffer(before.getOffer());
                before.setAnswer(beforeAnswerSdp);
                Dto beforeAnswer = new Dto("answer", null, before.getId(), null, beforeAnswerSdp);
                template.convertAndSend("/subs/signaling", beforeAnswer);
                before.getWebRtcEndpoint().gatherCandidates();

                String afterAnswerSdp = after.getWebRtcEndpoint().processOffer(after.getOffer());
                after.setAnswer(afterAnswerSdp);
                Dto afterAnswer = new Dto("answer", null, after.getId(), null, afterAnswerSdp);
                template.convertAndSend("/subs/signaling", afterAnswer);
                after.getWebRtcEndpoint().gatherCandidates();

            }
        } else if (dto.getType().equals("ice")) {
            if (dto.getIceCandidate() != null) {
                Ice candidate = dto.getIceCandidate();
                IceCandidate iceCandidate = new IceCandidate(candidate.getCandidate(), candidate.getSdpMid(), candidate.getSdpMLineIndex());
                if (before != null && dto.getFrom().equals(before.getId())) {
                    before.getWebRtcEndpoint().addIceCandidate(iceCandidate);
                } else if(after != null && dto.getFrom().equals(after.getId())) {
                    after.getWebRtcEndpoint().addIceCandidate(iceCandidate);
                }
            }
        }
    }

    @MessageMapping("/raw")
    public void raw(RawDto dto) {
        if (dto.getType().equals("offer")) {
            if (before == null) {
                before = ChatUser.builder()
                        .id(dto.getFrom())
                        .offer(dto.getSdp().getSdp())
                        .webRtcEndpoint(pipeline.getBeforeWebRtcEP())
                        .build();
            } else if (after == null) {
                after = ChatUser.builder()
                        .id(dto.getFrom())
                        .offer(dto.getSdp().getSdp())
                        .webRtcEndpoint(pipeline.getAfterWebRtcEP())
                        .build();

//                addIceCandidateFoundListener(pipeline);

                //answer 생성
                before.getWebRtcEndpoint().addIceCandidateFoundListener(event -> {
                    log.info("=========== before : {} =============", before.getId());
                    JsonObject response = new JsonObject();
                    response.addProperty("type", "ice");
                    response.addProperty("to", before.getId());
                    response.add("iceCandidate", JsonUtils.toJsonObject(event.getCandidate()));
                    template.convertAndSend("/subs/raw", response.toString());
                });
                String beforeAnswerSdp = before.getWebRtcEndpoint().processOffer(before.getOffer());
                before.setAnswer(beforeAnswerSdp);
                RawDto beforeAnswer = new RawDto("answer", null, before.getId(), null, new Sdp("answer", beforeAnswerSdp));
                template.convertAndSend("/subs/raw", beforeAnswer);
                before.getWebRtcEndpoint().gatherCandidates();

                after.getWebRtcEndpoint().addIceCandidateFoundListener(event -> {
                    log.info("=========== after : {} =============", after.getId());
                    JsonObject response = new JsonObject();
                    response.addProperty("type", "ice");
                    response.addProperty("to", after.getId());
                    response.add("iceCandidate", JsonUtils.toJsonObject(event.getCandidate()));
                    template.convertAndSend("/subs/raw", response.toString());
                });
                String afterAnswerSdp = after.getWebRtcEndpoint().processOffer(after.getOffer());
                after.setAnswer(afterAnswerSdp);
                RawDto afterAnswer = new RawDto("answer", null, after.getId(), null, new Sdp("answer", afterAnswerSdp));
                template.convertAndSend("/subs/raw", afterAnswer);
                after.getWebRtcEndpoint().gatherCandidates();
            }
        } else if (dto.getType().equals("ice")) {
            if (dto.getIceCandidate() != null) {
                Ice candidate = dto.getIceCandidate();
                IceCandidate iceCandidate = new IceCandidate(candidate.getCandidate(), candidate.getSdpMid(), candidate.getSdpMLineIndex());
                if (before != null && dto.getFrom().equals(before.getId())) {
                    before.getWebRtcEndpoint().addIceCandidate(iceCandidate);
                } else if(after != null && dto.getFrom().equals(after.getId())) {
                    after.getWebRtcEndpoint().addIceCandidate(iceCandidate);
                }
            }
        }
    }

    private void addIceCandidateFoundListener(CallMediaPipeline pipeline) {
        pipeline.getAfterWebRtcEP().addIceCandidateFoundListener(event -> {
            log.info("=========== after : {} =============", after.getId());
            JsonObject response = new JsonObject();
            response.addProperty("type", "ice");
            response.addProperty("to", after.getId());
            response.add("iceCandidate", JsonUtils.toJsonObject(event.getCandidate()));
            template.convertAndSend("/subs/signaling", response.toString());
        });
        pipeline.getBeforeWebRtcEP().addIceCandidateFoundListener(event -> {
            log.info("=========== before : {} =============", before.getId());
            JsonObject response = new JsonObject();
            response.addProperty("type", "ice");
            response.addProperty("to", before.getId());
            response.add("iceCandidate", JsonUtils.toJsonObject(event.getCandidate()));
            template.convertAndSend("/subs/signaling", response.toString());
        });
    }
}
