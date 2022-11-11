package com.example.springbootesnew;

import com.example.springbootesnew.config.FieldMappingConfiguration;
import com.example.springbootesnew.util.EsUtil;
import com.xpc.easyes.autoconfig.annotation.EsMapperScan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@EsMapperScan("com.example.springbootesnew.mapper.es")
class SpringbootEsNewApplicationTests {
    @Autowired
    private EsUtil esUtil;

    @Test
    void contextLoads() {
        List<FieldMappingConfiguration> fieldMappingList = new ArrayList<>();
        fieldMappingList.add(new FieldMappingConfiguration("name" , "string" , 2));
        fieldMappingList.add(new FieldMappingConfiguration("age" , "integer" , 0 ));

        boolean res = esUtil.createIndexAndCreateMapping("person", "_doc", fieldMappingList, esUtil.restHighLevelClient(), 1, 0);

        System.out.println(res);
    }

}
