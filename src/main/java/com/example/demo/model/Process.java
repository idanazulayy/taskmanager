package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Process implements Serializable {
    private String processName;
    private UUID processId;
    private int timeOfLiveInSeconds;
}
