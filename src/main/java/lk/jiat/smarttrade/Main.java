package lk.jiat.smarttrade;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.scan.StandardJarScanner;

import java.io.File;

/**
 * Optional launcher: starts an embedded Tomcat 9 for IDE use.
 * For the normal workflow just deploy target/smarttrade.war to a standalone Tomcat 9.
 * Servlets, filters, listeners and the Jersey servlet are all declared in WEB-INF/web.xml.
 */
public class Main {

    private static final int SERVER_PORT = 8082;
    private static final String CONTEXT_PATH = "/smarttrade";

    public static void main(String[] args) {
        try {
            // Check if port is already in use by another process
            try (java.net.ServerSocket testSocket = new java.net.ServerSocket(SERVER_PORT)) {
                testSocket.setReuseAddress(true);
            } catch (java.io.IOException e) {
                System.err.println("ERROR: Port " + SERVER_PORT + " is already in use by another application!");
                System.err.println("Please terminate the conflicting process or change SERVER_PORT in Main.java.");
                System.exit(1);
            }

            Tomcat tomcat = new Tomcat();
            tomcat.setPort(SERVER_PORT);
            tomcat.getConnector();

            Context context = tomcat.addWebapp(CONTEXT_PATH, new File("src/main/webapp").getAbsolutePath());
            context.setParentClassLoader(Main.class.getClassLoader());

            // Disable manifest scanning to prevent NoSuchFileException from broken Class-Path headers in 3rd-party jars
            if (context.getJarScanner() instanceof StandardJarScanner) {
                StandardJarScanner scanner = (StandardJarScanner) context.getJarScanner();
                scanner.setScanManifest(false);
            }

            // Remove default jsp servlet so it doesn't fail on missing Jasper dependency (app uses HTML + REST)
            context.addLifecycleListener(event -> {
                if (Lifecycle.BEFORE_START_EVENT.equals(event.getType()) || Lifecycle.START_EVENT.equals(event.getType())) {
                    org.apache.catalina.Container jsp = context.findChild("jsp");
                    if (jsp != null) {
                        context.removeChild(jsp);
                    }
                }
            });

            tomcat.start();
            System.out.println("App URL: http://localhost:" + SERVER_PORT + CONTEXT_PATH);
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            throw new RuntimeException("Tomcat Embedded Server loading failed: " + e.getMessage());
        }
    }
}
