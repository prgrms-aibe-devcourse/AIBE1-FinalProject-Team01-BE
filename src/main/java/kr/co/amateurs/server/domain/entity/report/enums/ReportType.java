package kr.co.amateurs.server.domain.entity.report.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    BAD_WORDS("욕설/비방"),
    SPAM("스팸/광고"),
    SEXUAL_CONTENT("음란물/선전성"),
    PERSONAL_INFO("개인정보 노출"),
    FLOODING("도배성"),
    OTHER("기타");

    private final String description;
}
