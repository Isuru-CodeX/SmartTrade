package lk.jiat.smarttrade.service;

import com.google.gson.JsonObject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import lk.jiat.smarttrade.dto.UserDTO;
import lk.jiat.smarttrade.entity.Address;
import lk.jiat.smarttrade.entity.City;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import lk.jiat.smarttrade.validation.Validator;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProfileService {

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public String loadUserAddresses(@Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null && httpSession.getAttribute("user") != null) {
            User sessionUser = (User) httpSession.getAttribute("user");

            responseObject.addProperty("name", sessionUser.getFirstName() + " " + sessionUser.getLastName());
            responseObject.addProperty("email", sessionUser.getEmail());

            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            List<Address> addressList = hibernateSession.createQuery("FROM Address a WHERE a.user=:user", Address.class)
                    .setParameter("user", sessionUser)
                    .getResultList();

            List<JsonObject> addresses = new ArrayList<>();
            for (Address a : addressList) {
                JsonObject jo = new JsonObject();
                jo.addProperty("lineOne", a.getLineOne());
                jo.addProperty("lineTwo", a.getLineTwo());
                jo.addProperty("mobile", a.getMobile());
                jo.addProperty("cityId", a.getCity().getId());
                jo.addProperty("cityName", a.getCity().getName());
                jo.addProperty("isPrimary", a.isPrimary());
                addresses.add(jo);
            }

            responseObject.add("addresses", AppUtil.GSON.toJsonTree(addresses));

            hibernateSession.close();
        }
        return AppUtil.GSON.toJson(responseObject);
    }

    public String userProfile(@Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        /// user-profile-data-loading-start
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "User not logged in");
            return AppUtil.GSON.toJson(responseObject);
        }
        User sessionUser = (User) httpSession.getAttribute("user");

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        User user = hibernateSession.find(User.class, sessionUser.getId());
        if (user == null) {
            user = sessionUser;
        } else {
            httpSession.setAttribute("user", user);
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        userDTO.setProfileImage(user.getProfileImage());

        List<Address> addressList = hibernateSession.createQuery("FROM Address a WHERE a.user=:user", Address.class)
                .setParameter("user", user).getResultList();

        Address primaryAddress = null;
        for (Address address : addressList) {
            if (address.isPrimary()) {
                primaryAddress = address;
                break;
            }
        }
        if (primaryAddress != null) {
            userDTO.setLineOne(primaryAddress.getLineOne());
            userDTO.setLineTwo(primaryAddress.getLineTwo());
            userDTO.setPostalCode(primaryAddress.getPostalCode());
            userDTO.setMobile(primaryAddress.getMobile());
            userDTO.setPrimary(primaryAddress.isPrimary());
            userDTO.setCityId(primaryAddress.getCity().getId());
            userDTO.setCityName(primaryAddress.getCity().getName());
        }

        LocalDateTime createdAt = user.getCreatedAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMMM");
        String sinceAt = createdAt.format(formatter);
        userDTO.setSinceAt(sinceAt);

        responseObject.add("user", AppUtil.GSON.toJsonTree(userDTO));
        /// user-profile-data-loading-end
        hibernateSession.close();
        status = true;
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateProfile(UserDTO userDTO, @Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        /// profile-update-start
        if (userDTO.getFirstName() == null) {
            message = "First name is required!";
        } else if (userDTO.getFirstName().isBlank()) {
            message = "First name can not be empty!";
        } else if (userDTO.getLastName() == null) {
            message = "Last name is required!";
        } else if (userDTO.getLastName().isBlank()) {
            message = "Last name can not be empty!";
        } else if (userDTO.getLineOne() == null) {
            message = "Address line one is required!";
        } else if (userDTO.getLineOne().isBlank()) {
            message = "Address line one can not be empty!";
        } else if (userDTO.getPostalCode() != null &&
                !userDTO.getPostalCode().isBlank() &&
                !userDTO.getPostalCode().matches(Validator.POSTAL_CODE_VALIDATION)) {
            message = "Enter a valid postal code!";
        } else if (userDTO.getCityId() == 0) {
            message = "Please select a city!";
        } else if (userDTO.getMobile() == null || userDTO.getMobile().isBlank()) {
            message = "Mobile is required!";
        } else if (!userDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
            message = "Enter valid mobile number!";
        } else if (userDTO.getPassword() == null) {
            message = "Password is required!";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password can not be empty!";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please provide valid password. \n " +
                    "The password must be at least 8 characters long and include at least one uppercase letter, " +
                    "one lowercase letter, one digit, and one special character";
        } else if (userDTO.getNewPassword() != null &&
                !userDTO.getNewPassword().isBlank() &&
                !userDTO.getNewPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "New password is not valid. \n " +
                    "The password must be at least 8 characters long and include at least one uppercase letter, " +
                    "one lowercase letter, one digit, and one special character";
        } else if (userDTO.getConfirmPassword() != null &&
                !userDTO.getConfirmPassword().isBlank() &&
                !userDTO.getConfirmPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Confirm password not valid. \n " +
                    "The password must be at least 8 characters long and include at least one uppercase letter, " +
                    "one lowercase letter, one digit, and one special character";
        } else if (userDTO.getNewPassword() != null && userDTO.getConfirmPassword() != null
                && !userDTO.getConfirmPassword().equals(userDTO.getNewPassword())) {
            message = "New password and confirm password did not match";
        } else {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                message = "Please login first";
            } else if (httpSession.getAttribute("user") == null) {
                message = "Please login first";
            } else {
                User sessionUser = (User) httpSession.getAttribute("user");
                Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
                User dbUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                        .setParameter("email", sessionUser.getEmail())
                        .getSingleResult();

                dbUser.setFirstName(userDTO.getFirstName());
                dbUser.setLastName(userDTO.getLastName());
                dbUser.setPassword(userDTO.getConfirmPassword() != null && !userDTO.getConfirmPassword().isBlank()
                        ? userDTO.getConfirmPassword()
                        : userDTO.getPassword());

                if (userDTO.getProfileImage() != null) {
                    dbUser.setProfileImage(userDTO.getProfileImage().isBlank() ? null : userDTO.getProfileImage());
                }

                List<Address> addressList = hibernateSession
                        .createQuery("FROM Address a WHERE a.user=:user", Address.class)
                        .setParameter("user", dbUser)
                        .getResultList();

                Address currentAddress = null;
                for (Address address : addressList) {
                    if (java.util.Objects.equals(nullToEmpty(address.getLineOne()), nullToEmpty(userDTO.getLineOne()))
                            &&
                            java.util.Objects.equals(nullToEmpty(address.getLineTwo()),
                                    nullToEmpty(userDTO.getLineTwo()))
                            &&
                            java.util.Objects.equals(nullToEmpty(address.getPostalCode()),
                                    nullToEmpty(userDTO.getPostalCode()))
                            &&
                            address.getCity().getId() == userDTO.getCityId()) {
                        currentAddress = address;
                        break;
                    }
                }

                if (currentAddress == null) {
                    currentAddress = new Address();
                }

                boolean hasPrimaryAddress = false;
                for (Address address : addressList) {
                    if (address.isPrimary()) {
                        hasPrimaryAddress = true;
                        break;
                    }
                }
                if (!hasPrimaryAddress) {
                    currentAddress.setPrimary(true); /// first saved address becomes the primary one
                }

                currentAddress.setLineOne(userDTO.getLineOne());
                currentAddress.setLineTwo(userDTO.getLineTwo());
                currentAddress.setPostalCode(userDTO.getPostalCode());
                currentAddress.setMobile(userDTO.getMobile());
                currentAddress.setUser(dbUser);

                City city = hibernateSession.find(City.class, userDTO.getCityId());

                currentAddress.setCity(city);

                Transaction transaction = hibernateSession.beginTransaction();
                try {
                    hibernateSession.merge(dbUser);
                    hibernateSession.merge(currentAddress);
                    transaction.commit();
                    httpSession.setAttribute("user", dbUser); /// update session user
                    status = true;
                    message = "Profile details update successful...";
                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Profile details update failed!";
                }

                hibernateSession.close();
            }
        }
        /// profile-update-end

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String uploadAvatar(FormDataBodyPart formDataBodyPart, @Context HttpServletRequest request,
            ServletContext context) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        String profileImageUrl = null;

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login first!");
            return AppUtil.GSON.toJson(responseObject);
        }

        if (formDataBodyPart == null || formDataBodyPart.getContentDisposition() == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please select an image file to upload!");
            return AppUtil.GSON.toJson(responseObject);
        }

        ContentDisposition contentDisposition = formDataBodyPart.getContentDisposition();
        String originalFileName = contentDisposition.getFileName();
        if (originalFileName == null || originalFileName.isBlank()) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Invalid file name!");
            return AppUtil.GSON.toJson(responseObject);
        }

        String extension = FilenameUtils.getExtension(originalFileName);
        if (extension == null || !extension.toLowerCase().matches("^(png|jpg|jpeg|webp|gif)$")) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message",
                    "Invalid image format! Only PNG, JPG, JPEG, WEBP, and GIF are supported.");
            return AppUtil.GSON.toJson(responseObject);
        }

        User sessionUser = (User) httpSession.getAttribute("user");
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = hibernateSession.beginTransaction();

        try {
            User dbUser = hibernateSession.find(User.class, sessionUser.getId());
            if (dbUser == null) {
                dbUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                        .setParameter("email", sessionUser.getEmail())
                        .getSingleResult();
            }

            InputStream inputStream = formDataBodyPart.getEntityAs(InputStream.class);
            FileUploadService fileUploadService = new FileUploadService(context);
            FileUploadService.FileItem fileItem = fileUploadService.uploadFile("profile/" + dbUser.getId(), inputStream,
                    contentDisposition);

            profileImageUrl = fileItem.getUrl();
            dbUser.setProfileImage(profileImageUrl);

            hibernateSession.merge(dbUser);
            transaction.commit();

            httpSession.setAttribute("user", dbUser);
            status = true;
            message = "Profile picture updated successfully!";
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            message = "Failed to upload profile picture: " + e.getMessage();
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        if (profileImageUrl != null) {
            responseObject.addProperty("profileImage", profileImageUrl);
        }
        return AppUtil.GSON.toJson(responseObject);
    }

    public String removeAvatar(@Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login first!");
            return AppUtil.GSON.toJson(responseObject);
        }

        User sessionUser = (User) httpSession.getAttribute("user");
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = hibernateSession.beginTransaction();

        try {
            User dbUser = hibernateSession.find(User.class, sessionUser.getId());
            if (dbUser == null) {
                dbUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                        .setParameter("email", sessionUser.getEmail())
                        .getSingleResult();
            }

            dbUser.setProfileImage(null);
            hibernateSession.merge(dbUser);
            transaction.commit();

            httpSession.setAttribute("user", dbUser);
            status = true;
            message = "Profile picture removed successfully!";
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            message = "Failed to remove profile picture: " + e.getMessage();
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }
}
