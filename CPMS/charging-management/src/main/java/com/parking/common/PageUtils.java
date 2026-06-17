package com.parking.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页工具类 - 小区停车场管理系统
 *
 * 功能说明：
 * 封装分页查询的结果，包括当前页数据、总记录数、每页大小等信息
 *
 * 使用场景：
 * 后端返回给前端的分页数据格式
 *
 * 返回格式示例：
 * {
 *   "code": 200,
 *   "msg": "操作成功",
 *   "data": {
 *     "total": 100,        // 总记录数
 *     "pageSize": 10,      // 每页显示条数
 *     "currentPage": 1,    // 当前页码
 *     "totalPages": 10,    // 总页数
 *     "records": [...]     // 当前页的数据列表
 *   }
 * }
 *
 * @param <T> 泛型：分页数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageUtils<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     * 数据库中符合条件的总数据条数
     */
    private long total;

    /**
     * 每页显示的记录数
     * 即pageSize参数
     */
    private long pageSize;

    /**
     * 当前页码
     * 从1开始计数
     */
    private long currentPage;

    /**
     * 总页数
     * 根据total和pageSize计算得出
     */
    private long totalPages;

    /**
     * 当前页的数据列表
     */
    private List<T> records;

    /**
     * 构造分页结果
     *
     * @param total      总记录数
     * @param pageSize   每页大小
     * @param currentPage 当前页码
     * @param records    当前页数据列表
     */
    public PageUtils(long total, long pageSize, long currentPage, List<T> records) {
        this.total = total;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.records = records;
        // 计算总页数：如果总记录数能被每页大小整除，则为整除结果，否则加1
        this.totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
    }

    /**
     * 判断是否有下一页
     *
     * @return true-有下一页，false-没有下一页
     */
    public boolean hasNext() {
        return this.currentPage < this.totalPages;
    }

    /**
     * 判断是否有上一页
     *
     * @return true-有上一页，false-没有上一页
     */
    public boolean hasPrevious() {
        return this.currentPage > 1;
    }

    /**
     * 获取起始行号（从0开始）
     * 常用于SQL的LIMIT子句
     *
     * @return 起始行号
     */
    public long getOffset() {
        return (this.currentPage - 1) * this.pageSize;
    }
}
