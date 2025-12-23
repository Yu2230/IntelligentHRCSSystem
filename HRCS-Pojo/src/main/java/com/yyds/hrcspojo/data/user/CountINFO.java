package com.yyds.hrcspojo.data.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountINFO {
    private int userCount;
    private int userCountMonth;
    private int noticeCount;
    private int noticeCountMonth;
}
