package com.fahai.web.dao;

import com.fahai.web.entity.caseInvestigation;

public interface caseInvestigationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(caseInvestigation record);

    int insertSelective(caseInvestigation record);

    caseInvestigation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(caseInvestigation record);

    int updateByPrimaryKeyWithBLOBs(caseInvestigation record);

    int updateByPrimaryKey(caseInvestigation record);
}