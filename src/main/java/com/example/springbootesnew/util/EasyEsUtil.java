package com.example.springbootesnew.util;

import com.example.springbootesnew.config.ElasticSearchConfiguration;
import com.xpc.easyes.core.conditions.LambdaEsIndexWrapper;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.index.mapper.ParseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * <p>ES操作工具类</p>
 * -----------------------------
 * 调用异步客户端就是异步方法；
 * 异步方法也要关闭transport；
 * 如果连接太多，又没有及时关闭，会报异常
 * <p>
 * 默认：异步方法
 * 索引名称/Id：一律转为小写
 * 文档Id：可以为大写，无须转换
 * -----------------------------
 *
 * @author 土味儿
 * Date 2022/8/9
 * @version 1.0
 */
@SuppressWarnings("all")
@Component
@Log4j2
public class EasyEsUtil {
    @Autowired
    private ElasticSearchConfiguration elasticSearchConfiguration;

    public void createIndex(String indexName , Map<String , Object> mapping){
        LambdaEsIndexWrapper<ParseContext.Document> wrapper = new LambdaEsIndexWrapper<>();

        wrapper.indexName(indexName.toLowerCase());

        wrapper.mapping(mapping);

        // 设置分片及副本信息,3个shards,2个replicas,可缺省
        wrapper.settings(3,2);
    }
}

