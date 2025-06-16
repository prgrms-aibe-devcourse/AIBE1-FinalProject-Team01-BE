package kr.co.amateurs.server.domain.entity.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarketStatus {
    SELLING("판매중"),
    SOLD_OUT("판매완료"),
    RESERVED("예약중");

    private final String description;
}
