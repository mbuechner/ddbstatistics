/*
 * Copyright 2023 Michael Büchner, Deutsche Digitale Bibliothek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ddb.dashboard.api;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author buechner
 */
@RestController
@RequestMapping("api")
@Slf4j
public class DZPNumberPages {

    @Getter
    private final static String API = "https://api.deutsche-digitale-bibliothek.de/search/index/newspaper-issues/select?q=type:page&rows=0";

    @Autowired
    private OkHttpClient httpClient;

    @GetMapping
    @RequestMapping("dzp-number-pages")
    public Integer restApiCall() throws IOException   {

        final Request request = new Request.Builder()
                .url(API)
                .build();

        final Call call = httpClient.newCall(request);
        final Response response = call.execute();

        final Integer numFound = JsonPath
                .parse(response.body().string())
                .read("$.response.numFound");
        
        return numFound;
    }
}