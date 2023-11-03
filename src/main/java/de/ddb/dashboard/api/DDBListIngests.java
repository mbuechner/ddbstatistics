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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.extra.YearWeek;

/**
 *
 * @author buechner
 */
@RestController
@RequestMapping("api")
@Slf4j
public class DDBListIngests {

    @Getter
    private final static String API = "https://api.deutsche-digitale-bibliothek.de/search/index/search/select?q=last_update:{{DATES}}&group=true&group.field=ingest_id&group.limit=1&fl=dataset_label,provider_fct,md_format,type_fct,sector_fct&oauth_consumer_key=";

    @Value("${ddbstatistics.apikey}")
    private String apiKey;

    private final static Map<String, String> MEDIATYPE = new HashMap<>() {
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

    private final static Map<String, String> SECTOR = new HashMap<>() {
        {
            put("sec_01", "Archiv");
            put("sec_02", "Bibliothek");
            put("sec_03", "Denkmalpflege");
            put("sec_04", "Wissenschaft");
            put("sec_05", "Mediathek");
            put("sec_06", "Museum");
            put("sec_07", "Sonstige");
        }

    };

    @Autowired
    private OkHttpClient httpClient;

    @GetMapping
    @RequestMapping("ddb-list-ingests")
    @Cacheable("ddb-list-ingests")
    public List<Map<String, Object>> restApiCall(@RequestParam("cw") Optional<String> cw) throws IOException {

        YearWeek yw = null;

        try {
            yw = YearWeek.parse(cw.get());
        } catch (Exception e) {
            yw = YearWeek.now();
        }

        final Request request = new Request.Builder()
                .url(API.replace("{{DATES}}", getSearchString(yw)) + apiKey)
                .build();

        final Call call = httpClient.newCall(request);
        final Response response = call.execute();

        final String responseString = response.body().string();

        final List<String> ingestIds = JsonPath
                .parse(responseString)
                .read("$.grouped.ingest_id.groups[*].groupValue", List.class);

        final List<Integer> numFound = JsonPath
                .parse(responseString)
                .read("$.grouped.ingest_id.groups[*].doclist.numFound", List.class);

        final List<String> dataset_label = JsonPath
                .parse(responseString)
                .read("$.grouped.ingest_id.groups[*].doclist.docs[*].dataset_label", List.class);

        final List<String> md_format = JsonPath
                .parse(responseString)
                .read("$.grouped.ingest_id.groups[*].doclist.docs[*].md_format", List.class);

        final List<JSONArray> type_fct = replaceSynonymsListJSONArray(JsonPath
                .parse(responseString)
                .read("$.grouped.ingest_id.groups[*].doclist.docs[*].type_fct", List.class),
                MEDIATYPE);

        final List<String> sector_fct = replaceSynonymsList(JsonPath
                .parse(responseString)
                .read("$.grouped.ingest_id.groups[*].doclist.docs[*].sector_fct", List.class),
                SECTOR);

        final List<JSONArray> provider_fct = JsonPath
                .parse(responseString)
                .read("$.grouped.ingest_id.groups[*].doclist.docs[*].provider_fct", List.class);

        final List<Map<String, Object>> resp = new ArrayList<>();

        for (int i = 0; i < ingestIds.size(); ++i) {
            final Map<String, Object> m = new HashMap<>();
            m.put("ingest_id", ingestIds.get(i));
            m.put("dataset_label", dataset_label.get(i));
            m.put("provider_fct", provider_fct.get(i));
            m.put("md_format", md_format.get(i));
            m.put("numFound", numFound.get(i));
            m.put("type_fct", type_fct.get(i));
            m.put("sector_fct", sector_fct.get(i));
            resp.add(m);
        }

        return resp;
    }

    public static List<String> replaceSynonymsList(@NotNull List<String> list, @NotNull final Map<String, String> concordance) {
        final List<String> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            final String skey = concordance.containsKey((String) list.get(i)) ? concordance.get((String) list.get(i)) : (String) list.get(i);
            newList.add(skey);
        }
        return newList;
    }

    public static List<JSONArray> replaceSynonymsListJSONArray(@NotNull List<JSONArray> list, @NotNull final Map<String, String> concordance) {
        final List<JSONArray> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            final JSONArray array = list.get(i);
            final JSONArray newArray = new JSONArray();
            for (int j = 0; j < array.size(); ++j) {
                if (concordance.containsKey((String) array.get(j))) {
                    newArray.add(concordance.get((String) array.get(j)));
                } else {
                    newArray.add(array.get(j));
                }
            }
            newList.add(newArray);
        }
        return newList;
    }

    public static String getSearchString(YearWeek calendarWeek) {
        LocalDate desiredDate = calendarWeek.atDay(DayOfWeek.MONDAY);
        final StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < 7; ++i) {
            sb.append(desiredDate.format(DateTimeFormatter.ISO_DATE));
            if (i < 6) {
                sb.append("* OR ");
            } else {
                sb.append("*)");
            }
            desiredDate = desiredDate.plusDays(1L);
        }
        return sb.toString();
    }

    @CacheEvict(value = "ddb-list-ingests", allEntries = true)
    @Scheduled(fixedRateString = "${ddbstatistics.cachettl}")
    public void emptyCache() {
    }
}
