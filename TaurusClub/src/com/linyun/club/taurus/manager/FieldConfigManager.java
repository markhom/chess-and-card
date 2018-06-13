package com.linyun.club.taurus.manager;

import java.util.List;

import com.linyun.common.entity.FieldConfig;
import com.linyun.middle.common.taurus.server.ActionAware;

/**
 * @Author walker
 * @Since 2018年5月23日
 **/

public class FieldConfigManager {
	private static FieldConfigManager fieldConfigManager = new FieldConfigManager();

	public List<FieldConfig> configs;

	private FieldConfigManager() {
		initFieldConfig();
	}

	public static FieldConfigManager getInstance() {
		return fieldConfigManager;
	}

	public void initFieldConfig() {
		ActionAware m_action = new ActionAware();
	    configs = m_action.commonAction().selectAllFieldConfig();

	}
	

}
