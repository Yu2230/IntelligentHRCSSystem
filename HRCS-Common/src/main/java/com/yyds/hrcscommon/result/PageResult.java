package com.yyds.hrcscommon.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private long total;          // 总记录数
    private List<T> list;        // 当前页数据
    private long pageNum;        // 当前页码
    private long pageSize;       // 每页条数
    private long pages;          // 总页数
    private boolean hasNext;     // 是否有下一页
    private boolean hasPrevious; // 是否有上一页

    public static <T> PageResult<T> success(long total, List<T> list) {
        return PageResult.<T>builder()
                .total(total)
                .list(list)
                .build();
    }

    public static <T> PageResult<T> build(List<T> list, long total, Integer pageNum, Integer pageSize) {
        long totalPages = (pageSize == null || pageSize == 0) ? 0 : (total + pageSize - 1) / pageSize;
        long currentPage = (pageNum == null) ? 0 : pageNum;

        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .pageNum(currentPage)
                .pageSize(pageSize == null ? 0 : pageSize)
                .pages(totalPages)
                .hasNext(currentPage < totalPages)
                .hasPrevious(currentPage > 1)
                .build();
    }
}
