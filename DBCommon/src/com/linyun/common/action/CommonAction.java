package com.linyun.common.action;

import java.util.List;
import java.util.Set;

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

public interface CommonAction {
      
	  //绑定邀请码时，拉取赠送玩家的钻石信息   
	  GameConfig  getBindRefCodePresentDiamond();
	
	  //用于初始化单例 
	  List<GiftCode> getGiftCodeBindInfoBySql();
	  
	  //拉取公告
	  Notice getNoticeContent();
	  
	  //更改公告内容
	  void updateNoticeContent(String content,int id);
	  
	  //拉取跑马灯信息
	  List<Marquee> getMarqueeList();
	  
	  //更新跑马灯信息
	  void updateMarquee(int id,String content,int voild);
	  
	  //拉取邀请码的集合
	  Set<Integer> getInviteCodeList();
	  
	  //拉取注册玩家的昵称的集合
	  List<UserNickName>  getUserNickName();
	  
	  //拉取客服相关的信息
	  CustomService getCustomServiceInfo();
	  
	  //初始化所有场次配置
	  List<FieldConfig> selectAllFieldConfig();
	  
	  void updateFieldConfig(FieldConfig f);
	  
	  List<SectionConfig> selectAllSectionConfig();
	  
	  List<PayConfig> selectAllPayConfig();
	  
}
