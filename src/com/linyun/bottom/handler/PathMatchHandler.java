/**
 * Juice
 * com.juice.orange.game.handler
 * PathMatchHandler.java
 */
package com.linyun.bottom.handler;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shaojieque
 * 2013-3-20
 */
public class PathMatchHandler implements IJuiceHandler {
	private final Pattern pathPattern;
	private final IJuiceHandler handler;
	
	public PathMatchHandler(Pattern pathPattern, IJuiceHandler handler) {
		this.pathPattern = pathPattern;
		this.handler = handler;
	}
	
	public PathMatchHandler(String path, IJuiceHandler httpHandler) {
		this(Pattern.compile(path), httpHandler);
	}

	@Override
	public void handlerRequest(IJuiceRequest request, IJuiceResponse response,
			IJuiceControl control) throws Exception {
		String path = URI.create(request.uri()).getPath();
		Matcher matcher = pathPattern.matcher(path);
		if (matcher.matches()) {
			handler.handlerRequest(request, response, control);
		} else {
			control.nextHandler();
		}
	}
}
