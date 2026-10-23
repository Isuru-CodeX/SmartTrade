package lk.jiat.smarttrade.listener;

import lk.jiat.smarttrade.provider.MailServiceProvider;
import lk.jiat.smarttrade.util.HibernateUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/** Registered in WEB-INF/web.xml. Starts / stops the mail worker and closes Hibernate on undeploy. */
public class ContextPathListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MailServiceProvider.getInstance().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MailServiceProvider.getInstance().shutdown();
        try {
            HibernateUtil.shutdown();
        } catch (Throwable ignored) {
            // SessionFactory was never created or is already closed
        }
    }
}
