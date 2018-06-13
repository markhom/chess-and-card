package com.linyun.pay.service;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.linyun.bottom.handler.HttpRequest;
import com.linyun.bottom.handler.HttpResponse;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.common.entity.DiamondLog;
import com.linyun.common.entity.Order;
import com.linyun.common.entity.PayConfig;
import com.linyun.common.entity.SectionConfig;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.DiamondChangedType;
import com.linyun.middle.common.taurus.service.BaseServer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
*  @Author walker
*  @Since 2018年5月28日
**/

public class PayServer extends BaseServer
{
	
	 Logger logger = LoggerFactory.getLogger(PayServer.class);
	 
	 public static final int CID = 137;
	 public static final String APIKEY = "ZaHJIWkZ7pTjAwEALZCSK0sajbZka1aqP8Yc96L80xF4L8wjJi09azgdtntOGHP9";
	 public static final String RECHARGE_ORDER_URL = "https://www.dsdfpay.com/dsdf/customer_pay/init_din";//充值订单接口URL GET 
	 public static final String QUERY_ORDER_URL = "https://www.dsdfpay.com/dsdf/api/query_order";//查询充值订单
	/**
	 *  跳转选择支付方式
	 * @param request
	 * @param response
	 */
	public void choosePayType(HttpRequest request, HttpResponse response)
	{
		try
		{
			String body = request.body();
			JSONObject params = JSONObject.fromObject(body);
			String userId = params.getString("userId");
			int diamond = params.getInt("diamond");//选择充值钻石数
			boolean isGaming = params.getBoolean("isGaming");
			
			JSONObject obj = new JSONObject();
			if(diamond <60)
			{
				obj.put("res",1);
				obj.put("msg", "充值钻石数不得低于60！");
				response.content(obj.toString());
				response.end();
				return;
			}
			
		    User u = userAction().getExistUser(userId);
		    //打折计算
		    List<SectionConfig> configs = commonAction().selectAllSectionConfig();
		    int scale = 100;
		    for(int i=0; i<configs.size(); ++i)
		    {
		    	SectionConfig config = configs.get(i);
		    	if(config.getMaxMoney() > 0)
		    	{
		    		if(diamond>config.getMinMoney() && diamond <= config.getMaxMoney())
			    	{
			    		scale = config.getScale();
			    		break;
			    	}
		    	}else
		    	{
		    		scale = config.getScale();
		    		break;
		    	}
		    }
		    BigDecimal b = new BigDecimal(Double.toString(diamond));
		    BigDecimal c = new BigDecimal(Double.toString(1000));
		    BigDecimal d = new BigDecimal(Double.toString(scale));
		    BigDecimal f = b.divide(c).multiply(d);
		    double f1 = f.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		    int amount = new BigDecimal(Double.toString(f1)).multiply(new BigDecimal(Double.toString(100))).intValue();
		    
		    String order_id = null;
		    JSONArray arr = new JSONArray();
		    if(!isGaming)
		    {
		    	 order_id = getOrderIdByUUID();//生成订单号
				
				 List<PayConfig> payConfigs = commonAction().selectAllPayConfig();
				 for(PayConfig p : payConfigs)
				 {
				    if(p.getMinMoney()*100<= amount && p.getMaxMoney()*100>= amount)
				    {
				    	JSONObject json = new JSONObject();
				    	json.put("payType", p.getSort());
				    	json.put("minMoney", p.getMinMoney());
				    	json.put("maxMoney", p.getMaxMoney());
				    	arr.add(json);
				    }
				 }
				    
				 //创建订单
				 Order o = new Order();
				 o.setOrderId(order_id);
				 o.setUserId(Integer.parseInt(userId));
				 o.setDiamond(diamond);
				 o.setAmount(amount);
				 o.setStatus(Order.STATUS_CREATED);
				 orderAction().saveOrder(o);
		    }
		   
		    obj.put("res", 0);
		    obj.put("diamond", diamond);
		    obj.put("money", f1);
		    obj.put("scale", 100-scale);
		    if(!isGaming)
		    {
		    	obj.put("orderId", order_id);
			    obj.put("payTypes", arr);
		    }
		    response.content(obj.toString());
		    response.end();
		   
			
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			JSONObject obj = new JSONObject();
			obj.put("res", 200);
			response.content(obj.toString());
			response.end();
		} finally {
		}
	
	}
	
