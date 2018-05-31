package com.ai.base;


import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class AbstractBatisDao {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired(required = false)
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * 获取默认的SqlSessionTemplate
     *
     * @return SqlSessionTemplate
     */
    public final SqlSessionTemplate getSqlSessionTemplate() {
        return this.sqlSessionTemplate;
    }

    /**
     * 默认采用类全名 + . + 方法名构建Mybatis声明的命名空间
     *
     * @param methodName 方法名
     * @return Mybatis声明的命名空间
     */
    protected String buildStatement(String methodName) {
        if (StringUtils.isNotEmpty(methodName) && !methodName.contains("."))
            methodName = "." + methodName;
        return getClass().getName() + methodName;
    }


}
