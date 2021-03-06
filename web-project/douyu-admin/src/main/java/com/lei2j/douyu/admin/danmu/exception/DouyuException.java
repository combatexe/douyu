/*
* Copyright (c) [2020] [jinjun lei]
* [douyu danmu] is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
* See the Mulan PSL v2 for more details.
*/

package com.lei2j.douyu.admin.danmu.exception;

public class DouyuException extends RuntimeException{

	private static final long serialVersionUID = 6310350082052887913L;

	public DouyuException(String message) {
		super(message);
	}
	
	public DouyuException(String message,Throwable cause) {
		super(message, cause);
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public synchronized Throwable getCause() {
		return super.getCause();
	}
}
