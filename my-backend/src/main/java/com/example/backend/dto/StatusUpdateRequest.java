package com.example.backend.dto;

import com.example.backend.domain.Business;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusUpdateRequest {
    private Business.BusinessStatus status;
}