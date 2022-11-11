package com.example.springbootesnew.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;


/**
 * ES的mapping类
 * ElasticSearchConfig
 *
 * @author tanghaorong
 */
@Data
@Configuration
@AllArgsConstructor
@NoArgsConstructor
public class FieldMappingConfiguration {
    private String field;
    /**
     * @string    text ,keyword
     * @Numeric   long, integer, short, byte, double, float, half_float, scaled_float
     * @date      date(分  datetime, timestamp   两种情况处理)
     * @Object   object
     */
    private String type;
    /**
     * 分词器选择  0. not_analyzed   1. ik_smart 2. ik_max_word
     */
    private Integer participle;

    /**
     * 当字段文本的长度大于指定值时，不做倒排索引
     * @return
     */
//    private Integer ignoreAbove;
}
