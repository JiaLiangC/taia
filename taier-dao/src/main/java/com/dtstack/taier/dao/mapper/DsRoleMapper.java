package com.dtstack.taier.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dtstack.taier.dao.domain.DsRole;
import com.dtstack.taier.dao.domain.DsUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DsRoleMapper extends BaseMapper<DsRole> {
    List<Long> queryDsIdListByRole(@Param("roleId") Long roleId);
}
