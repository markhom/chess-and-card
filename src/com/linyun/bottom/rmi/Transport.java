/**
 * Juice
 * com.juice.orange.game.rmi
 * Transport.java
 */
package com.linyun.bottom.rmi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import com.linyun.bottom.util.DateUtils;


/**
 * @author shaojieque 
 * 2013-4-15
 */
public class Transport implements Serializable {
	public static final String REMOTE_PREFIX = "Remote";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	private String id;
	private String clazz;
	private String method;
	private Class<?>[] paramTypes;
	private Object[] args;
	private Result result;
	private transient CountDownLatch latch;
	//
	public Transport() {
		this.id = DateUtils.generateRemoteId();
	}

	public String getId() {
		return id;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Class<?>[] getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Transport [id=" + id + ", clazz=" + clazz + ", method="
				+ method + ", paramTypes=" + Arrays.toString(paramTypes)
				+ ", args=" + Arrays.toString(args) + ", result=" + result
				+ "]";
	}
}
