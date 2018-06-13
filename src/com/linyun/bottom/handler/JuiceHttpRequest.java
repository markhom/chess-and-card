/**
 * Juice
 * com.juice.orange.game.handler
 * JuiceHttpRequest.java
 */
package com.linyun.bottom.handler;

import java.net.HttpCookie;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.util.CharsetUtil;

import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.util.InboundCookieParser;
import com.linyun.bottom.util.QueryParameters;


/**
 * @author shaojieque 2013-3-20
 */
public class JuiceHttpRequest implements
		com.linyun.bottom.handler.HttpRequest {
	private final org.jboss.netty.handler.codec.http.HttpRequest httpRequest;
	private final MessageEvent messageEvent;
	private final Map<String, Object> data = new HashMap<String, Object>();
	private final Object id;
	private final long timestamp;
	//
	public GameSession session;
	public QueryParameters queryParameters;
	public QueryParameters postParameters;

	public JuiceHttpRequest(MessageEvent e,
			org.jboss.netty.handler.codec.http.HttpRequest request,
			GameSession session, Object id, long timestamp) {
		this.messageEvent = e;
		this.httpRequest = request;
		this.id = id;
		this.timestamp = timestamp;
		this.session = session;
	}

	@Override
	public String uri() {
		return httpRequest.getUri();
	}

	@Override
	public JuiceHttpRequest uri(String uri) {
		httpRequest.setUri(uri);
		return this;
	}

	@Override
	public String getParam(String key) {
		return parsedQueryParams().first(key);
	}

	private QueryParameters parsedQueryParams() {
		if (queryParameters == null) {
			String[] temp = uri().split("\\?");
			if (temp.length > 1) {
				String paramStr = temp[1];
				queryParameters = new QueryParameters(paramStr);
			}
		}
		return queryParameters;
	}

	@Override
	public SocketAddress remoteAddress() {
		return messageEvent.getRemoteAddress();
	}

	@Override
	public QueryParameters getParams() {
		return queryParameters;
	}

	@Override
	public String header(String name) {
		return httpRequest.getHeader(name);
	}

	@Override
	public List<String> headers(String name) {
		return httpRequest.getHeaders(name);
	}

	@Override
	public boolean hasHeader(String name) {
		return httpRequest.containsHeader(name);
	}

	@Override
	public List<HttpCookie> cookies() {
		return InboundCookieParser.parse(headers(COOKIE_HEADER));
	}

	@Override
	public HttpCookie cookie(String name) {
		for (HttpCookie cookie : cookies()) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	@Override
	public List<String> queryParams(String key) {
		return parsedQueryParams().all(key);
	}

	@Override
	public String cookieValue(String name) {
		HttpCookie cookie = cookie(name);
		return cookie == null ? null : cookie.getValue();
	}

	@Override
	public List<Entry<String, String>> allHeaders() {
		return httpRequest.getHeaders();
	}

	@Override
	public String method() {
		return httpRequest.getMethod().getName();
	}

	@Override
	public String body() {
		return httpRequest.getContent().toString(CharsetUtil.UTF_8);
	}

	@Override
	public Object id() {
		return id;
	}

	@Override
	public long timestamp() {
		return timestamp;
	}

	@Override
	public Map<String, Object> data() {
		return data;
	}

	@Override
	public Object data(String key) {
		return data.get(key);
	}

	@Override
	public void data(String key, Object value) {
		data.put(key, value);
	}

	@Override
	public GameSession getSession() {
		return session;
	}

	@Override
	public String toString() {
		return messageEvent.getRemoteAddress() + " " + httpRequest.getMethod()
				+ " " + httpRequest.getUri();
	}
}
