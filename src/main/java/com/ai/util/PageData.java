package com.ai.util;

import org.apache.ibatis.type.Alias;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Alias("pd")
public class PageData extends HashMap<Object, Object> implements Map<Object, Object> {
	
	private static final long serialVersionUID = 1L;
	
	private Map<Object, Object> map = null;
	
	private HttpServletRequest request;
	
	private Object page;
	
	public PageData(HttpServletRequest request){
		this.request = request;
		if(map == null){
			map = new HashMap<Object, Object>();
		}
		Map<String, String[]> properties = request.getParameterMap();
		for(Entry<String, String[]> entry : properties.entrySet()){
			String [] values = entry.getValue();
			StringBuilder value = new StringBuilder();
			for(int i = 0; i < values.length; i++){
				value.append(values[i]);
				if(i < values.length - 1){
					value.append(",");
				}
			}
			map.put(entry.getKey(), value.toString());
		}
	}

	public PageData() {
		if(map == null){
			map = new HashMap<Object, Object>();
		}
	}

	public Object get(Object key) {
		Object obj = null;
		if(map.get(key) instanceof Object[]) {
			Object[] arr = (Object[])map.get(key);
			obj = request == null ? arr:(request.getParameter((String)key) == null ? arr:arr[0]);
		} else {
			obj = map.get(key);
		}
		return obj;
	}

	public String getString(Object key) {
		Object value = get(key);
		if(value != null){
			return value.toString();
		}
		return null;
	}

	public Object put(Object key, Object value) {
		return map.put(key, value);
	}

	public Object remove(Object key) {
		return map.remove(key);
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Set<Entry<Object, Object>> entrySet() {
		return map.entrySet();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<Object> keySet() {
		return map.keySet();
	}

	public void putAll(Map<?, ?> t) {
		map.putAll(t);
	}

	public int size() {
		return map.size();
	}

	public Collection<Object> values() {
		return map.values();
	}

	public Object getPage() {
		return page;
	}

	public void setPage(Object page) {
		this.page = page;
	}

	
}
