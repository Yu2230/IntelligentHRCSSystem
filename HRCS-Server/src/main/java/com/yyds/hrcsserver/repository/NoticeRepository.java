package com.yyds.hrcsserver.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyds.hrcspojo.entity.Notice;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface NoticeRepository extends IService<Notice> {
    IPage<Notice> listNotice(Integer pageNum, Integer pageSize, String keyword);

    IPage<Notice> getNoticeByDepartmentId(Integer pageNum,  Integer pageSize, Long id);

    int getNoticeCount();

    int getNoticeCountForCurrentMonth();

    List<Map<String, Object>> getDailyUserCountNotice(LocalDate startDate, LocalDate today);
}
