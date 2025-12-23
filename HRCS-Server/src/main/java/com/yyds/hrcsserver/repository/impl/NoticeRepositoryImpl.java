package com.yyds.hrcsserver.repository.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yyds.hrcspojo.entity.Notice;
import com.yyds.hrcsserver.mapper.NoticeMapper;
import com.yyds.hrcsserver.repository.NoticeRepository;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class NoticeRepositoryImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeRepository {
    @Override
    public IPage<Notice> listNotice(Integer pageNum, Integer pageSize, String keyword) {
        return lambdaQuery()
                .like(StringUtil.isNotBlank(keyword), Notice::getTitle, keyword)  // 模糊查询
                .orderByDesc(Notice::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    @Override
    public IPage<Notice> getNoticeByDepartmentId(Integer pageNum, Integer pageSize, Long id) {
        return lambdaQuery()
                .eq(Notice::getDepartmentId, id)
                .eq(Notice::getType,2)
                .orderByDesc(Notice::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    @Override
    public int getNoticeCount() {
        return lambdaQuery()
                .count()
                .intValue();
    }

    @Override
    public int getNoticeCountForCurrentMonth() {
        // 获取本月第一天 00:00:00
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 获取本月最后一天 23:59:59
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        return lambdaQuery()
                .ge(Notice::getUpdateTime, startOfMonth)
                .le(Notice::getUpdateTime, endOfMonth)
                .count()
                .intValue();
    }

    @Override
    public List<Map<String, Object>> getDailyUserCountNotice(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return getBaseMapper().selectMaps(
                Wrappers.<Notice>query()  // ✅ 改用 query() 而不是 lambdaQuery()
                        .ge("publish_time", startDateTime)
                        .le("publish_time", endDateTime)
                        .select("DATE(publish_time) as date", "COUNT(*) as count")  // ✅ SELECT 子句
                        .groupBy("DATE(publish_time)")  // ✅ GROUP BY 子句
                        .orderByAsc("DATE(publish_time)") // ✅ ORDER BY 子句
        );
    }
}
