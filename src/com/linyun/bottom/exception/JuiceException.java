package com.linyun.bottom.exception;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

public class JuiceException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	private short id;

	public JuiceException(String message) {
        super(message);
    }
	
	public JuiceException(short id, String message) {
        super(message);
        this.id = id;
    }

    public JuiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public JuiceException(Throwable cause) {
        super(cause);
    }

    public static JuiceException fromExceptionEvent(ExceptionEvent e) {
    	return fromException(e.getCause(), e.getChannel());
    }

    public static JuiceException fromException(Throwable t, Channel channel) {
        String throwableStr = t != null ? t.getMessage() : "[null throwable]";
        String channelStr = channel != null ? channel.toString() : "[null channel]";
        return new JuiceException(String.format("%s on %s", throwableStr, channelStr), t);
    }

	public short getId() {
		return id;
	}
}
