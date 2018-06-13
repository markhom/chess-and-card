/**
 * 
 */
package com.linyun.middle.common.taurus.server;

import com.linyun.bottom.container.Container;
import com.linyun.common.action.BullGameLogAction;
import com.linyun.common.action.BullWaybillAction;
import com.linyun.common.action.ClubAction;
import com.linyun.common.action.ClubCommonAction;
import com.linyun.common.action.ClubConfigAction;
import com.linyun.common.action.ClubDiamondLogAction;
import com.linyun.common.action.ClubMessageAction;
import com.linyun.common.action.ClubOperateLogAction;
import com.linyun.common.action.ClubRoomAction;
import com.linyun.common.action.ClubRoomLogAction;
import com.linyun.common.action.CommonAction;
import com.linyun.common.action.DiamondLogAction;
import com.linyun.common.action.GameAccountLogAction;
import com.linyun.common.action.GamePlayerLogAction;
import com.linyun.common.action.GameRoundLogAction;
import com.linyun.common.action.OnlineTaurusAction;
import com.linyun.common.action.OrderAction;
import com.linyun.common.action.ClubMemberAction;
import com.linyun.common.action.PrivateRoomAction;
import com.linyun.common.action.TaurusGameAction;
import com.linyun.common.action.TaurusLogAction;
import com.linyun.common.action.TaurusRoundLogAction;
import com.linyun.common.action.UserAction;

/**
 * @author shaojieque 2013-5-6
 */
public class ActionAware 
{
	public static final String REMOTE_PREFIX_SYSTEM = "DBSystem";
	public static final String REMOTE_PREFIX_GAME_SERVER = "GameServer";
	
	public UserAction userAction() 
	{
		return (UserAction) Container.createRemoteService(UserAction.class, REMOTE_PREFIX_SYSTEM);
	}
	public OrderAction orderAction()
	{
		return (OrderAction) Container.createRemoteService(OrderAction.class, REMOTE_PREFIX_SYSTEM);
	}
	public TaurusLogAction taurusLogAction()
	{
		return (TaurusLogAction) Container.createRemoteService(TaurusLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public PrivateRoomAction roomAction()
	{	
		return (PrivateRoomAction) Container.createRemoteService(PrivateRoomAction.class, REMOTE_PREFIX_SYSTEM);
	}	
	
	public TaurusRoundLogAction taurusRoundLogAction()
	{
		return (TaurusRoundLogAction) Container.createRemoteService(TaurusRoundLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public OnlineTaurusAction onlineAction()
	{
		return (OnlineTaurusAction) Container.createRemoteService(OnlineTaurusAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubMemberAction clubMemberAction()
	{
		return (ClubMemberAction) Container.createRemoteService(ClubMemberAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubAction clubAction()
	{
		return (ClubAction) Container.createRemoteService(ClubAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubDiamondLogAction clubDiamondLogAction()
	{
		return (ClubDiamondLogAction) Container.createRemoteService(ClubDiamondLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public  DiamondLogAction diamondLogAction()
	{
		return (DiamondLogAction) Container.createRemoteService(DiamondLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubMessageAction clubMessageAction()
	{
		return (ClubMessageAction) Container.createRemoteService(ClubMessageAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubOperateLogAction clubOperateLogAction()
	{
		return (ClubOperateLogAction) Container.createRemoteService(ClubOperateLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubConfigAction clubConfigAction()
	{
		return (ClubConfigAction) Container.createRemoteService(ClubConfigAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubCommonAction clubCommonAction()
	{
		return (ClubCommonAction) Container.createRemoteService(ClubCommonAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public ClubRoomAction clubRoomAction()
	{
		return (ClubRoomAction) Container.createRemoteService(ClubRoomAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public TaurusGameAction taurusGameAction()
	{
		return (TaurusGameAction) Container.createRemoteService(TaurusGameAction.class, REMOTE_PREFIX_GAME_SERVER);
	}
	
	public CommonAction commonAction()
	{
		return (CommonAction) Container.createRemoteService(CommonAction.class, REMOTE_PREFIX_SYSTEM);
	}
	public ClubRoomLogAction clubRoomLogAction()
	{
		return (ClubRoomLogAction) Container.createRemoteService(ClubRoomLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	
	public GamePlayerLogAction gamePlayerLogAction()
	{
		return (GamePlayerLogAction) Container.createRemoteService(GamePlayerLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	public GameRoundLogAction gameRoundLogAction()
	{
		return (GameRoundLogAction) Container.createRemoteService(GameRoundLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	public BullWaybillAction bullWaybillAction()
	{
		return (BullWaybillAction) Container.createRemoteService(BullWaybillAction.class, REMOTE_PREFIX_SYSTEM);
	}
	public GameAccountLogAction gameAccountLogAction()
	{
		return (GameAccountLogAction) Container.createRemoteService(GameAccountLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
	public BullGameLogAction bullGameLogAction()
	{
		return (BullGameLogAction) Container.createRemoteService(BullGameLogAction.class, REMOTE_PREFIX_SYSTEM);
	}
}