	/**
	 *  跳转第三方支付平台
	 * @param request
	 * @param response
	 */
	public void skipPayPlatform(HttpRequest request, HttpResponse response)   
	{
		try
		{
			String body = request.body();
			JSONObject params = JSONObject.fromObject(body);
			String orderId = params.getString("orderId");
			String payType = params.getString("payType");
			
			String type = null;
			String tflag = null;
			switch(payType.toLowerCase())
	       	{
	          case "wxpay" : type ="qrcode"; tflag ="WebMM"; break;
	       	  case "remit" : type ="remit" ; break;
	       	  case "qqpay" : type = "qrcode" ;tflag = "QQPAY"; break;
	       	  case "alipay" : type = "qrcode" ; tflag = "ALIPAY";break;
	       	  case "online" : type = "online" ; break;
	          case "quick" : type="quick" ; break;
	       	}
			Order order = orderAction().selectByOrderId(orderId);
			int amount = order.getAmount();//订单金额  单位：分
			BigDecimal b = new BigDecimal(Double.toString(amount)).divide(new BigDecimal(Double.toString(100))).setScale(2, BigDecimal.ROUND_HALF_UP);
			double d = b.doubleValue();
			
			InetSocketAddress remoteAddress = (InetSocketAddress) request.remoteAddress();
		    String ip = remoteAddress.getHostString();
		    StringBuilder sb = new StringBuilder();
		    sb.append("").append("cid").append("=").append(CID);
		    sb.append("&").append("uid").append("=").append(order.getUserId());
		    sb.append("&").append("time").append("=").append(System.currentTimeMillis()/1000);
		    sb.append("&").append("amount").append("=").append(d);//单位：元
		    sb.append("&").append("order_id").append("=").append(orderId); 
		    sb.append("&").append("ip").append("=").append(ip);
		    //获取签名
		    String sign = sha1Encrypt(APIKEY,sb.toString());
		    sb.append("&").append("sign").append("=").append(sign);
		    sb.append("&").append("type").append("=").append(type);
		    if(tflag != null)
		    {
		    	sb.append("&").append("tflag").append("=").append(tflag);
		    }
		    JSONObject obj = new JSONObject();
		    obj.put("res", 0);
		    obj.put("url", RECHARGE_ORDER_URL+"?"+sb.toString());
		    response.content(obj.toString());
		    response.end();
		    
			
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			JSONObject obj = new JSONObject();
			obj.put("res", 200);
			response.content(obj.toString());
			response.end();
		}
	}
	
	/**
	 *  客户端取消充值订单
	 */
	
	public void revokeOrder(HttpRequest request, HttpResponse response)
	{
		try
		{
			String body = request.body();
			JSONObject params = JSONObject.fromObject(body);
			String userId = params.getString("userId");
			String orderId = params.getString("orderId");
			
			userAction().getExistUser(userId);
			Order o = orderAction().selectByOrderId(orderId);
			JSONObject obj = new JSONObject();
			if(o.getStatus() != Order.STATUS_CREATED)
			{
				obj.put("res", 1);
				obj.put("msg", "玩家主动撤单时该订单状态已被更改！");
				
			}else
			{
				orderAction().updateOrderStatus(orderId, Order.STATUS_REVOKED);
				obj.put("res", 0);
			}
			response.content(obj.toString());
			response.end();
			
		}catch(Exception e)
		{
			JSONObject obj = new JSONObject();
			obj.put("res", 200);
			response.content(obj.toString());
			response.end();
		   logger.error(e.getMessage(),e);	
		}
	}
	
