package com.feedpresso;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.File;

public class ServerStart {
    public static void main(String[] args) throws Exception {
        Resource serverXml = Resource.newSystemResource("jetty.xml");
        XmlConfiguration configuration = new XmlConfiguration(serverXml.getInputStream());
        Server server = (Server) configuration.configure();

        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");

        File warFile = new File("target/rest-api.war");
        context.setWar(warFile.getAbsolutePath());

        server.setHandler(context);

        server.start();
        server.join();
    }
}