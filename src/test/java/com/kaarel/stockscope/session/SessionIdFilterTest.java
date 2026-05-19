package com.kaarel.stockscope.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class SessionIdFilterTest {

    private final SessionIdFilter filter = new SessionIdFilter();

    @Test
    void generates_new_session_id_when_cookie_missing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Cookie cookie = response.getCookie(SessionIdHolder.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotBlank();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isPositive();
        assertThat(request.getAttribute(SessionIdHolder.REQUEST_ATTRIBUTE)).isEqualTo(cookie.getValue());
    }

    @Test
    void reuses_existing_session_id_from_cookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(SessionIdHolder.COOKIE_NAME, "existing-session-id"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getAttribute(SessionIdHolder.REQUEST_ATTRIBUTE)).isEqualTo("existing-session-id");
        assertThat(response.getCookie(SessionIdHolder.COOKIE_NAME)).isNull();
    }

    @Test
    void treats_blank_cookie_as_missing_and_generates_new() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(SessionIdHolder.COOKIE_NAME, ""));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        Cookie cookie = response.getCookie(SessionIdHolder.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotBlank();
    }
}
