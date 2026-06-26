package com.unicom.post.common.result;

import java.util.List;

public class PageResult<T> {
    private Long total;
    private Integer pageNo;
    private Integer pageSize;
    private List<T> list;

    public PageResult(Long total, Integer pageNo, Integer pageSize, List<T> list) {
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.list = list;
    }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
}