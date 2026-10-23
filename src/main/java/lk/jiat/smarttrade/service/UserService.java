package lk.jiat.smarttrade.service;

import com.google.gson.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import lk.jiat.smarttrade.dto.UserDTO;
import lk.jiat.smarttrade.entity.Status;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.mail.PasswordResetMail;
import lk.jiat.smarttrade.mail.VerificationMail;
import lk.jiat.smarttrade.provider.MailServiceProvider;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import lk.jiat.smarttrade.validation.Validator;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserService {

    public String userLogin(UserDTO userDTO, @Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (userDTO == null || userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
            message = "Email address is required!";
        } else if (!userDTO.getEmail().trim().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide a valid email address!";
        } else if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            message = "Password is required!";
        } else {
            String normalizedEmail = userDTO.getEmail().trim().toLowerCase();
            Session hibernateSession = null;
            try {
                hibernateSession = HibernateUtil.getSessionFactory().openSession();
                User singleUser = hibernateSession.createQuery(
                        "FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                        .setParameter("email", normalizedEmail)
                        .uniqueResult();

                if (singleUser == null) {
                    message = "Account not found. Please register first!";
                } else if (!singleUser.getPassword().equals(userDTO.getPassword())) {
                    message = "Invalid email or password. Please check your login credentials!";
                } else {
                    Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                            .getSingleResult();

                    if (!singleUser.getStatus().equals(verifiedStatus)) {
                        message = "Your account is not verified. Please verify your account first!";
                    } else {
                        HttpSession oldSession = request.getSession(false);
                        if (oldSession != null) {
                            oldSession.invalidate();
                        }
                        HttpSession httpSession = request.getSession(true);
                        httpSession.setAttribute("user", singleUser);
                        status = true;
                        message = "Login successful";
                        responseObject.addProperty("userId", singleUser.getId());
                        responseObject.addProperty("email", singleUser.getEmail());
                        responseObject.addProperty("name", singleUser.getFirstName() + " " + singleUser.getLastName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "Database error. Please try again later!";
            } finally {
                if (hibernateSession != null && hibernateSession.isOpen()) {
                    hibernateSession.close();
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String verifyUserAccount(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (userDTO == null || userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
            message = "Email address is required!";
        } else if (!userDTO.getEmail().trim().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide a valid email address!";
        } else if (userDTO.getVerificationCode() == null || userDTO.getVerificationCode().trim().isBlank()) {
            message = "Verification code is required!";
        } else if (!userDTO.getVerificationCode().trim().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Please provide a valid 6-digit verification code!";
        } else {
            String normalizedEmail = userDTO.getEmail().trim().toLowerCase();
            String code = userDTO.getVerificationCode().trim();
            Session hibernateSession = null;
            try {
                hibernateSession = HibernateUtil.getSessionFactory().openSession();
                User user = hibernateSession.createQuery(
                        "FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.verificationCode = :verificationCode", User.class)
                        .setParameter("email", normalizedEmail)
                        .setParameter("verificationCode", code)
                        .uniqueResult();

                if (user == null) {
                    message = "Invalid email or verification code. Please check and try again!";
                } else {
                    Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                            .getSingleResult();

                    if (user.getStatus().equals(verifiedStatus)) {
                        message = "Account already verified!";
                        status = true;
                    } else {
                        user.setStatus(verifiedStatus);
                        user.setVerificationCode("");
                        Transaction transaction = hibernateSession.beginTransaction();
                        try {
                            hibernateSession.merge(user);
                            transaction.commit();
                            status = true;
                            message = "Account verification completed!";
                        } catch (Exception e) {
                            if (transaction.isActive()) {
                                transaction.rollback();
                            }
                            e.printStackTrace();
                            message = "Something went wrong. Verification process failed!";
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "Database error. Please try again later!";
            } finally {
                if (hibernateSession != null && hibernateSession.isOpen()) {
                    hibernateSession.close();
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addNewUser(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (userDTO == null) {
            message = "User data is missing!";
        } else if (userDTO.getFirstName() == null || userDTO.getFirstName().trim().isBlank()) {
            message = "First name is required!";
        } else if (userDTO.getFirstName().trim().length() > 45) {
            message = "First name must not exceed 45 characters!";
        } else if (userDTO.getLastName() == null || userDTO.getLastName().trim().isBlank()) {
            message = "Last name is required!";
        } else if (userDTO.getLastName().trim().length() > 45) {
            message = "Last name must not exceed 45 characters!";
        } else if (userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
            message = "Email address is required!";
        } else if (userDTO.getEmail().trim().length() > 150) {
            message = "Email must not exceed 150 characters!";
        } else if (!userDTO.getEmail().trim().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide a valid email address!";
        } else if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            message = "Password is required!";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "The password must be 8-20 characters long and include at least one uppercase letter, " +
                    "one lowercase letter, one digit, and one special character";
        } else {
            String normalizedEmail = userDTO.getEmail().trim().toLowerCase();
            String trimmedFirstName = userDTO.getFirstName().trim();
            String trimmedLastName = userDTO.getLastName().trim();

            Session hibernateSession = null;
            try {
                hibernateSession = HibernateUtil.getSessionFactory().openSession();
                User singleUser = hibernateSession.createQuery(
                        "FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                        .setParameter("email", normalizedEmail)
                        .uniqueResult();

                if (singleUser != null) {
                    message = "This email already exists! Please use another email";
                } else {
                    User u = new User();
                    u.setFirstName(trimmedFirstName);
                    u.setLastName(trimmedLastName);
                    u.setEmail(normalizedEmail);
                    u.setPassword(userDTO.getPassword());

                    String verificationCode = AppUtil.generateCode();
                    u.setVerificationCode(verificationCode);

                    Status pendingStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.PENDING)).getSingleResult();

                    u.setStatus(pendingStatus);

                    Transaction transaction = hibernateSession.beginTransaction();
                    try {
                        hibernateSession.persist(u);
                        transaction.commit();

                        try {
                            VerificationMail verificationMail = new VerificationMail(u.getEmail(), verificationCode);
                            MailServiceProvider.getInstance().sendMail(verificationMail);
                        } catch (Exception mailEx) {
                            System.err.println("Failed to queue verification email: " + mailEx.getMessage());
                        }

                        status = true;
                        message = "Account created successfully. Verification code has been sent to your email. " +
                                "Please verify it to activate your account!";
                    } catch (Exception e) {
                        if (transaction.isActive()) {
                            transaction.rollback();
                        }
                        e.printStackTrace();
                        message = "Account creation failed. Please try again!";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "Database error. Please try again later!";
            } finally {
                if (hibernateSession != null && hibernateSession.isOpen()) {
                    hibernateSession.close();
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String sendPasswordResetCode(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (userDTO == null || userDTO.getEmail() == null) {
            message = "Email is required!";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email address cannot be empty!";
        } else if (!userDTO.getEmail().trim().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide a valid email address!";
        } else {
            try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                User user = hibernateSession.createQuery("FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                        .setParameter("email", userDTO.getEmail().trim())
                        .uniqueResult();

                if (user == null) {
                    message = "No account found with this email address!";
                } else {
                    String resetCode = AppUtil.generateCode();
                    user.setVerificationCode(resetCode);

                    Transaction tx = hibernateSession.beginTransaction();
                    try {
                        hibernateSession.update(user);
                        tx.commit();

                        try {
                            PasswordResetMail mail = new PasswordResetMail(user.getEmail(), resetCode);
                            MailServiceProvider.getInstance().sendMail(mail);
                        } catch (Exception e) {
                            System.err.println("Failed to send reset email: " + e.getMessage());
                        }

                        status = true;
                        message = "A 6-digit verification code has been sent to your email!";
                    } catch (Exception e) {
                        tx.rollback();
                        message = "Failed to generate reset code. Please try again!";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "Database error. Please try again later!";
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String resetPassword(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (userDTO == null || userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            message = "Email is required!";
        } else if (!userDTO.getEmail().trim().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide a valid email address!";
        } else if (userDTO.getVerificationCode() == null || userDTO.getVerificationCode().isBlank()) {
            message = "Verification code is required!";
        } else if (!userDTO.getVerificationCode().trim().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Verification code must be exactly 6 digits!";
        } else if (userDTO.getNewPassword() == null || userDTO.getNewPassword().isBlank()) {
            message = "New password is required!";
        } else if (!userDTO.getNewPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "The password must be 8-20 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character!";
        } else if (userDTO.getConfirmPassword() == null || !userDTO.getConfirmPassword().equals(userDTO.getNewPassword())) {
            message = "Passwords do not match!";
        } else {
            try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                User user = hibernateSession.createQuery(
                        "FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.verificationCode = :code", User.class)
                        .setParameter("email", userDTO.getEmail().trim())
                        .setParameter("code", userDTO.getVerificationCode().trim())
                        .uniqueResult();

                if (user == null || user.getVerificationCode() == null || user.getVerificationCode().isBlank()) {
                    message = "Invalid or expired verification code! Please request a new code.";
                } else {
                    user.setPassword(userDTO.getNewPassword());
                    user.setVerificationCode(""); // Invalidate code after successful reset

                    Transaction tx = hibernateSession.beginTransaction();
                    try {
                        hibernateSession.update(user);
                        tx.commit();
                        status = true;
                        message = "Password reset successfully! You can now sign in with your new password.";
                    } catch (Exception e) {
                        tx.rollback();
                        message = "Failed to update password. Please try again!";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "Database error while resetting password.";
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }
}
