package com.centit.product.dataopt.bizopt;

import com.centit.product.dataopt.core.SimpleDataSet;
import com.centit.product.dataopt.dataset.HttpDataSet;
import com.centit.support.algorithm.CollectionsOpt;

public class FileTest {
    public static void main(String[] args) throws Exception {
        HttpDataSet httpDataSet = new HttpDataSet("http://ceshi.centit.com/fh/api/metaform/formaccess/fJceWCF7Q6aLvGGUlMilQg/list");
        SimpleDataSet dataSet=httpDataSet.load(CollectionsOpt.createHashMap("title","2020.3.25雨花推市局"));

        System.out.println("");
    }

}
