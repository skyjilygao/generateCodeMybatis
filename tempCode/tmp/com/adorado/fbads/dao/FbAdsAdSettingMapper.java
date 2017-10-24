package com.adorado.fbads.dao;

import com.adorado.fbads.entity.FbAdsAdSetting;

public interface FbAdsAdSettingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FbAdsAdSetting record);

    int insertSelective(FbAdsAdSetting record);

    FbAdsAdSetting selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FbAdsAdSetting record);

    int updateByPrimaryKey(FbAdsAdSetting record);
}