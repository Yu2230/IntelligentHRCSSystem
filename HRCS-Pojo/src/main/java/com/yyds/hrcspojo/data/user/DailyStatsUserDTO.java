package com.yyds.hrcspojo.data.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsUserDTO {
    private String date; // 格式: 2024-01-15
    private Long userCount = 0L;

}