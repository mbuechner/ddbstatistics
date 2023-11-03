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
public class DZPListTitles {

    @Getter
    private final static String API = "https://api.deutsche-digitale-bibliothek.de/search/index/newspaper/select?q=hasLoadedIssues:true&rows=2147483647&fl=id,title";

    @Autowired
    private OkHttpClient httpClient;

    @GetMapping
    @RequestMapping("dzp-list-titles")
    public List<Map<String, String>> restApiCall() throws IOException {

        final Request request = new Request.Builder()
                .url(API)
                .build();

        final Call call = httpClient.newCall(request);
        final Response response = call.execute();

        final String responseString = response.body().string();

        final List<String> zdbIds = JsonPath
                .parse(responseString)
                .read("$.response.docs.*.id", List.class);

        final List<List<String>> titles = JsonPath
                .parse(responseString)
                .read("$.response.docs.*.title", List.class);

        final List<Map<String, String>> resp = new ArrayList<>();

        for (int i = 0; i < zdbIds.size(); ++i) {
            Map<String, String> m = new HashMap<>();
            m.put("id", zdbIds.get(i));
            m.put("title", listToString(titles.get(i), "; "));
            resp.add(m);
        }

        return resp;
    }

    public static String listToString(List<String> list, String separator) {
        final StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
            sb.append(separator);
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }
}
