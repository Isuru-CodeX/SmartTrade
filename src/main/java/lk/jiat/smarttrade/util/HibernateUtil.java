package lk.jiat.smarttrade.util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;
    static {
        try {
            Configuration configuration = new Configuration().configure();

            // Read database password from environment variable for security
            String dbPassword = System.getenv("DB_PASSWORD");
            if (dbPassword != null && !dbPassword.trim().isEmpty()) {
                configuration.setProperty("hibernate.connection.password", dbPassword);
            }

            sessionFactory = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            throw new ExceptionInInitializerError("SessionFactory creation failed: "+e.getMessage());
        }
    }
    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }
    public static void shutdown(){
        sessionFactory.close();
    }
}
