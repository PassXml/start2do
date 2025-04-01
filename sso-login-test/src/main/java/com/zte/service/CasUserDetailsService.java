package com.zte.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CasUserDetailsService extends AbstractCasAssertionUserDetailsService {



    @Override
    protected UserDetails loadUserDetails(Assertion assertion) {
        // 可自定义获取用户信息
        String username = assertion.getPrincipal().getName();
        Map<String, Object> attributes = assertion.getPrincipal().getAttributes();
        log.error("{}", attributes);
        return new User(username, "", true, true,
            true, true, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
    }
}
