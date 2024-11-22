package com.backend.allreva.rent.command.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class AdditionalInfo {
    @Column(nullable = false)
    private int recruitmentCount; //모집인원

    @Column(nullable = false)
    private Date eddate; //모집마감날짜

    @Column(nullable = false)
    private String chatUrl; //채팅방 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundType refundType; //환불 정책

    private String information; //안내 사항

    private boolean isClosed; //마감 여부
}
