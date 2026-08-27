package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;

@SuppressWarnings("ALL")
public class TraceOrgCronExpressionManager
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgCronExpressionManager.class, "Cron Expression Manager");

    public String getCronExpression()
    {

        String minutes;

        String time;

        String hour;

        String cronExpression = "";

        String date;

        String day;

        String[] hourMinute;

        String[] partialHourMinute;

        try
        {

            if (isRecurring())
            {
                time = getTime();

                hourMinute = time.split("\\:");

                partialHourMinute = hourMinute[1].split("\\ ");

                minutes = partialHourMinute[0];

                hour = hourMinute[0];

                date = getDate();

                String[] dateArray = date.split("\\-");

                day = dateArray[2];

                if (day.charAt(0) == '0')
                {
                    day = day.substring(1);
                }

                String month = dateArray[1];

                if (month.charAt(0) == '0')
                {
                    month = month.substring(1);
                }

                String year = dateArray[0];

                cronExpression = "0 " + minutes + " " + hour + " " + day + " " + month + " ? " + year;

            }

            else if (getTime() != null)
            {
                StringBuilder cronBuilder = new StringBuilder();

                time = getTime();

                for (String timeValue : time.split(","))
                {
                    hourMinute = timeValue.split("\\:");

                    partialHourMinute = hourMinute[1].split(" ");

                    minutes = partialHourMinute[0];

                    hour = hourMinute[0];

                    if (isDaily())
                    {

                        cronExpression = "0 " + minutes + " " + hour + " ? * *";

                        cronBuilder.append(cronExpression).append(TraceOrgCommonConstants.LINK_SEPARATOR);

                    }
                    else if (isHourly())
                    {
                        cronExpression = "0 " + minutes + "/" + 60 + " * * * ?";

                        cronBuilder.append(cronExpression).append(TraceOrgCommonConstants.LINK_SEPARATOR);
                    }

                    else if (isWeekly())
                    {

                        cronExpression = "0 " + minutes + " " + hour + " ? * " + getWeekDay();

                        cronBuilder.append(cronExpression).append(TraceOrgCommonConstants.LINK_SEPARATOR);

                    }

                    else if (isMonthly())
                    {

                        for (String dayValue : getDay().split(","))
                        {

                            if (getMonth() != null)
                            {

                                for (String monthValue : getMonth().split(","))
                                {

                                    cronExpression = "0 " + minutes + " " + hour + " " + dayValue + " " + monthValue + " ? *";

                                    cronBuilder.append(cronExpression).append(TraceOrgCommonConstants.LINK_SEPARATOR);

                                }
                            }
                            else
                            {
                                cronExpression = "0 " + minutes + " " + hour + " " + dayValue + " " + " * ? *";

                                cronBuilder.append(cronExpression).append(TraceOrgCommonConstants.LINK_SEPARATOR);

                            }

                        }

                    }

                }

                if (cronBuilder.length() > 0)
                {
                    cronBuilder.delete(cronBuilder.length() - TraceOrgCommonConstants.LINK_SEPARATOR.length(), cronBuilder.length());
                }

                cronExpression = cronBuilder.toString();

            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return cronExpression;
    }

    private boolean m_monthly;
    private boolean m_hourly;
    private boolean m_weekly;
    private boolean m_daily;
    private boolean m_recurring;

    private String getWeekDay()
    {
        return m_weekDay;
    }

    public void setWeekDay(String weekDay)
    {
        this.m_weekDay = weekDay;
    }

    private String m_weekDay;
    private String m_date;
    private String m_time;
    private String m_day;
    private String m_minute;

    private String getMonth()
    {
        return m_month;
    }

    public void setMonth(String month)
    {
        this.m_month = month;
    }

    private String m_month;

    private boolean isHourly()
    {
        return m_hourly;
    }

    public void setHourly(boolean hourly)
    {
        this.m_hourly = hourly;
    }

    private boolean isWeekly()
    {
        return m_weekly;
    }

    public void setWeekly(boolean weekly)
    {
        this.m_weekly = weekly;
    }

    private boolean isMonthly()
    {
        return m_monthly;
    }

    public void setMonthly(boolean monthly)
    {
        this.m_monthly = monthly;
    }

    private String getDay()
    {
        return m_day;
    }

    public void setDay(String day)
    {
        this.m_day = day;
    }

    private String getDate()
    {
        return m_date;
    }

    public void setDate(String date)
    {
        this.m_date = date;
    }

    private String getTime()
    {
        return m_time;
    }

    public void setTime(String time)
    {
        this.m_time = time;
    }

    private boolean isDaily()
    {
        return m_daily;
    }

    public void setDaily(boolean daily)
    {
        this.m_daily = daily;
    }

    private boolean isRecurring()
    {
        return m_recurring;
    }

    public void setRecurring(boolean recurring)
    {
        this.m_recurring = recurring;
    }


}
