package com.linyun.common.vo;

import java.io.Serializable;

import com.linyun.common.entity.User;

public class UserVO  implements  Serializable{

	private static final long serialVersionUID = 1L;
	
	private User user ;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
    
}
