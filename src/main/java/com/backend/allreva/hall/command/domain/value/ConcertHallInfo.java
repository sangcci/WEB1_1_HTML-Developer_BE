package com.backend.allreva.hall.command.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class ConcertHallInfo {

    @Column(name = "concert_hall_fclty_name")
    private String fcltyName;
    @Column(name = "concert_hall_prfplc_name")
    private String prfplcName;
    @Column(name = "concert_hall_seat_scale")
    private Integer seatScale;
}