package com.dtstack.taier.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dtstack.taier.dao.domain.TenantUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantUserMapper extends BaseMapper<TenantUser> {

    List<Long> queryTenantIdListByUser(@Param("userId") Long userId);
}
