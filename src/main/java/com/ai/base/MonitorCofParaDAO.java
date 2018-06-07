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

	/**
	 * 分页查询全部有效监控
	 * @return
	 */
	public List<PageData> queryMonitorByPage(PageData pd) {

		return this.getSqlSessionTemplate().selectList("queryMonitorByPage",pd);
	}


	/**
	 * id查询全部有效监控
	 * @return
	 */
	public PageData queryMonitorById(PageData pd) {

		return this.getSqlSessionTemplate().selectOne("queryMonitorById",pd);
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

	public Integer insertMonitor(PageData pd) {
		Integer result = (Integer) this.getSqlSessionTemplate().insert("insertMonitor",pd);
		Integer id = Integer.valueOf(String.valueOf(pd.get("MONITOR_CON_PARA_ID")));
		return id;
	}
	public Integer insertMonitorSql(PageData pd) {

		return (Integer) this.getSqlSessionTemplate().insert("insertMonitorSql",pd);
	}
	public Integer queryMonitorByCount(PageData pd) {

		return (Integer) this.getSqlSessionTemplate().selectOne("queryMonitorByCount",pd);
	}

	public Integer updateMonitorById(PageData pd) {

		return (Integer)this.getSqlSessionTemplate().update("updateMonitorById",pd);
	}
	public Integer updateMonitorItemById(PageData pd) {

		return (Integer)this.getSqlSessionTemplate().update("updateMonitorItemById",pd);
	}
}
