package com.layjava.test.domain.bo;

import com.layjava.test.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户信息表
 *
 * @author chengliang4810
 * @since 2024-04-08
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class UserBo extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户编号
     */
    private String tenantId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 执法证号
     */
    private String certificateNumber;

    /**
     * 用户类型（sys_user系统用户）
     */
    private String userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 头像地址
     */
    private Long avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;


    /**
     * 登录日期
     */
    private LocalDateTime loginDate;

    /**
     * 备注
     */
    private String remark;

}
