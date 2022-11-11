package com.example.springbootesnew.util;

import com.example.springbootesnew.config.ElasticSearchConfiguration;
import com.example.springbootesnew.config.FieldMappingConfiguration;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


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
public class EsUtil {
    @Autowired
    private ElasticSearchConfiguration elasticSearchConfiguration;

    public RestHighLevelClient restHighLevelClient() {
        return elasticSearchConfiguration.restHighLevelClient();
    }

    // ===================== 索引操作（封装在Index内部类中） ============================

    /**
     * 创建索引（同步）
     *
     * @param indexName
     * @return true：成功，false：失败
     */
    @SneakyThrows
    public Boolean createSync(String indexName , List<FieldMappingConfiguration> fieldMappingList) {
        // 索引名称转为小写
        String iName = indexName.toLowerCase(Locale.ROOT);
        //String iName = indexName;

        // 获取【索引客户端对象】
        RestHighLevelClient client = restHighLevelClient();

        /**
         * ===== 判断目标索引是否存在（等价于下面的Lambda写法）=====
         */
        boolean flag = indexExists(iName , client);
        boolean result = true;

        if (flag) {
            // 目标索引已存在
            //System.out.println("索引【" + indexName + "】已存在！");
            log.info("索引【" + iName + "】已存在！");
        } else {
            // 不存在
            // 获取【创建索引请求对象】
            //CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(indexName).build();
            // 创建索引，得到【创建索引响应对象】
            //CreateIndexResponse createIndexResponse = indexClient.create(createIndexRequest);
            //createIndexResponse = indexClient.create(req -> req.index(indexName));
            result = createIndexAndCreateMapping(iName, "", fieldMappingList, client, 1, 1);

            //System.out.println("创建索引响应对象：" + createIndexResponse);
            if (result) {
                log.info("索引【" + iName + "】创建成功！");
            } else {
                log.info("索引【" + iName + "】创建失败！");
            }
        }

        return result;
    }

    /**
     * 判断索引库是否存在
     */
    public boolean indexExists(String indexName , RestHighLevelClient client) throws Exception {
        IndicesClient indicesClient = client.indices();
        // 创建get请求
        GetIndexRequest request = new GetIndexRequest(indexName);
        // 判断索引库是否存在
        boolean result = indicesClient.exists(request, RequestOptions.DEFAULT);
        System.out.println(result);
        return result;
    }

    // ===================== 基础操作（仅供内部调用） ============================

    /**
     * 动态创建索引
     * 根据信息自动创建索引与mapping
     * 构建mapping描述
     *
     * @param index              索引名称
     * @param type               类型名称
     * @param fieldMappingList   字段信息
     * @param client             es客户端
     * @param number_of_shards   分片数
     * @param number_of_replicas 副本数
     * @return
     */
    public static boolean createIndexAndCreateMapping(String index, String type, List<FieldMappingConfiguration> fieldMappingList, RestHighLevelClient client, Integer number_of_shards, Integer number_of_replicas) {
        XContentBuilder mapping = null;
        try {
            CreateIndexRequest indexRequest = new CreateIndexRequest(index);
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties"); //设置之定义字段
            for (FieldMappingConfiguration info : fieldMappingList) {
                String field = info.getField();
                String dateType = info.getType();
                if (dateType == null || "".equals(dateType.trim())) {
                    dateType = "string";
                }
                dateType = dateType.toLowerCase();
                int participle = info.getParticiple();
                if ("string".equals(dateType)) {
                    if (participle == 0) {
                        mapping.startObject(field)
                                .field("type", "keyword")
                                .field("index", false)
//                                .field("ignore_above", info.getIgnoreAbove())
                                .endObject();
                    } else if (participle == 1) {
                        mapping.startObject(field)
                                .field("type", "text")
                                .field("analyzer", "ik_smart")
                                .endObject();
                    } else if (participle == 2) {
                        mapping.startObject(field)
                                .field("type", "text")
                                .field("analyzer", "ik_max_word")
                                .endObject();
                    }
                } else if ("datetime".equals(dateType)) {
                    mapping.startObject(field)
                            .field("type", "date")
                            .field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
                            .endObject();
                } else if ("timestamp".equals(dateType)) {
                    mapping.startObject(field)
                            .field("type", "date")
                            .field("format", "strict_date_optional_time||epoch_millis")
                            .endObject();
                } else if ("integer".equals(dateType)) {
                    mapping.startObject(field)
                            .field("type", "integer")
                            .endObject();
                }else if ("float".equals(dateType) || "double".equals(dateType)) {
                    mapping.startObject(field)
                            .field("type", "scaled_float")
                            .field("scaling_factor", 100)
                            .endObject();
                } else {
                    mapping.startObject(field)
                            .field("type", dateType)
                            .field("index", true)
                            .endObject();
                }
            }
            mapping.endObject()
                    .endObject();
            HashMap<String, Object> settings_map = new HashMap<>(4);
            settings_map.put("number_of_shards", number_of_shards);
            settings_map.put("number_of_replicas", number_of_replicas);
            indexRequest.settings(settings_map).mapping(type, mapping);
            // 请求服务器
            CreateIndexResponse response = client.indices().create(indexRequest, RequestOptions.DEFAULT);

            System.out.println(response.isAcknowledged());
            return true;
        } catch (IOException e) {
            log.error("根据信息自动创建索引与mapping创建失败，失败信息为:{" + e.getMessage() + "}");
            return false;
        }
    }
}

