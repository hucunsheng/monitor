package com.ai.base;

import com.ai.util.PageData;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MySqlPullDao extends AbstractBatisDao{

	/**
	 * 根据参数类型查询参数值
	 */
	public List<PageData> getSlowSQL(PageData param) throws Exception{
		return getSqlSessionTemplate().selectList(buildStatement("getSlowSQL"),param);
	}

}
