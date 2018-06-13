/**
 * Juice
 * com.juice.orange.game.util
 * ConnectionHelper.java
 */
package com.linyun.bottom.util;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;

import com.linyun.bottom.exception.JuiceException;

/**
 * @author shaojieque
 * 2013-3-20
 */
public abstract class ConnectionHelper {

    protected final Executor executor;
    protected final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler ioExceptionHandler;

    public ConnectionHelper(Executor executor, Thread.UncaughtExceptionHandler exceptionHandler, Thread.UncaughtExceptionHandler ioExceptionHandler) {
        this.ioExceptionHandler = ioExceptionHandler;
        this.executor = executor;
        this.exceptionHandler = exceptionHandler;
    }

    public void fireOnClose(final ChannelStateEvent e) {
        final Thread thread = Thread.currentThread();
        final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = juiceWrappingExceptionHandler(e.getChannel());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    fireOnClose();
                } catch (Throwable t) {
                    uncaughtExceptionHandler.uncaughtException(thread, t);
                }
            }
        });
    }

    public void fireConnectionException(final ExceptionEvent e) {
        if (e.getCause() instanceof ClosedChannelException) {
            e.getChannel().close();
        } else {
            final Thread thread = Thread.currentThread();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ioExceptionHandler.uncaughtException(thread, JuiceException.fromExceptionEvent(e));
                }
            });
        }
    }

    protected abstract void fireOnClose() throws Throwable;

    // Uncaught exception handler including the connection for context.
    protected Thread.UncaughtExceptionHandler juiceWrappingExceptionHandler(final Channel channel) {
        return new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                exceptionHandler.uncaughtException(t, JuiceException.fromException(e, channel));
            }
        };
    }
}