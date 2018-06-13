/**
 * Juice
 * com.juice.orange.game.util
 * ClassUtils.java
 */
package com.linyun.bottom.util;

import com.linyun.bottom.exception.JuiceException;

/**
 * @author shaojieque 
 * 2013-4-16
 */
public class ClassUtils {
	public static Object getObject(String clazz) throws JuiceException {
		clazz = clazz.concat("Impl");
		String clazzImpl = clazz.replace("action", "action.impl");
		try {
			Class<?> clazzImpls = Class.forName(clazzImpl);
			return clazzImpls.newInstance();
		} catch (Exception e) {
			throw new JuiceException("can't not instance:"+clazzImpl);
		}
	}
}
