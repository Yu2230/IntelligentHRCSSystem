package com.yyds.hrcsserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.data.user.apply.ApplyDTO;
import com.yyds.hrcspojo.data.user.apply.ApplyPageQueryDTO;
import com.yyds.hrcspojo.data.user.apply.ApplyVO;
import com.yyds.hrcsserver.service.LeaveWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave/workflow")
@RequiredArgsConstructor
public class LeaveWorkflowController {

    private final LeaveWorkflowService leaveWorkflowService;

    /** 提交请假申请 */
    @PostMapping("/apply")
    public Result apply(@RequestBody ApplyDTO dto) {
        return leaveWorkflowService.submitApply(dto);
    }

    /** 审批申请 */
    @PostMapping("/approve/{applyId}")
    public Result approve(@PathVariable Long applyId,
                          @RequestParam Long approverId,
                          @RequestParam Integer action,
                          @RequestParam(required = false) String comment) {
        return leaveWorkflowService.approve(applyId, approverId, action, comment);
    }

    /** 分页查询所有申请（支持条件筛选） */
    @GetMapping("/page")
    public Result<PageResult<ApplyVO>> page(ApplyPageQueryDTO dto) {
        return Result.getSuccessResult(leaveWorkflowService.getApplyPage(dto));
    }

    /** 获取用户待办任务列表 */
    @GetMapping("/todo/{userId}")
    public Result<List<ApplyVO>> getTodoList(@PathVariable Long userId) {
        return Result.getSuccessResult(leaveWorkflowService.getTodoList(userId));
    }

    /** 获取个人所有申请（分页） */
    @GetMapping("/all/{userId}")
    public Result<IPage<ApplyVO>> getAllApply(@PathVariable Long userId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.getSuccessResult(leaveWorkflowService.getOwnApply(userId, pageNum, pageSize));
    }

    /** 分类查询：获取已审核通过的申请（分页） */
    @GetMapping("/approved")
    public Result<PageResult<ApplyVO>> getApprovedApplications(ApplyPageQueryDTO dto) {
        dto.setState(1); // 已通过状态
        return Result.getSuccessResult(leaveWorkflowService.getApplyPage(dto));
    }

    /** 分类查询：获取待审核的申请（分页） */
    @GetMapping("/pending")
    public Result<PageResult<ApplyVO>> getPendingApplications(ApplyPageQueryDTO dto) {
        dto.setState(0); // 审批中状态
        return Result.getSuccessResult(leaveWorkflowService.getApplyPage(dto));
    }

    /** 查询单个部门的全部申请（分页） */
    @GetMapping("/dept/{deptId}/all")
    public Result<PageResult<ApplyVO>> getDeptApplications(@PathVariable Long deptId,
                                                           ApplyPageQueryDTO dto) {
        dto.setDepartmentId(deptId);
        return Result.getSuccessResult(leaveWorkflowService.getDeptApplyPage(dto));
    }

    /** 查询所有待审核的申请（管理员视角，分页） */
    @GetMapping("/pending/all")
    public Result<PageResult<ApplyVO>> getAllPendingApplications(ApplyPageQueryDTO dto) {
        dto.setState(0);
        return Result.getSuccessResult(leaveWorkflowService.getApplyPage(dto));
    }
}