package de.ddb.dashboard;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@Slf4j
public class Application {

    private OkHttpClient httpClient; // http client

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public OkHttpClient httpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        final Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(128);
        dispatcher.setMaxRequestsPerHost(64);
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(0, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .build();
        return httpClient;
    }
}
