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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
public class DDBPiechartSector {

    private final static String API = "https://api.deutsche-digitale-bibliothek.de/search/index/search/select?q=*:*&rows=0&facet=on&facet.field=sector_fct&facet.mincount=1&facet.limit=-1&oauth_consumer_key=";

    @Value("${ddbstatistics.apikey}")
    private String apiKey;

    private final static Map<String, String> CCD = new HashMap<>() {
        {
            put("sec_01", "Archiv");
            put("sec_02", "Bibliothek");
            put("sec_03", "Denkmalpflege");
            put("sec_04", "Wissenschaft");
            put("sec_05", "Mediathek");
            put("sec_06", "Museum");
            put("sec_07", "Sonstiges");
        }
    };

    @Autowired
    private OkHttpClient httpClient;

    @GetMapping
    @RequestMapping("ddb-piechart-sector")
    @Cacheable("ddb-piechart-sector")
    public Map<String, Integer> restApiCall() throws IOException {

        final Request request = new Request.Builder()
                .url(API + apiKey)
                .build();

        final Call call = httpClient.newCall(request);
        final Response response = call.execute();

        final List<Object> list = JsonPath
                .parse(response.body().string())
                .read("$.facet_counts.facet_fields.sector_fct.*");

        final Map<String, Integer> data = new HashMap<>();

        for (int i = 0; i < list.size(); i += 2) {
            final String skey = CCD.containsKey((String) list.get(i)) ? CCD.get((String) list.get(i)) : (String) list.get(i);
            final Integer svalue = (Integer) list.get(i + 1);
            data.put(skey, svalue);
        }

        return data;
    }

    @CacheEvict(value = "ddb-piechart-sector", allEntries = true)
    @Scheduled(fixedRateString = "${ddbstatistics.cachettl}")
    public void emptyCache() {
    }
}
