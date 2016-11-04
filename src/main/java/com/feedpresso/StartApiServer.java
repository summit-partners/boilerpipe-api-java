package com.feedpresso;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;

public class StartApiServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));

        Server server = new Server(createThreadPool());

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);

        server.setConnectors(new Connector[]{connector});

        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        sh.setInitParameter(
                "javax.ws.rs.Application",
                "org.boilerpipe.web.BoilerpipeApplication"
        );

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(sh, "/*");

        HandlerCollection handlers = new HandlerCollection();

        handlers.setHandlers(new Handler[]{
                context,
                new DefaultHandler(),
                createRequestLogHandler()
        });
        server.setHandler(handlers);

        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addEventListener(mbContainer);
        server.addBean(mbContainer);

        server.addBean(Log.getLog());

        server.start();
        server.join();
    }

    private static ThreadPool createThreadPool() {
        BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(6000);
        return new QueuedThreadPool(400, 50, 1000, queue);
    }

    private static RequestLogHandler createRequestLogHandler() {
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(new Slf4jRequestLog());
        return requestLogHandler;
    }
}