package com.backend.allreva.rent.command.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Depositor {
    @Column(nullable = false)
    private String depositorName;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String phone;
}
