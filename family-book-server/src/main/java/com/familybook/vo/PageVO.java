package com.familybook.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页VO类
 *
 * @param <T> 数据类型
 */
@Data
public class PageVO<T> {

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 快速创建分页对象
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param total    总记录数
     * @param pages    总页数
     * @param list     数据列表
     * @param <T>      数据类型
     * @return 分页VO对象
     */
    public static <T> PageVO<T> of(Integer pageNum, Integer pageSize, Long total, Integer pages, List<T> list) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotal(total);
        pageVO.setPages(pages);
        pageVO.setList(list);
        return pageVO;
    }
}
