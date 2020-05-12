package com.lcjian.parser.tv;

public class TVParser {

    public String parse(String type, String url) {
        if ("cntv".equals(type)) {
            return new CntvParser().parse(url);
        }
        return null;
    }
}