	/**
	 *  查询充值订单
	 * @return
	 * @throws Exception 
	 */
	public String queryOrder(String orderId) throws Exception
	{
		JSONObject data = new JSONObject();
		data.put("cid", CID);
		data.put("order_id", orderId);
		data.put("time", System.currentTimeMillis()/1000);
	
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(QUERY_ORDER_URL);
		String sign = sha1Encrypt(APIKEY, data.toString());
		httpPost.addHeader("Content-Hmac", sign);
		httpPost.addHeader("content-type", "application/json");
		httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);
		String result = EntityUtils.toString(response2.getEntity(), "utf-8"); 
		return result;

	}
	/**
	 *  风云支付平台订单回调接口
	 * @param request
	 * @param response
	 */
	public void orderCallback(HttpRequest request, HttpResponse response)
	{
		try
		{
			String body = request.body();
			String header = request.header("Content-Hmac");
			String sign = sha1Encrypt(APIKEY, body);
			JSONObject params = JSONObject.fromObject(body);
			if(!header.equals(sign))
			{
				response.content("false: sign error");
				response.end();
				return;
			}else
			{
				String orderId = params.getString("order_id");
				int amount = params.getInt("amount"); //充值金额  单位：分
				String userId = params.getString("customer_name");
				String type = params.getString("type");//充值方式
				String orderStatus = params.getString("cmd"); //  “order_success”/ “order_revoked”/ “order_timeout”
				Order order = orderAction().selectByOrderId(orderId);
				User u = userAction().getExistUser(userId);
				if(order.getStatus() != Order.STATUS_CREATED)
				{
					response.content("true");
					response.end();
					return;
				}else
				{
					switch(orderStatus)
					{
					   case "order_success" : 
					   {
						   userAction().rechargeDiamond(userId, order.getDiamond());
						   orderAction().updateOrderStatus(orderId, Order.STATUS_VERIFIED);
						   DiamondLog log = new DiamondLog();
						   log.setUserId(Integer.parseInt(userId));
						   log.setOldDiamond(u.getDiamond());
						   log.setNewDiamond(u.getDiamond()+order.getDiamond());
						   log.setChangedDiamond(order.getDiamond());
						   log.setChangedType(DiamondChangedType.TYPE_RECHARGE.value);
						   log.setRemark("玩家"+userId+"通过"+type+"方式充值钻石"+order.getDiamond()+"成功！");
						   diamondLogAction().addOneRecord(log);
						   logger.info("玩家"+userId+",充值订单号为："+orderId+",通过风云支付平台充值成功钻石数："+order.getDiamond()+",时间为："+new Date().toString());
					
					   } break;
					   case "order_revoked" :
					   {
						   orderAction().updateOrderStatus(orderId, Order.STATUS_REVOKED);
						   logger.info("玩家"+userId+",充值订单号为："+orderId+"在风云平台已撤单！");
						   
					   } break;
					   case "order_timeout" :
					   {
						   orderAction().updateOrderStatus(orderId, Order.STATUS_TIMEOUT);
						   logger.info("玩家"+userId+",充值订单号为："+orderId+",因超时未支付撤单！");
					   }break;
					}
					
					response.content("true");
					response.end();
				}
			}
			
		}catch(Exception e)
		{
			response.content("false");
			response.end();
			logger.error(e.getMessage(),e);
		}
	}
	
	
	/**
	 *  sha1 盐加密 获得签名 sign
	 * @param key
	 * @param data
	 * @return
	 * @throws Exception
	 */
	
	public String sha1Encrypt(String key, String data) throws Exception
	{
		  String HMAC_SHA1_ALGORITHM = "HmacSHA1";
		  SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		  Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		  mac.init(signingKey);
		  byte[] rawHmac = mac.doFinal(data.getBytes());
		  String result = new String(Base64.encodeBase64(rawHmac));
		  
		  return result;
	}
	
	/**
	 * 利用UUID生成16位唯一订单号
	 * @return
	 */
	
	public String getOrderIdByUUID()
	{
		 int machineId = new Random().nextInt(8)+1;//最大支持1-9个集群机器部署
         int hashCodeV = UUID.randomUUID().toString().hashCode();
         if(hashCodeV < 0) {//有可能是负数
             hashCodeV = - hashCodeV;
         }
         // 0 代表前面补充0     
         // 4 代表长度为4     
         // d 代表参数为正数型
         return machineId + String.format("%015d", hashCodeV);
	}
	
	
	

	
	
	
	

}
