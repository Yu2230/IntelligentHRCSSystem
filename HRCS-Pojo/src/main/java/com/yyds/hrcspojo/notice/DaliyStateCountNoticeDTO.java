package com.yyds.hrcspojo.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.PortResolverImpl;

@Data
@Builder
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class DaliyStateCountNoticeDTO {
    private String date; // 格式: 2024-01-15
    private Long userCount = 0L;
}
