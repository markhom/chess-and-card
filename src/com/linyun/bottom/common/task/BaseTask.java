/**
 * 
 */
package com.linyun.bottom.common.task;

import com.linyun.bottom.schedule.Task;

/**
 * @author queshaojie
 * 
 *         lewan
 */
public class BaseTask implements Task {
	private Object id;

	//
	@Override
	public void run() {

	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void setId(Object id) {
		this.id = id;
	}
}
