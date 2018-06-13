/**
 * Juice
 * com.juice.orange.game.handler
 * ServerHeaderHandler.java
 */
package com.linyun.bottom.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.log.LoggerFactory;


/**
 * @author shaojieque 
 * 2013-3-21
 */
public class DefaultJuiceHandler implements IJuiceHandler 
{
	private static Logger logger = LoggerFactory
			.getLogger(DefaultJuiceHandler.class);
	private static final String BROWSER_REQUEST_PATH = "favicon.ico";

	@Override
	public void handlerRequest(IJuiceRequest request, IJuiceResponse response,
			IJuiceControl control) throws Exception 
	{
		if (request instanceof DefaultJuiceRequest) 
		{
			handlerDefaultRequest((SocketRequest)request, 
					(SocketResponse)response);
		} 
		else if (request instanceof JuiceHttpRequest
				&& response instanceof JuiceHttpResponse) 
		{
			handleHttpRequest((JuiceHttpRequest) request,
					(JuiceHttpResponse) response);
		}
	}

	private void handlerDefaultRequest(SocketRequest request, SocketResponse response) 
	{
		try 
		{
			String flag = request.getPath();
			String path = Container.getServerPath(flag);
			String[] paths = path.split("\\/");
			//
			String serverName = paths[0];
			String methodName = paths[1];

			Object server = Container.getServer(serverName);
			if (server != null)
			{
				Method method = server.getClass().getMethod(methodName,
						SocketRequest.class, SocketResponse.class);
				method.invoke(server, request, response);
			}
		} 
		catch (InvocationTargetException e) 
		{
			logger.error(e.getTargetException().getMessage(), e.getTargetException().getCause());
		}
		catch (Exception e) 
		{
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("handler request error,");
			errorMsg.append("path:" + request.getPath());
			JuiceException je = new JuiceException(errorMsg.toString());
			logger.error(e.getMessage());
			logger.error(je);
		}
	}

	private void handleHttpRequest(HttpRequest request, HttpResponse response) 
	{
		try 
		{
			String path = URI.create(URLEncoder.encode(request.uri(), "UTF-8")).getPath();
			String[] paths = path.split("\\/");
			//
			String serverName = paths[1];
			if (serverName.equals(BROWSER_REQUEST_PATH))
			{//屏蔽掉浏览器图标显示的请求
				return;
			}
			String[] methodNames = paths[2].split("\\?");
			String methodName = methodNames[0];

			Object server = Container.getServer(serverName);
			if (server != null) 
			{
				Method method = server.getClass().getMethod(methodName,
						HttpRequest.class, HttpResponse.class);
				method.invoke(server, request, response);
			}
		}
		catch (InvocationTargetException e) 
		{
			logger.error(e.getTargetException().getMessage(), e.getTargetException());
		}
		catch (Exception e) 
		{
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("handler request error,");
			errorMsg.append("path:" + request.uri() + ";param:" + request.getParams());
			JuiceException je = new JuiceException(errorMsg.toString());
			logger.error(je);
			logger.error(e);
		}
	}
}
