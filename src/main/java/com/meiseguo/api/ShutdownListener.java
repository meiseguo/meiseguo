package com.meiseguo.api;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private static final AtomicBoolean closing = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (closing.compareAndSet(false, true)) {
            System.out.println("Shutting down");
            try {
                Runtime runtime = Runtime.getRuntime();
                Process p = runtime.exec("ps -ef");
                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream())));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
