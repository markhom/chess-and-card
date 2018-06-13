/**
 * SuperStarServer
 * com.orange.superstar.server
 * ActionAware.java
 */
package com.linyun.pay.server;

import com.linyun.bottom.container.Container;
import com.linyun.common.action.CommonAction;
import com.linyun.common.action.DomainConfigAction;
import com.linyun.common.action.TaurusClubGameAction;
import com.linyun.common.action.TaurusGameAction;
import com.linyun.common.action.UserAction;

public class ActionAware extends Application
{
	public static final String REMOTE_PREFIX_SYSTEM = "DBSystem";
	public static final String REMOTE_PREFIX_GAME = "GameTaurus";
	public static final String REMOTE_PREFIX_CLUB_GAME = "ClubGameTaurus";
	
	public UserAction userAction() 
	{
		return (UserAction) Container.createRemoteService(UserAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public CommonAction commonAction() 
	{
		return (CommonAction) Container.createRemoteService(CommonAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public TaurusGameAction taurusGameAction()
	{
		return (TaurusGameAction) Container.createRemoteService(TaurusGameAction.class, REMOTE_PREFIX_GAME);
	}
	
	public TaurusClubGameAction taurusClubGameAction()
	{
		return (TaurusClubGameAction) Container.createRemoteService(TaurusClubGameAction.class, REMOTE_PREFIX_CLUB_GAME);
	}
	
	public DomainConfigAction domainConfigAction()
	{
		return (DomainConfigAction) Container.createRemoteService(DomainConfigAction.class, REMOTE_PREFIX_SYSTEM);
	}

}
