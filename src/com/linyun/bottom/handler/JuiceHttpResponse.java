/**
 * Juice
 * com.juice.orange.game.handler
 * JuiceHttpResponse.java
 */
package com.linyun.bottom.handler;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.util.DateUtils;


/**
 * @author shaojieque 2013-3-20
 */
public class JuiceHttpResponse implements HttpResponse {
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	private final ChannelHandlerContext ctx;
	private final org.jboss.netty.handler.codec.http.HttpResponse response;
	private final boolean isKeepAlive;
	private final Thread.UncaughtExceptionHandler exceptionHandler;
	private final ChannelBuffer responseBuffer;
	private Charset charset;

	public JuiceHttpResponse(ChannelHandlerContext ctx,
			org.jboss.netty.handler.codec.http.HttpResponse response,
			boolean isKeepAlive,
			Thread.UncaughtExceptionHandler exceptionHandler) {
		this.ctx = ctx;
		this.response = response;
		this.isKeepAlive = isKeepAlive;
		this.exceptionHandler = exceptionHandler;
		this.charset = DEFAULT_CHARSET;
		responseBuffer = ChannelBuffers.dynamicBuffer();
		header("Access-Control-Allow-Origin", "*");
	}

	@Override
	public JuiceHttpResponse charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public JuiceHttpResponse status(int status) {
		response.setStatus(HttpResponseStatus.valueOf(status));
		return this;
	}

	@Override
	public int status() {
		return response.getStatus().getCode();
	}

	@Override
	public JuiceHttpResponse header(String name, String value) {
		if (value == null) {
			response.removeHeader(name);
		} else {
			response.addHeader(name, value);
		}
		return this;
	}

	@Override
	public JuiceHttpResponse header(String name, long value) {
		response.addHeader(name, value);
		return this;
	}

	@Override
	public JuiceHttpResponse header(String name, Date value) {
		response.addHeader(name, DateUtils.rfc1123Format(value));
		return this;
	}

	@Override
	public boolean containsHeader(String name) {
		return response.containsHeader(name);
	}

	@Override
	public JuiceHttpResponse cookie(HttpCookie httpCookie) {
		return header(HttpHeaders.Names.SET_COOKIE, httpCookie.toString());
	}

	@Override
	public JuiceHttpResponse content(String content) {
		return content(ChannelBuffers.copiedBuffer(content, charset()));
	}

	@Override
	public JuiceHttpResponse content(byte[] content) {
		return content(ChannelBuffers.copiedBuffer(content));
	}

	@Override
	public JuiceHttpResponse content(ByteBuffer buffer) {
		return content(ChannelBuffers.wrappedBuffer(buffer));
	}

	private JuiceHttpResponse content(ChannelBuffer content) {
		responseBuffer.writeBytes(content);
		return this;
	}

	@Override
	public JuiceHttpResponse write(String content) {
		write(copiedBuffer(content, CharsetUtil.UTF_8));
		return this;
	}

	private ChannelFuture write(ChannelBuffer responseBuffer) {
		response.setContent(responseBuffer);
		return ctx.getChannel().write(response);
	}

	@Override
	public JuiceHttpResponse error(Throwable error) {
		if (error instanceof TooLongFrameException) {
			response.setStatus(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
		} else {
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
		//
		String message = getStackTrace(error);
		header("Content-Type", "text/plain");
		content(message);
		flushResponse();
		exceptionHandler.uncaughtException(Thread.currentThread(),
				JuiceException.fromException(error, ctx.getChannel()));
		return this;
	}

	private String getStackTrace(Throwable error) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        error.printStackTrace(writer);
        writer.flush();
        return buffer.toString();
    }
	
	private void flushResponse() {
        try {
            // TODO: Shouldn't have to do this, but without it we sometimes seem to get two Content-Length headers in the response.
            header("Content-Length", (String) null);
            header("Content-Length", responseBuffer.readableBytes());
            ChannelFuture future = write(responseBuffer);
            if (!isKeepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            exceptionHandler.uncaughtException(Thread.currentThread(),
                    JuiceException.fromException(e, ctx.getChannel()));
        }
    }
	
	@Override
	public HttpResponse end() {
		flushResponse();
        return this;
	}

	@Override
	public void sendMessage(String message) {
		write(message);
	}
}
