package lk.jiat.smarttrade.mail;

import lk.jiat.smarttrade.util.Env;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;

public class PasswordResetMail extends Mailable {
    private final String to;
    private final String verificationCode;

    public PasswordResetMail(String to, String verificationCode) {
        this.to = to;
        this.verificationCode = verificationCode;
    }

    @Override
    public void build(Message message) throws MessagingException {
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("Reset your password - " + Env.get("app.name"));

        String appName = Env.get("app.name");
        String appURL = Env.get("app.url");
        String resetURL = appURL + "/sign-in.html?action=reset&email="
                + URLEncoder.encode(to, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(verificationCode, StandardCharsets.UTF_8);

        byte[] logo = loadLogo();
        String logoHtml = logo != null
                ? "<img src=\"cid:smarttrade-logo\" width=\"190\" alt=\"SmartTrade\" style=\"display:block;border:0;height:auto;\">"
                : "<span style=\"font-family:Arial,sans-serif;font-size:26px;font-weight:700;color:#ffffff;\">SmartTrade</span>";

        String html = TEMPLATE
                .replace("{{LOGO}}", logoHtml)
                .replace("{{EMAIL}}", esc(to))
                .replace("{{CODE}}", esc(verificationCode))
                .replace("{{URL}}", esc(resetURL))
                .replace("{{APP}}", esc(appName))
                .replace("{{APPURL}}", esc(appURL))
                .replace("{{YEAR}}", String.valueOf(Year.now().getValue()));

        MimeMultipart related = new MimeMultipart("related");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=utf-8");
        related.addBodyPart(htmlPart);

        if (logo != null) {
            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.setDataHandler(new DataHandler(new ByteArrayDataSource(logo, "image/png")));
            imagePart.setContentID("<smarttrade-logo>");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            imagePart.setFileName("smarttrade-logo.png");
            related.addBodyPart(imagePart);
        }
        message.setContent(related);
    }

    private static byte[] loadLogo() {
        try (InputStream in = PasswordResetMail.class.getResourceAsStream("/mail/logo.png")) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static final String TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="color-scheme" content="dark">
<title>Reset your password</title>
</head>
<body style="margin:0;padding:0;background-color:#0a1015;">
<div style="display:none;max-height:0;overflow:hidden;opacity:0;color:#0a1015;">Your {{APP}} password reset code is {{CODE}}</div>

<table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" bgcolor="#0a1015" style="background-color:#0a1015;">
<tr><td align="center" style="padding:32px 12px;">

  <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0" style="width:100%;max-width:600px;">

    <!-- logo -->
    <tr><td bgcolor="#10171c" style="background-color:#10171c;border-radius:18px 18px 0 0;border-top:4px solid #18a953;padding:28px 36px;">
      {{LOGO}}
    </td></tr>

    <!-- card -->
    <tr><td bgcolor="#141d24" style="background-color:#141d24;padding:40px 36px 12px 36px;border-left:1px solid #1f2c35;border-right:1px solid #1f2c35;">
      <div style="font-family:Consolas,'Courier New',monospace;font-size:12px;letter-spacing:3px;color:#2bd97f;font-weight:700;">&#9679; PASSWORD RESET REQUEST</div>
      <h1 style="margin:14px 0 14px 0;font-family:'Segoe UI',Arial,Helvetica,sans-serif;font-size:30px;line-height:38px;color:#eef4f1;font-weight:700;">Reset your password.<br>We've got you covered.</h1>
      <p style="margin:0 0 6px 0;font-family:'Segoe UI',Arial,Helvetica,sans-serif;font-size:15px;line-height:24px;color:#b6c4cc;">
        We received a request to reset the password for your account (<strong style="color:#eef4f1;">{{EMAIL}}</strong>). Use the verification code below to complete the reset.
      </p>
    </td></tr>

    <!-- code -->
    <tr><td bgcolor="#141d24" align="center" style="background-color:#141d24;padding:18px 36px 8px 36px;border-left:1px solid #1f2c35;border-right:1px solid #1f2c35;">
      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0">
        <tr><td align="center" bgcolor="#0e151a" style="background-color:#0e151a;border:1px solid #2c3d49;border-radius:14px;padding:20px 12px;">
          <div style="font-family:'Segoe UI',Arial,sans-serif;font-size:11px;letter-spacing:2px;color:#8b9ca6;text-transform:uppercase;">Your password reset code</div>
          <div style="margin-top:8px;font-family:Consolas,'Courier New',monospace;font-size:34px;line-height:40px;letter-spacing:8px;font-weight:700;color:#2bd97f;">{{CODE}}</div>
        </td></tr>
      </table>
    </td></tr>

    <!-- button -->
    <tr><td bgcolor="#141d24" align="center" style="background-color:#141d24;padding:26px 36px 8px 36px;border-left:1px solid #1f2c35;border-right:1px solid #1f2c35;">
      <table role="presentation" cellpadding="0" cellspacing="0" border="0">
        <tr><td align="center" bgcolor="#1fc26a" style="background-color:#1fc26a;border-radius:999px;">
          <a href="{{URL}}" target="_blank" style="display:inline-block;padding:15px 40px;font-family:'Segoe UI',Arial,Helvetica,sans-serif;font-size:16px;font-weight:700;color:#03140a;text-decoration:none;border-radius:999px;">Reset password now &rarr;</a>
        </td></tr>
      </table>
      <p style="margin:16px 0 0 0;font-family:'Segoe UI',Arial,sans-serif;font-size:13px;line-height:20px;color:#8b9ca6;">or enter the 6-digit code on the sign-in page.</p>
    </td></tr>

    <!-- fallback link -->
    <tr><td bgcolor="#141d24" style="background-color:#141d24;padding:22px 36px 34px 36px;border-left:1px solid #1f2c35;border-right:1px solid #1f2c35;border-bottom:1px solid #1f2c35;border-radius:0 0 18px 18px;">
      <div style="border-top:1px solid #1f2c35;padding-top:18px;font-family:'Segoe UI',Arial,sans-serif;font-size:12px;line-height:19px;color:#8b9ca6;">
        If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.<br>
        <span style="font-size:11px;color:#62747e;">Link: <a href="{{URL}}" style="color:#2bd97f;word-break:break-all;">{{URL}}</a></span>
      </div>
    </td></tr>

    <!-- footer -->
    <tr><td align="center" style="padding:26px 20px 6px 20px;">
      <div style="font-family:'Segoe UI',Arial,sans-serif;font-size:12px;line-height:20px;color:#62747e;">
        Verified sellers &nbsp;&middot;&nbsp; 24-hour returns &nbsp;&middot;&nbsp; Secure checkout
      </div>
      <div style="margin-top:10px;font-family:'Segoe UI',Arial,sans-serif;font-size:12px;line-height:19px;color:#62747e;">
        &copy; {{YEAR}} <a href="{{APPURL}}" style="color:#8b9ca6;text-decoration:none;">{{APP}}</a>. All rights reserved.
      </div>
    </td></tr>

  </table>

</td></tr>
</table>
</body>
</html>
""";
}
