package com.linyun.data.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.CustomService;
import com.linyun.common.entity.DomainConfig;
import com.linyun.common.entity.FieldConfig;
import com.linyun.common.entity.GameConfig;
import com.linyun.common.entity.GiftCode;
import com.linyun.common.entity.Marquee;
import com.linyun.common.entity.Notice;
import com.linyun.common.entity.PayConfig;
import com.linyun.common.entity.SectionConfig;
import com.linyun.common.entity.UserNickName;

public interface CommonMapper {

	//绑定邀请码，送钻石
	GameConfig getBindRefCodePresentDiamond();
	
	//拉取公告内容
	Notice getNoticeContent();
	
	//更新公告
	void updateNoticeContent(Map<String,Object> map);
	
	List<GiftCode> getAllGiftCode();
	
	//拉取跑马灯
	List<Marquee> getMarqueeList(int statue);
	
	//根据id拉取一条跑马灯
	Marquee getMarqueeById(int id);
	
	//更新跑马灯
	void updateMarquee(@Param("condition")Marquee marquee);
	
	//拉取邀请码
	List<GiftCode> getInviteCodeList();
	
	//拉取用户昵称的集合
	List<UserNickName> getUserNickName();
	
	//玩家拉取代理商qq群，微信公众号信息
	CustomService getCustomServiceInfo();
	
	//获取所有场次配置信息
	List<FieldConfig> selectAllFieldConfig();
	//修改场次配置
	public void updateFieldConfig(FieldConfig f);
	
	//获取充值钻石折扣区间
	List<SectionConfig>selectAllSectionConfig();
	
	//拉取充值方式配置
	List<PayConfig> selectAllPayConfig();
	
}
