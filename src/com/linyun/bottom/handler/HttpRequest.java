/**
 * Juice
 * com.juice.orange.game.handler
 * HttpRequest.java
 */
package com.linyun.bottom.handler;

import java.net.HttpCookie;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import com.linyun.bottom.util.QueryParameters;

/**
 * @author shaojieque 2013-3-19
 */
public interface HttpRequest extends IJuiceRequest{
	public String COOKIE_HEADER = "Cookie";

	String uri();

	/**
	 * Modify uri
	 */
	HttpRequest uri(String uri);

	/**
	 * Retrieve the value single HTTP header.
	 */
	String header(String name);
	
	/**
     * Retrieve all values for an HTTP header. If no values are found, an empty List is returned.
     */
    List<String> headers(String name);
    
    /**
     * Whether a specific HTTP header was present in the request.
     */
    boolean hasHeader(String name);
    
    /**
     * @return all inbound cookies
     */
    List<HttpCookie> cookies();
    
    /**
     * Get a cookie with a specific name
     *
     * @param name cookie name
     * @return cookie with that name
     */
    HttpCookie cookie(String name);

    /**
     * Get query parameter value.
     */
	String getParam(String key);

	/**
     * Get all query parameter values.
     */
    List<String> queryParams(String key);
	
	QueryParameters getParams();
	
	 /**
     * Get the value of named cookie
     *
     * @param name cookie name
     * @return cookie value, or null if the cookie does not exist.
     */
    String cookieValue(String name);

    /**
     * Returns all headers sent from client.
     */
    List<Map.Entry<String, String>> allHeaders();
    
    /**
     * HTTP method (e.g. "GET" or "POST")
     */
    String method();
    
    /**
     * The body
     */
    String body();
    
    /**
     * Arbitrary data that can be stored for the lifetime of the connection.
     */
    Map<String, Object> data();
    
    /**
     * Retrieve data value by key.
     */
    Object data(String key);

    /**
     * Store data value by key.
     */
    void data(String key, Object value);


	/**
	 * Remote address of connection (i.e. the host of the client).
	 */
	SocketAddress remoteAddress();
	
	 /**
     * A unique identifier for this request. This should be treated as an opaque object,
     * that can be used to track the lifecycle of a request.
     */
    Object id();

    /**
     * Timestamp (millis since epoch) of when this request was first received by the server.
     */
    long timestamp();
}
