package org.start2do.test.plugin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {


    public String version() {
        return "2025年12月10日16:02:26";
    }

}
