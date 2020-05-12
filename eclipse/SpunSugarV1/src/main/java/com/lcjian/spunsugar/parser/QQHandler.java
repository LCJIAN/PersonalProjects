package com.lcjian.spunsugar.parser;

public class QQHandler {

	public static String getLiveUrl(String key){
		String liveurl = null;
		liveurl = "http://zb.v.qq.com:1863/?progid="+key;
		return liveurl;
	}
}
