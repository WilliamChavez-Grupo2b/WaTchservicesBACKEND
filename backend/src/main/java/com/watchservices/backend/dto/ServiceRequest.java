package com.watchservices.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServiceRequest {
    private String operation;
    private List<Object> params;
}
