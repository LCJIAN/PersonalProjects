package com.lcjian.spunsugar.crawlers;

import cn.wanghaomiao.seimi.core.Seimi;

public class Boot {
    
    public static void main(String[] args) {
        Seimi s = new Seimi();
        s.goRun("BQfuliCrawler");
//        s.start("YingshidaquanCrawler");
    }
}
