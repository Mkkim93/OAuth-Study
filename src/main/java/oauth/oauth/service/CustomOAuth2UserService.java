package oauth.oauth.service;

import lombok.extern.slf4j.Slf4j;
import oauth.oauth.dto.*;
import oauth.oauth.entity.UserEntity;
import oauth.oauth.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("oAuth2User = " + oAuth2User);

        // 소셜 경로 확인 (naver or google)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("registrationId={}", registrationId);
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
            log.info("google oAuthResponse email ={}", oAuth2Response.getEmail());
            log.info("google oAuthResponse name ={}", oAuth2Response.getName());

        } else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());

        } else if (registrationId.equals("kakao")) {

            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
            log.info("kakao oAuthResponse email ={}", oAuth2Response.getEmail());
            log.info("kakao oAuthResponse name ={}", oAuth2Response.getName());
            System.out.println("oAuth2Response.getEmail() = " + oAuth2Response.getEmail());
            System.out.println("oAuth2Response.getName() = " + oAuth2Response.getName());
            System.out.println("oAuth2Response.getProviderId() = " + oAuth2Response.getProviderId());

        } else {

            return null;
        }

        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        UserEntity existData = userRepository.findByUsername(username);

        if (existData == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(username);
            userEntity.setName(oAuth2Response.getName());
            userEntity.setEmail(oAuth2Response.getEmail());
            userEntity.setRole("ROLE_USER");

            userRepository.save(userEntity);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        } else { // 한번이라도 로그인 한 경우
            existData.setEmail(oAuth2Response.getEmail());
            existData.setName(oAuth2Response.getName());

            userRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.getRole());
            return new CustomOAuth2User(userDTO);
        }
    }
}
