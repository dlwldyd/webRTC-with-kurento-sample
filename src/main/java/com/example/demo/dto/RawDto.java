package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RawDto {
    private String type;

    private String from;

    private String to;

    private Ice iceCandidate;

    private Sdp sdp;
}
