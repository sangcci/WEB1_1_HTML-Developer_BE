package com.backend.allreva.concert.infra;

import com.backend.allreva.concert.command.application.KopisConcertService;
import com.backend.allreva.concert.command.application.dto.KopisConcertResponse;
import com.backend.allreva.concert.infra.dto.Relate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KopisConcertServiceImpl implements KopisConcertService {

    private final WebClient webClient;
    private final XmlMapper xmlMapper;

    @Value("${public-data.kopis.prfplc-url}")
    private String findConcertIdUrl;

    @Value("${public-data.kopis.prf-url}")
    private String ConcertUrl;

    @Value("${public-data.kopis.stdate}")
    private String stdate;

    @Value("${public-data.kopis.eddate}")
    private String eddate;


    public KopisConcertServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.xmlMapper = new XmlMapper();
    }

    @Override
    public Mono<List<String>> fetchConcertCodes(final String hallId, final boolean isDaily) {
        return webClient.get()
                .uri(buildConcertUri(hallId, isDaily))
                .retrieve()
                .bodyToMono(String.class)
                .map(xml -> {
                    try {
                        JsonNode rootNode = xmlMapper.readTree(xml);
                        List<String> concertIds = new ArrayList<>();
                        JsonNode dbArray = rootNode.get("db");

                        // 공연이 없는 경우 빈 리스트 반환
                        if (dbArray == null) return new ArrayList<>();
                        if (dbArray.isArray()) {
                            for (JsonNode node : dbArray) {
                                concertIds.add(node.get("mt20id").asText());
                            }
                        }

                        log.info("fetch concert ids complete : hallId {}", hallId);
                        return concertIds;
                    } catch (Exception e) {
                        log.error("can't fetch concert ids : hallId {}", hallId);
                        log.error("error Message: {}", e.getMessage());
                        return new ArrayList<>();
                    }
                });
    }

    private String buildConcertUri(final String hallId, final boolean isDaily) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(findConcertIdUrl)
                .queryParam("prfplccd", hallId);

        if (isDaily) {
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            uriBuilder.queryParam("afterdate", formattedDate);
        }

        return uriBuilder.buildAndExpand(stdate, eddate).toUriString();
    }

    @Override
    public Mono<KopisConcertResponse> fetchConcertDetail(final String hallId, final String concertcd) {
        return webClient.get()
                .uri(UriComponentsBuilder.fromUriString(ConcertUrl)
                        .buildAndExpand(concertcd)
                        .toUriString())
                .retrieve()
                .bodyToMono(String.class)
                .map(xml -> {
                    try {
                        // XML을 JsonNode로 파싱
                        JsonNode rootNode = xmlMapper.readTree(xml);
                        JsonNode node = rootNode.path("db");

                        log.info("fetch concert info complete : concertcd {}", concertcd);

                        return toKopisConcertResponse(node, hallId);
                    } catch (Exception e) {
                        log.error("can't fetch concert info : concertcd {}", concertcd);
                        log.error("error Message: {}", e.getMessage());
                        return null;
                    }
                });
    }

    private KopisConcertResponse toKopisConcertResponse(final JsonNode node, final String hallId) {
        String concertcd = node.get("mt20id").asText();
        String prfnm = node.get("prfnm").asText();
        String prfpdfrom = node.get("prfpdfrom").asText();
        String prfpdto = node.get("prfpdto").asText();
        String prfstate = node.get("prfstate").asText();
        String pcseguidance = node.get("pcseguidance").asText();
        String dtguidance = node.get("dtguidance").asText();
        String poster = node.get("poster").asText();
        String entrpsnmH = node.get("entrpsnmH").asText();
        List<String> styurl = getStyurls(node);
        List<Relate> relate = getRelates(node);

        return new KopisConcertResponse(concertcd, getFacilityId(hallId), prfnm, prfpdfrom, prfpdto, hallId, poster,
                pcseguidance, prfstate, dtguidance, entrpsnmH, styurl, relate);
    }

    private String getFacilityId(final String hallId) {
        return hallId.split("-")[0];
    }


    private List<Relate> getRelates(final JsonNode node) {
        List<Relate> relates = new ArrayList<>();
        JsonNode relatesNode = node.get("relates").get("relate");

        if (relatesNode.isArray()) {
            for (JsonNode one : relatesNode) {
                String relatenm = one.path("relatenm").asText();
                String relateurl = one.path("relateurl").asText();
                relates.add(new Relate(relatenm, relateurl));

            }
        } else {
            String relatenm = relatesNode.get("relatenm").asText();
            String relateurl = relatesNode.get("relateurl").asText();
            relates.add(new Relate(relatenm, relateurl));
        }

        return relates;
    }

    private List<String> getStyurls(final JsonNode node) {
        JsonNode styurlsNode = node.get("styurls");
        if (styurlsNode == null) return new ArrayList<>();
        List<String> styurlList = new ArrayList<>();

        if (styurlsNode.get("styurl").isArray()) {
            for (JsonNode one : styurlsNode.get("styurl")) {
                styurlList.add(one.asText());
            }
        } else {
            styurlList.add(styurlsNode.get("styurl").asText());
        }

        return styurlList;
    }
}
