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
public class DDBPiechartMediatype {

    private final static String API = "https://api.deutsche-digitale-bibliothek.de/search/index/search/select?q=*:*&rows=0&facet=on&facet.field=type_fct&facet.mincount=1&facet.limit=-1&oauth_consumer_key=";

    @Value("${ddbstatistics.apikey}")
    private String apiKey;

    private final static Map<String, String> CCD = new HashMap<>() {
        {
            put("mediatype_001", "Audio");
            put("mediatype_002", "Bild");
            put("mediatype_003", "Text");
            put("mediatype_004", "Volltext");
            put("mediatype_005", "Video");
            put("mediatype_006", "Sonstiges");
            put("mediatype_007", "Ohne Digitalisat");
            put("mediatype_008", "Institution");
            put("mediatype_010", "3D");
        }
    };

    @Autowired
    private OkHttpClient httpClient;

    @GetMapping
    @RequestMapping("ddb-piechart-mediatype")
    public Map<String, Integer> restApi() throws IOException {

        final Request request = new Request.Builder()
                .url(API + apiKey)
                .build();

        final Call call = httpClient.newCall(request);
        final Response response = call.execute();

        final List<Object> list = JsonPath
                .parse(response.body().string())
                .read("$.facet_counts.facet_fields.type_fct.*");

        final Map<String, Integer> data = new HashMap<>();

        for (int i = 0; i < list.size(); i += 2) {
            final String skey = CCD.containsKey((String) list.get(i)) ? CCD.get((String) list.get(i)) : (String) list.get(i);
            final Integer svalue = (Integer) list.get(i + 1);
            data.put(skey, svalue);
        }

        return data;

    }
}
