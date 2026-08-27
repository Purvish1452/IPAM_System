package com.motadata.traceorg.ipam.dao;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;

import java.util.List;

/**
 * @author Krunal Thakkar
 *
 */

public interface TraceOrgDao
{

	TraceOrgUser findByUserName(String email);
	
	boolean insert(Object object);

    boolean insertAll(List<?> objects);

	boolean updateAll(List<?> objects);
	
	List<?> commonQuery(String suffix, String prefix);

    List<?> commonQuery(String suffix, String prefix, int pageSize, int offset);

    List<?> sqlQuery(String query);

	boolean sqlQueryAction(String query);
	
	boolean isExist(String voName, String paramName, String paramValue);
	
	Object getById(String voName, Long id);

	boolean delete(String voName, String paramName, String paramValue);

	void removeKeyFromCustomColumns(String keyToRemove);
}