/**
 * Juice
 * com.juice.orange.game.rmi
 * Result.java
 */
package com.linyun.bottom.rmi;

import java.io.Serializable;

/**
 * @author shaojieque 
 * 2013-4-16
 */
public class Result implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	private String id;
	private Object result;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Result [id=" + id + ", result=" + result + "]";
	}
}
