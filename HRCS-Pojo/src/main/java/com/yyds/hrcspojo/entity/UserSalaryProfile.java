package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工薪酬档案表
 * @TableName user_salary_profile
 */
@TableName(value ="user_salary_profile")
public class UserSalaryProfile {
    /**
     * 档案ID
     */
    @TableId
    private Long id;

    /**
     * 员工ID
     */
    private Long userId;

    /**
     * 基础工资
     */
    private BigDecimal baseSalary;

    /**
     * 社保基数
     */
    private BigDecimal socialSecurityBase;

    /**
     * 公积金基数
     */
    private BigDecimal housingFundBase;

    /**
     * 生效日期
     */
    private Date effectiveDate;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 档案ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 档案ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 员工ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 员工ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 基础工资
     */
    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    /**
     * 基础工资
     */
    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    /**
     * 社保基数
     */
    public BigDecimal getSocialSecurityBase() {
        return socialSecurityBase;
    }

    /**
     * 社保基数
     */
    public void setSocialSecurityBase(BigDecimal socialSecurityBase) {
        this.socialSecurityBase = socialSecurityBase;
    }

    /**
     * 公积金基数
     */
    public BigDecimal getHousingFundBase() {
        return housingFundBase;
    }

    /**
     * 公积金基数
     */
    public void setHousingFundBase(BigDecimal housingFundBase) {
        this.housingFundBase = housingFundBase;
    }

    /**
     * 生效日期
     */
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * 生效日期
     */
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * 创建人
     */
    public Long getCreateBy() {
        return createBy;
    }

    /**
     * 创建人
     */
    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    /**
     * 更新人
     */
    public Long getUpdateBy() {
        return updateBy;
    }

    /**
     * 更新人
     */
    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    /**
     * 
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        UserSalaryProfile other = (UserSalaryProfile) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getBaseSalary() == null ? other.getBaseSalary() == null : this.getBaseSalary().equals(other.getBaseSalary()))
            && (this.getSocialSecurityBase() == null ? other.getSocialSecurityBase() == null : this.getSocialSecurityBase().equals(other.getSocialSecurityBase()))
            && (this.getHousingFundBase() == null ? other.getHousingFundBase() == null : this.getHousingFundBase().equals(other.getHousingFundBase()))
            && (this.getEffectiveDate() == null ? other.getEffectiveDate() == null : this.getEffectiveDate().equals(other.getEffectiveDate()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getBaseSalary() == null) ? 0 : getBaseSalary().hashCode());
        result = prime * result + ((getSocialSecurityBase() == null) ? 0 : getSocialSecurityBase().hashCode());
        result = prime * result + ((getHousingFundBase() == null) ? 0 : getHousingFundBase().hashCode());
        result = prime * result + ((getEffectiveDate() == null) ? 0 : getEffectiveDate().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", baseSalary=").append(baseSalary);
        sb.append(", socialSecurityBase=").append(socialSecurityBase);
        sb.append(", housingFundBase=").append(housingFundBase);
        sb.append(", effectiveDate=").append(effectiveDate);
        sb.append(", createBy=").append(createBy);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}