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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DZPListProviders {

    private final static String API = "https://api.deutsche-digitale-bibliothek.de/2/search/index/newspaper-issues/select?q=type:issue&rows=-1&fl=provider&group=true&group.field=provider_ddb_id&group.limit=1";

    @Autowired
    private OkHttpClient httpClient;

    @GetMapping
    @RequestMapping("dzp-list-providers")
    @Cacheable("dzp-list-providers")
    public List<Map<String, Object>> restApiCall() throws IOException {

        final Request request = new Request.Builder()
                .url(API)
                .build();

        final Call call = httpClient.newCall(request);
        final Response response = call.execute();

        final String responseString = response.body().string();

        final List<String> provider_ddb_id = JsonPath
                .parse(responseString)
                .read("$.grouped.provider_ddb_id.groups[*].groupValue", List.class);

        final List<String> provider = JsonPath
                .parse(responseString)
                .read("$.grouped.provider_ddb_id.groups[*].doclist.docs[0].provider", List.class);

        final List<Integer> numFound = JsonPath
                .parse(responseString)
                .read("$.grouped.provider_ddb_id.groups[*].doclist.numFound", List.class);

        final List<Map<String, Object>> resp = new ArrayList<>();

        for (int i = 0; i < provider_ddb_id.size(); ++i) {
            Map<String, Object> m = new HashMap<>();
            m.put("provider_ddb_id", provider_ddb_id.get(i));
            m.put("provider", provider.get(i));
            m.put("numFound", numFound.get(i));
            resp.add(m);
        }

        return resp;
    }

    @CacheEvict(value = "dzp-list-providers", allEntries = true)
    @Scheduled(fixedRateString = "${ddbstatistics.cachettl}")
    public void emptyCache() {
    }
}
