package com.parking.controller;

import com.parking.common.R;
import com.parking.entity.ParkConfig;
import com.parking.service.ParkConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置控制器
 *
 * 提供系统级别的配置查询与修改接口
 * 接口路径：/api/system
 *
 * 权限说明：
 * - super_admin (role=0): 超级管理员可修改系统配置
 * - 其他角色：仅可查看，不可修改
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemConfigController {

    @Autowired
    private ParkConfigService parkConfigService;

    /**
     * 角色常量
     */
    private static final int ROLE_SUPER_ADMIN = 0;

    /**
     * 从请求头获取当前登录用户角色
     */
    private Integer getCurrentUserRole(HttpServletRequest request) {
        String roleStr = request.getHeader("X-User-Role");
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                return Integer.parseInt(roleStr);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    /**
     * 获取系统配置
     *
     * GET /api/system/parkConfig
     */
    @GetMapping("/parkConfig")
    public R<?> getParkConfig() {
        ParkConfig config = parkConfigService.getConfig();
        if (config == null) {
            return R.error(404, "系统配置不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", config.getId());
        data.put("systemName", config.getSystemName());
        data.put("copyright", config.getCopyright());
        data.put("createTime", config.getCreateTime());
        data.put("updateTime", config.getUpdateTime());

        return R.success(data);
    }

    /**
     * 更新系统配置
     * 仅超级管理员(role=0)可操作
     *
     * PUT /api/system/parkConfig
     */
    @PutMapping("/parkConfig")
    public R<?> updateParkConfig(@RequestBody ParkConfig parkConfig, HttpServletRequest request) {
        Integer currentRole = getCurrentUserRole(request);
        if (currentRole == null || currentRole != ROLE_SUPER_ADMIN) {
            return R.error(403, "无权限访问");
        }

        // 参数校验
        if (parkConfig.getSystemName() == null || parkConfig.getSystemName().trim().isEmpty()) {
            return R.error(400, "系统名称不能为空");
        }
        if (parkConfig.getCopyright() == null || parkConfig.getCopyright().trim().isEmpty()) {
            return R.error(400, "版权标语不能为空");
        }

        boolean success = parkConfigService.updateConfig(parkConfig);
        if (success) {
            return R.success("系统配置更新成功");
        }
        return R.error("系统配置更新失败");
    }
}
