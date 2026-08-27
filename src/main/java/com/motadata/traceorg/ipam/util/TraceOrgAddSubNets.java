package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by hardik on 17/7/18.
 */

public class TraceOrgAddSubNets implements Callable<Boolean>
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubNetScanner.class, "Add Subnet Process");

    private List<?> subnetIPs = new ArrayList<>();

    private SessionFactory sessionFactory;

    public TraceOrgAddSubNets(List<?> subnetIPs, SessionFactory sessionFactory)
    {
        this.subnetIPs = subnetIPs;

        this.sessionFactory = sessionFactory;
    }

    @Override
    public Boolean call() throws Exception
    {
        try
        {
            if(subnetIPs != null && sessionFactory != null)
            {
                Session session = sessionFactory.openSession();

                try
                {
                    session.setFlushMode(FlushMode.MANUAL);

                    session.getTransaction().begin();

                    subnetIPs.forEach(session::saveOrUpdate);

                    session.getTransaction().commit();

                    session.flush();

                    session.clear();
                }
                catch (Exception e)
                {
                    _logger.error(e);
                }
                finally
                {
                    if(session != null)
                    {
                        try
                        {
                            session.close();
                        }
                        catch (Exception ignored) {}
                    }

                    if(TraceOrgCommonUtil.getCSVImportCount()>0)
                    {
                        TraceOrgCommonUtil.decrementCSVImportCount();
                    }
                    else if(TraceOrgCommonUtil.getScanCount() >0 )
                    {
                        TraceOrgCommonUtil.decrementScanStatusCount();
                    }

                }
            }
        }
        catch (Exception e)
        {
            _logger.error(e);
        }

        return true;
    }
}