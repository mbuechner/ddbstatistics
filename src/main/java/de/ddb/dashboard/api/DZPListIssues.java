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
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author buechner
 */
@RestController
@RequestMapping("api")
@Slf4j
public class DZPListIssues {

    private final static String API = "https://api.deutsche-digitale-bibliothek.de/2/search/index/newspaper-issues/select?q=type:issue AND zdb_id:{{zdb_id}}&rows=2147483647&fl=id,publication_date";

    @Autowired
    private OkHttpClient httpClient;

    @GetMapping
    @RequestMapping("dzp-list-issues")
    @Cacheable("dzp-list-issues")
    public List<Map<String, String>> restApiCall(@RequestParam(value = "zdb_id", required = false) Optional<String> zdb_id) throws IOException {

        String queryUrl;

        if (zdb_id.isPresent() && !zdb_id.equals("null")) {
            queryUrl = API.replace("{{zdb_id}}", zdb_id.get());
        } else {
            throw new IllegalArgumentException("No ZDB ID given");
        }

        final Request request = new Request.Builder()
                .url(queryUrl)
                .build();
        final Call call = httpClient.newCall(request);
        final Response response = call.execute();

        final String responseString = response.body().string();

        final List<String> id = JsonPath
                .parse(responseString)
                .read("$.response.docs.*.id", List.class);

        final List<String> publication_date = JsonPath
                .parse(responseString)
                .read("$.response.docs.*.publication_date", List.class);

        final List<Map<String, String>> resp = new ArrayList<>();

        for (int i = 0; i < id.size(); ++i) {
            Map<String, String> m = new HashMap<>();
            m.put("id", id.get(i));
            m.put("publication_date", publication_date.get(i));
            resp.add(m);
        }

        return resp;
    }

    @CacheEvict(value = "dzp-list-issues", allEntries = true)
    @Scheduled(fixedRateString = "${ddbstatistics.cachettl}")
    public void emptyCache() {
    }

}
