package com.yyds.hrcsserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.yyds.hrcspojo.data.user.notice.DaliyStateCountNoticeDTO;
import com.yyds.hrcspojo.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 21641
* @description 针对表【notice(公告表)】的数据库操作Service
* @createDate 2025-12-17 20:22:38
*/
public interface NoticeService extends IService<Notice> {

    boolean saveWithFile(Notice notice, MultipartFile file);

    boolean publishNotice(Long id);

    boolean deleteWithFile(Long id);

    IPage<Notice> listNotice(Integer pageNum, Integer pageSize, String keyword);

    boolean updateNotice(Long id);

    IPage<Notice> getNoticeByDepartmentId(Integer pageNum, Integer pageSize, Long id);

    Notice getDetail(Long id);

    List<DaliyStateCountNoticeDTO> getDaliyStateCountNoticeDTO();
}
