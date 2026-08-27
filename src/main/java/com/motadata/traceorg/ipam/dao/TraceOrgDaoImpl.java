package com.motadata.traceorg.ipam.dao;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.util.TraceOrgAddSubNets;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgTaskExecutor;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Transactional
@Repository
public class TraceOrgDaoImpl implements TraceOrgDao
{
	private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDaoImpl.class, "Dao Impl");

	@Autowired
	private SessionFactory sessionFactory;

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public TraceOrgUser findByUserName(String email)
	{
		TraceOrgUser traceOrgUser = null;

		try
		{
			List<TraceOrgUser> traceOrgUsers = sessionFactory.getCurrentSession().createQuery("from TraceOrgUser where userName=?")
					.setParameter(0, email).list();

			if (traceOrgUsers != null && !traceOrgUsers.isEmpty())
			{
				traceOrgUser = traceOrgUsers.get(0);
			}
			else
			{
				_logger.debug("user is null..");
			}
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}

		return traceOrgUser;
	}

	@Override
	public boolean insert(Object object)
	{
		boolean result = false;

		try
		{
			Session session = this.sessionFactory.getCurrentSession();

			session.setCacheMode(CacheMode.IGNORE);

			session.setFlushMode(FlushMode.COMMIT);

			session.saveOrUpdate(object);

			result = true;
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}

		return result;
	}

	@Override
	public boolean insertAll(List<?> objects)
	{
		boolean result = false;

		try
		{
			int incremental = TraceOrgCommonConstants.BATCH_SIZE;

			List<Object> partialSubNets = new ArrayList<>();

			for(int index = 0; index < objects.size(); index ++)
			{
				try
				{
					if(index % (incremental) == 0)
					{
						TraceOrgCommonUtil.incrementCSVImportCount();

						TraceOrgTaskExecutor.executeImportCSVTask(new TraceOrgAddSubNets(new ArrayList<>(partialSubNets), sessionFactory));

						partialSubNets.clear();
					}

					partialSubNets.add(objects.get(index));
				}
				catch (Exception exception)
				{
					_logger.error(exception);
				}
			}

			if(partialSubNets.size() > 0)
			{
				TraceOrgCommonUtil.incrementCSVImportCount();

				TraceOrgTaskExecutor.executeImportCSVTask(new TraceOrgAddSubNets(new ArrayList<>(partialSubNets), sessionFactory));
			}

			result = true;
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}

		return result;
	}

	@Override
	public boolean updateAll(List<?> objects)
	{
		boolean result = false;

		try
		{
			int incremental = 20000;

			List<Object> partialSubNets = new ArrayList<>();

			for(int index = 0; index < objects.size(); index ++)
			{
				try
				{
					if(index % (incremental) == 0)
					{
						TraceOrgCommonUtil.incrementScanStatusCount();

						TraceOrgTaskExecutor.executeScanTask(new TraceOrgAddSubNets(new ArrayList<>(partialSubNets), sessionFactory));

						partialSubNets.clear();
					}

					partialSubNets.add(objects.get(index));
				}
				catch (Exception exception)
				{
					_logger.error(exception);
				}
			}

			if(partialSubNets.size() > 0)
			{
				TraceOrgCommonUtil.incrementScanStatusCount();

				TraceOrgTaskExecutor.executeScanTask(new TraceOrgAddSubNets(new ArrayList<>(partialSubNets), sessionFactory));
			}

			result = true;
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}

		return result;
	}

	@Override
	public List<?> commonQuery(String suffix,String prefix)
	{
		List<?> result = null;

		try
		{
			Session session = sessionFactory.getCurrentSession();

			Query query =session.createQuery(suffix+" from "+prefix).setMaxResults(70000);

			result = query.list();
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}

		return result;
	}

	/**
	 * IPAM-142 : IPAM | Alert notification for Monitor IP capacity and receive alerts on IP depletion
	 * Added a method to set offset and limit for server-side pagination
	 * */
	@Override
	public List<?> commonQuery(String suffix, String prefix, int pageSize, int offset)
	{
		List<?> result = null;

		try
		{
			Session session = sessionFactory.getCurrentSession();

			Query query = session.createQuery(suffix + " from " + prefix);

			query.setFirstResult(offset);  // Set the OFFSET (starting point)

			query.setMaxResults(pageSize);  // Set the LIMIT (maximum number of results)

			result = query.list();

		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}

		return result;
	}

	@Override
	public List<?> sqlQuery(String query)
	{
		List<?> result = null;

		try
		{
			Session session = sessionFactory.getCurrentSession();


			SQLQuery sqlQuery = session.createSQLQuery(query);

			result = sqlQuery.list();

		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
		return result;
	}


	@Override
	public boolean isExist(String voName, String paramName, String paramValue)
	{
		try
		{
			Session session = sessionFactory.getCurrentSession();

			Query query =session.createQuery(" from " + voName + " where " + paramName + " = '" + paramValue + "'");

			List<?> list = query.list();

			return list != null && !list.isEmpty();
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
		return false;
	}

	@Override
	public Object getById(String voName, Long id)
	{
		Object result = null;

		try
		{
			Session session = sessionFactory.getCurrentSession();

			Query query =session.createQuery(" from " + voName + " where id = " +id);

			List<?> list = query.list();

			if(list != null && !list.isEmpty())
			{
				result = list.get(0);
			}
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}

		return result;
	}

	@Override
	public boolean delete(String voName,String paramName,String paramValue)
	{
		boolean result = false;

		try
		{
			Session session =this.sessionFactory.getCurrentSession();

			Query query = session.createQuery("Delete from "+voName+" where "+ paramName + " = '" + paramValue + "'");

			result = query.executeUpdate() > 0;
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
		return result;
	}

	@Override
	public boolean sqlQueryAction(String query)
	{
		boolean result = false;

		try
		{
			Session session = sessionFactory.getCurrentSession();

			Query queryDelete = session.createSQLQuery(query);

			result = queryDelete.executeUpdate() > 0;
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
		return result;
	}

	@Override
	@Transactional
	public void removeKeyFromCustomColumns(String keyToRemove) {
		entityManager.createNativeQuery("SET SQL_SAFE_UPDATES = 0").executeUpdate();

		Query query = entityManager.createNativeQuery(
				"UPDATE subnet_ip_details " +
						"SET custom_columns = JSON_REMOVE(custom_columns, CONCAT('$.', :key)) " +
						"WHERE JSON_CONTAINS_PATH(custom_columns, 'one', CONCAT('$.', :key)) " +
						"AND id IS NOT NULL"
		).unwrap(Query.class); // Unwrap to Hibernate Query in Hibernate 6.x

		query.setParameter("key", keyToRemove);
		query.executeUpdate();

		entityManager.createNativeQuery("SET SQL_SAFE_UPDATES = 1").executeUpdate();
	}


}