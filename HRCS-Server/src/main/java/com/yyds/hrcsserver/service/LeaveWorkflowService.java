package com.yyds.hrcsserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.data.user.apply.ApplyDTO;
import com.yyds.hrcspojo.data.user.apply.ApplyPageQueryDTO;
import com.yyds.hrcspojo.data.user.apply.ApplyVO;

import java.util.List;

public interface LeaveWorkflowService {
    /** 提交申请 */
    Result submitApply(ApplyDTO dto);

    /** 审批申请 */
    Result approve(Long applyId, Long approverId, Integer action, String comment);

    /** 分页查询申请 */
    PageResult<ApplyVO> getApplyPage(ApplyPageQueryDTO dto);

    /** 获取待办列表 */
    List<ApplyVO> getTodoList(Long userId);

    /** 获取个人申请（分页） */
    IPage<ApplyVO> getOwnApply(Long userId, Integer pageNum, Integer pageSize);

    /** 查询部门申请（分页） */
    PageResult<ApplyVO> getDeptApplyPage(ApplyPageQueryDTO dto);
}