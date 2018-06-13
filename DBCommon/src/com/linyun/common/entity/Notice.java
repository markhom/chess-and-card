package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class Notice  implements Serializable{
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id ;
	 private String content ; //公告内容
	 private Timestamp createTime ;//创建时间
	 private Timestamp updateTime ;//修改时间
	 
	 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	 
	 

}
