package com.eroom.erooja.features.auth.kakao.service;

import com.eroom.erooja.common.constants.ErrorEnum;
import com.eroom.erooja.features.auth.kakao.exception.KakaoNotRegisteredUserException;
import com.eroom.erooja.features.auth.kakao.exception.KakaoRESTException;
import com.eroom.erooja.features.auth.kakao.json.KakaoIdsJSON;
import com.eroom.erooja.features.auth.kakao.json.KakaoUserJSON;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
public class KakaoUserRESTService {
    private final static Logger logger = LoggerFactory.getLogger(KakaoUserRESTService.class);
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${thirdPartyProperties.kakaoAdminKey}")
    private String adminKey;

    @Value("${thirdPartyProperties.kakaoRestKey}")
    private String restKey;

    public KakaoUserJSON findUserById(Long targetId) throws KakaoRESTException {
        try {
            ResponseEntity<KakaoUserJSON> response = this.restTemplate.postForEntity(
                    ServiceConstants.BASE_HOST_KAPI + ServiceConstants.END_POINT_USER_ME, buildHttpEntity(targetId), KakaoUserJSON.class);
            return response.getBody();
        } catch (HttpClientErrorException errorException) {
            throw buildException(errorException);
        }
    }

    public Long logoutById(Long targetId) throws KakaoRESTException {
        try {
            ResponseEntity<Long> response = this.restTemplate.postForEntity(
                    ServiceConstants.BASE_HOST_KAPI + ServiceConstants.END_POINT_USER_LOGOUT, buildHttpEntity(targetId), Long.class);
            return response.getBody();
        } catch (HttpClientErrorException errorException) {
            throw buildException(errorException);
        }
    }

    private KakaoRESTException buildException(HttpClientErrorException clientException) {
        String message = clientException.getMessage();
        logger.error("KakaoRESTException", clientException);

        if (message == null) message = "";

        if (message.contains("NotRegisteredUserException")) return new KakaoNotRegisteredUserException();
        
        return new KakaoRESTException(ErrorEnum.AUTH_KAKAO_UNKNOWN_ERROR);
    }

    private HttpEntity<MultiValueMap<String, String>> buildHttpEntity(Long targetId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("target_id_type", "user_id");
        formData.add("target_id", String.valueOf(targetId));

        return new HttpEntity<>(formData, buildHeader());
    }

    public KakaoIdsJSON findUserIds(Integer limit, Integer fromId, Boolean isAsc) throws HttpClientErrorException {

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("limit", limit);
        formData.add("from_id", fromId);
        formData.add("order", isAsc? "asc" : "desc");

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formData, buildHeader());

        ResponseEntity<KakaoIdsJSON> response = this.restTemplate.postForEntity(
                ServiceConstants.BASE_HOST_KAPI + ServiceConstants.END_POINT_USER_IDS, httpEntity, KakaoIdsJSON.class);

        return response.getBody();
    }

    public KakaoUserJSON findUserByToken(String accessToken) throws HttpClientErrorException {
        HttpEntity httpEntity = new HttpEntity(buildHeader(accessToken));

        ResponseEntity<KakaoUserJSON> response = this.restTemplate.postForEntity(
                ServiceConstants.BASE_HOST_KAPI + ServiceConstants.END_POINT_USER_ME, httpEntity, KakaoUserJSON.class);

        return response.getBody();
    }

    private HttpHeaders buildHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey);

        return httpHeaders;
    }

    private HttpHeaders buildHeader(String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setBearerAuth(accessToken);

        return httpHeaders;
    }
}
