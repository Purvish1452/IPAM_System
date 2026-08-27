package com.motadata.traceorg.ipam.services.impl;

import com.motadata.traceorg.ipam.dao.TraceOrgDao;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@org.springframework.stereotype.Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class  TraceOrgServiceImpl implements TraceOrgService
{
	
	@Autowired
	private TraceOrgDao traceOrgDao;

	@Override
	public TraceOrgUser findByUserName(String email)
	{
		return this.traceOrgDao.findByUserName(email);
	}

	@Override
	public boolean insert(Object object)
	{
		return this.traceOrgDao.insert(object);
	}

	@Override
	public boolean insertAll(List<?> objects)
	{
		return this.traceOrgDao.insertAll(objects);
	}

	@Override
	public boolean updateAll(List<?> objects)
	{
		return this.traceOrgDao.updateAll(objects);
	}

	@Override
	public List<?> commonQuery(String suffix, String prefix)
	{
		return this.traceOrgDao.commonQuery(suffix, prefix);
	}

	@Override
	public List<?> commonQuery(String prefix, int pageSize, int offset)
	{
		return this.traceOrgDao.commonQuery("", prefix, pageSize, offset);
	}

    @Override
    public List<?> commonQuery(String prefix)
    {
        return this.traceOrgDao.commonQuery("", prefix);
    }

    @Override
	public List<?> sqlQuery(String query)
	{
		return this.traceOrgDao.sqlQuery(query);
	}

	@Override
	public boolean sqlQueryAction(String query)
	{
		return this.traceOrgDao.sqlQueryAction(query);
	}

	@Override
	public void switchSafeUpdateMode(int onOffToggle) {
		this.traceOrgDao.sqlQueryAction("SET SQL_SAFE_UPDATES = " + onOffToggle);
	}

	@Override
	public boolean isExist(String voName, String paramName,String paramValue)
	{
		return this.traceOrgDao.isExist(voName, paramName, paramValue);
	}

	@Override
	public Object getById(String voName, Long id)
	{
		return this.traceOrgDao.getById(voName, id);
	}

	@Override
	public boolean delete(String voName,String paramName,String paramValue)
	{
		return this.traceOrgDao.delete(voName,paramName,paramValue);
	}

}
