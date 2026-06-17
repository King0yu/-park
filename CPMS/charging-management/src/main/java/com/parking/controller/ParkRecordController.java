package com.parking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parking.common.R;
import com.parking.entity.ParkRecord;
import com.parking.service.ParkRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车记录控制器
 *
 * 提供 RESTful 风格的停车记录管理接口
 * 接口路径：/api/record
 *
 * 权限说明：
 * - super_admin (role=0): 完全控制所有停车记录
 * - admin (role=1): 完全控制所有停车记录
 * - user (role=2): 只能查看和操作自己的停车记录
 */
@RestController
@RequestMapping("/api/record")
@CrossOrigin(origins = "*")
public class ParkRecordController {

    @Autowired
    private ParkRecordService parkRecordService;

    /**
     * 从请求头获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

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
     * 检查登录状态
     */
    private R<?> checkLogin(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(401, "未登录或登录已过期");
        }
        return null;
    }

    /**
     * 新增停车记录
     *
     * POST /api/record
     *
     * 请求体示例：
     * {
     *   "userId": 1,
     *   "areaId": 1,
     *   "spaceId": 1,
     *   "startTime": "2024-01-01 10:00:00",
     *   "unitPrice": 5.00
     * }
     */
    @PostMapping
    public R<?> addRecord(@RequestBody ParkRecord parkRecord, HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        try {
            boolean success = parkRecordService.addRecord(parkRecord, currentUserId, currentRole);
            if (success) {
                return R.success("停车记录创建成功");
            }
            return R.error("停车记录创建失败");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 删除停车记录
     *
     * DELETE /api/record/{id}
     *
     * @param id 记录ID
     */
    @DeleteMapping("/{id}")
    public R<?> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        try {
            boolean success = parkRecordService.deleteRecord(id, currentUserId, currentRole);
            if (success) {
                return R.success("停车记录删除成功");
            }
            return R.error("停车记录删除失败");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 根据ID查询停车记录详情
     *
     * GET /api/record/{id}
     *
     * @param id 记录ID
     */
    @GetMapping("/{id}")
    public R<?> getRecordById(@PathVariable Long id, HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        ParkRecord record = parkRecordService.getRecordById(id, currentUserId, currentRole);
        if (record != null) {
            return R.success(record);
        }
        return R.error(404, "停车记录不存在或您没有权限查看此记录");
    }

    /**
     * 分页查询停车记录列表
     *
     * GET /api/record/page
     *
     * 请求参数：
     * - pageNum: 页码（默认1）
     * - pageSize: 每页数量（默认10）
     * - userId: 用户ID（可选，筛选指定用户的记录）
     * - spaceId: 停车位ID（可选，筛选指定停车位的记录）
     * - status: 记录状态（可选）
     * - startTime: 开始时间（可选）
     * - endTime: 结束时间（可选）
     * - recordNo: 记录编号（可选，模糊查询）
     * - username: 用户名（可选，模糊查询）
     * - spaceCode: 停车位编码（可选，模糊查询）
     */
    @GetMapping("/page")
    public R<?> getRecordListPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long spaceId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String recordNo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String spaceCode,
            HttpServletRequest request) {

        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        try {
            IPage<ParkRecord> page = parkRecordService.getRecordListPage(
                    pageNum, pageSize, userId, spaceId, status, startTime, endTime,
                    recordNo, username, spaceCode, currentUserId, currentRole);
            return R.success(page);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 结束停车（完成记录）
     *
     * PUT /api/record/finish/{id}
     *
     * 请求体示例：
     * {
     *   "parkingDuration": 2.5,
     *   "totalCost": 12.50
     * }
     *
     * @param id 记录ID
     */
    @PutMapping("/finish/{id}")
    public R<?> finishRecord(@PathVariable Long id,
                             @RequestBody FinishRecordRequest requestBody,
                             HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        // 获取请求参数
        BigDecimal parkingDuration = requestBody.getParkingDuration();
        BigDecimal totalCost = requestBody.getTotalCost();
        LocalDateTime endTime = LocalDateTime.now();

        try {
            boolean success = parkRecordService.endRecord(id, endTime, parkingDuration, totalCost, currentUserId, currentRole);
            if (success) {
                return R.success("停车结束，记录已完成");
            }
            return R.error("记录更新失败");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 取消记录
     *
     * PUT /api/record/cancel/{id}
     *
     * @param id 记录ID
     */
    @PutMapping("/cancel/{id}")
    public R<?> cancelRecord(@PathVariable Long id, HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        try {
            boolean success = parkRecordService.cancelRecord(id, currentUserId, currentRole);
            if (success) {
                return R.success("记录已取消");
            }
            return R.error("记录取消失败");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的停车记录列表
     *
     * GET /api/record/my
     */
    @GetMapping("/my")
    public R<?> getMyRecords(HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);

        try {
            return R.success(parkRecordService.getUserRecords(currentUserId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的进行中记录
     *
     * GET /api/record/my/active
     */
    @GetMapping("/my/active")
    public R<?> getMyActiveRecords(HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);

        try {
            return R.success(parkRecordService.getUserActiveRecords(currentUserId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的记录统计信息
     *
     * GET /api/record/my/statistics
     */
    @GetMapping("/my/statistics")
    public R<?> getMyStatistics(HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);

        try {
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalRecords", parkRecordService.countUserRecords(currentUserId));
            statistics.put("totalParkingDuration", parkRecordService.sumUserParkingDuration(currentUserId));
            statistics.put("totalAmount", parkRecordService.sumUserAmount(currentUserId));
            return R.success(statistics);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 自动计算并结束停车（完成记录）
     * 根据停车时长自动计算费用
     *
     * PUT /api/record/autoFinish/{id}
     *
     * 计算规则：
     * - 单价：5 元/小时
     * - 不足1小时向上取整
     *
     * @param id 记录ID
     */
    @PutMapping("/autoFinish/{id}")
    public R<?> autoFinishRecord(@PathVariable Long id, HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        Long currentUserId = getCurrentUserId(request);
        Integer currentRole = getCurrentUserRole(request);

        try {
            boolean success = parkRecordService.autoEndRecord(id, currentUserId, currentRole);
            if (success) {
                // 查询更新后的记录信息
                ParkRecord record = parkRecordService.getRecordById(id, currentUserId, currentRole);
                return R.success("停车结束，记录已完成", record);
            }
            return R.error("记录更新失败");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 模拟停车（创建停车记录）
     *
     * POST /api/record/simulateCreate
     *
     * 请求体示例：
     * {
     *   "spaceId": 1
     * }
     *
     * 参数说明：
     * - spaceId: 停车位ID（必填）
     * - userId: 从请求头X-User-Id获取（当前登录用户）
     *
     * 业务逻辑：
     * 1. 创建一条状态为「已占用(停车中)」的停车记录
     * 2. 自动记录停车开始时间
     * 3. 更新对应停车位状态为「已占用」
     */
    @PostMapping("/simulateCreate")
    public R<?> simulateCreateRecord(@RequestBody SimulateCreateRecordRequest requestBody, HttpServletRequest request) {
        // 检查登录状态
        R<?> loginCheck = checkLogin(request);
        if (loginCheck != null) {
            return loginCheck;
        }

        // 获取当前登录用户ID
        Long userId = getCurrentUserId(request);

        // 获取请求参数
        Long spaceId = requestBody.getSpaceId();
        String carNumber = requestBody.getCarNumber();

        // 调试日志
        System.out.println("模拟停车 - 用户ID: " + userId);
        System.out.println("模拟停车 - 停车位ID: " + spaceId);
        System.out.println("模拟停车 - 车牌号: " + carNumber);

        try {
            ParkRecord record = parkRecordService.simulateCreateRecord(userId, spaceId, carNumber);
            return R.success("停车成功，记录已创建", record);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 结束记录请求体
     */
    public static class FinishRecordRequest {
        private BigDecimal parkingDuration;
        private BigDecimal totalCost;

        public BigDecimal getParkingDuration() {
            return parkingDuration;
        }

        public void setParkingDuration(BigDecimal parkingDuration) {
            this.parkingDuration = parkingDuration;
        }

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
        }
    }

    /**
     * 模拟停车请求体
     */
    public static class SimulateCreateRecordRequest {
        private Long spaceId;
        private String carNumber;

        public Long getSpaceId() {
            return spaceId;
        }

        public void setSpaceId(Long spaceId) {
            this.spaceId = spaceId;
        }

        public String getCarNumber() {
            return carNumber;
        }

        public void setCarNumber(String carNumber) {
            this.carNumber = carNumber;
        }
    }
}