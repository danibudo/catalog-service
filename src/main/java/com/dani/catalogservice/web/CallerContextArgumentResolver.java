package com.dani.catalogservice.web;

import com.dani.catalogservice.model.UserRole;
import com.dani.catalogservice.service.CallerContext;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

public class CallerContextArgumentResolver implements HandlerMethodArgumentResolver {

    static final String HEADER_USER_ID   = "X-User-Id";
    static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return CallerContext.class.equals(parameter.getParameterType());
    }

    @Override
    public CallerContext resolveArgument(@NonNull MethodParameter parameter,
                                         ModelAndViewContainer mavContainer,
                                         @NonNull NativeWebRequest webRequest,
                                         WebDataBinderFactory binderFactory) {
        String userIdHeader = webRequest.getHeader(HEADER_USER_ID);
        String roleHeader   = webRequest.getHeader(HEADER_USER_ROLE);

        if (userIdHeader == null || roleHeader == null) {
            return null;
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);
            UserRole role = UserRole.fromValue(roleHeader);
            return new CallerContext(userId, role);
        } catch (IllegalArgumentException e) {
            // Malformed UUID or unrecognised role — treat as unauthenticated
            return null;
        }
    }
}