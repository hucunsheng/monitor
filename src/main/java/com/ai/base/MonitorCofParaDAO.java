package com.ai.base;

import com.ai.util.PageData;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class MonitorCofParaDAO extends AbstractBatisDao{


	/**
	 * 查询全部有效监控
	 * @return
	 */
	public List<PageData> queryMonitorCofParas() {

		return this.getSqlSessionTemplate().selectList("queryMonitorCofParas");
	}

	public Double queryMonitorResult(PageData pd) {

		return (Double)this.getSqlSessionTemplate().selectOne("queryDataCount",pd);
	}

	public Integer updateMonitorCofParas(PageData pd) {

		return (Integer)this.getSqlSessionTemplate().update("updateMonitorCofParas",pd);
	}


	public List<PageData> queryMonitorRecords(PageData pd) {

		return this.getSqlSessionTemplate().selectList("queryMonitorRecords",pd);
	}

	public Integer insertMonitorRecord(PageData pd) {

		return (Integer) this.getSqlSessionTemplate().insert("insertMonitorRecord",pd);
	}

	public Integer updateMonitorRecord(PageData pd) {

		return (Integer)this.getSqlSessionTemplate().update("updateMonitorRecord",pd);
	}

}
