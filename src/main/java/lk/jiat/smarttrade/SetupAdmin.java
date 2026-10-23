package lk.jiat.smarttrade;

import lk.jiat.smarttrade.entity.Status;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SetupAdmin {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Check if admin already exists
            User admin = session.createQuery("FROM User WHERE email = 'admin@gmail.com'", User.class).uniqueResult();
            
            if (admin == null) {
                admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEmail("admin@gmail.com");
                admin.setPassword("admin@123");
                admin.setVerificationCode("");
                admin.setAdmin(true);
                
                Status status = session.createQuery("FROM Status WHERE value = 'VERIFIED'", Status.class).uniqueResult();
                if (status == null) {
                    status = session.createQuery("FROM Status WHERE value = 'ACTIVE'", Status.class).uniqueResult();
                }
                admin.setStatus(status);
                
                session.save(admin);
                System.out.println("Admin user admin@gmail.com created successfully.");
            } else {
                admin.setPassword("admin@123");
                admin.setAdmin(true);
                session.update(admin);
                System.out.println("Admin user admin@gmail.com updated with password and isAdmin=true.");
            }
            tx.commit();
        }
        System.exit(0);
    }
}
