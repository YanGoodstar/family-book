package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper接口
 * 用于管理收支分类的数据访问操作，包括用户自定义分类和系统预设分类的CRUD
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}
