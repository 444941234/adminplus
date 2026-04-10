package com.adminplus.common.exception;

/**
 * 业务错误码枚举
 * <p>
 * 错误码规范：
 * - 格式：模块(2位) + 业务(2位) + 序号(2位)，共 6 位数字
 * - 模块编码：10=通用, 20=用户, 30=角色, 40=权限, 50=部门, 60=菜单,
 *             70=配置, 80=字典, 90=文件, 100=工作流, 110=日志
 * - 示例：200101 = 用户模块(20) + 用户业务(01) + 序号(01) = 用户名已存在
 *
 * @author AdminPlus
 * @since 2026-04-11
 */
public enum ErrorCode {

    // ========== 通用错误 10xxxx ==========
    UNKNOWN_ERROR(100001, "系统异常"),
    INVALID_PARAMETER(100002, "参数校验失败"),
    OPERATION_FAILED(100003, "操作失败"),

    // ========== 用户模块 20xxxx ==========
    USER_NOT_FOUND(200101, "用户不存在"),
    USER_USERNAME_EXISTS(200102, "用户名已存在"),
    USER_PASSWORD_WRONG(200103, "密码错误"),
    USER_DISABLED(200104, "用户已禁用"),
    USER_CANNOT_DELETE_SELF(200105, "不能删除自己"),
    USER_OLD_PASSWORD_WRONG(200106, "原密码错误"),

    // ========== 认证模块 21xxxx ==========
    AUTH_LOGIN_FAILED(210101, "登录失败"),
    AUTH_TOKEN_EXPIRED(210102, "Token已过期"),
    AUTH_TOKEN_INVALID(210103, "Token无效"),
    AUTH_CAPTCHA_WRONG(210104, "验证码错误"),
    AUTH_CAPTCHA_EXPIRED(210105, "验证码已过期"),
    AUTH_NOT_LOGIN(210106, "未登录"),
    AUTH_PERMISSION_DENIED(210107, "权限不足"),

    // ========== 角色模块 30xxxx ==========
    ROLE_NOT_FOUND(300101, "角色不存在"),
    ROLE_NAME_EXISTS(300102, "角色名称已存在"),
    ROLE_CODE_EXISTS(300103, "角色编码已存在"),
    ROLE_HAS_USERS(300104, "角色已关联用户，无法删除"),
    ROLE_IS_SYSTEM(300105, "系统角色无法操作"),

    // ========== 权限模块 40xxxx ==========
    PERMISSION_NOT_FOUND(400101, "权限不存在"),

    // ========== 部门模块 50xxxx ==========
    DEPT_NOT_FOUND(500101, "部门不存在"),
    DEPT_NAME_EXISTS(500102, "部门名称已存在"),
    DEPT_HAS_USERS(500103, "部门已关联用户，无法删除"),
    DEPT_HAS_CHILDREN(500104, "部门存在子部门，无法删除"),

    // ========== 菜单模块 60xxxx ==========
    MENU_NOT_FOUND(600101, "菜单不存在"),
    MENU_HAS_CHILDREN(600102, "菜单存在子菜单，无法删除"),

    // ========== 配置模块 70xxxx ==========
    CONFIG_NOT_FOUND(700101, "配置不存在"),
    CONFIG_KEY_EXISTS(700102, "配置键已存在"),
    CONFIG_GROUP_NOT_FOUND(700103, "配置组不存在"),

    // ========== 字典模块 80xxxx ==========
    DICT_NOT_FOUND(800101, "字典不存在"),
    DICT_ITEM_NOT_FOUND(800102, "字典项不存在"),

    // ========== 文件模块 90xxxx ==========
    FILE_NOT_FOUND(900101, "文件不存在"),
    FILE_UPLOAD_FAILED(900102, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(900103, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(900104, "文件大小超限"),
    FILE_VIRUS_FOUND(900105, "文件包含病毒"),

    // ========== 工作流模块 100xxx ==========
    WORKFLOW_NOT_FOUND(100101, "工作流不存在"),
    WORKFLOW_NODE_NOT_FOUND(100102, "工作流节点不存在"),
    WORKFLOW_DEFINITION_NOT_FOUND(100103, "工作流定义不存在"),
    WORKFLOW_STATUS_INVALID(100104, "工作流状态无效"),
    WORKFLOW_ALREADY_APPROVED(100105, "工作流已审批完成"),
    WORKFLOW_ALREADY_REJECTED(100106, "工作流已驳回"),
    WORKFLOW_NOT_RUNNING(100107, "工作流未运行"),
    WORKFLOW_NO_PREVIOUS_NODE(100108, "没有可回退的节点"),

    // ========== 日志模块 110xxx ==========
    LOG_NOT_FOUND(110101, "日志不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码查找枚举
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}