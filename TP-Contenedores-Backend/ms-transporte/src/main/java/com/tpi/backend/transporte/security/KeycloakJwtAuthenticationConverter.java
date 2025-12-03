package com.tpi.backend.transporte.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        JwtGrantedAuthoritiesConverter baseConverter = new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = baseConverter.convert(source);

        Map<String, Object> access = source.getClaimAsMap("realm_access");
        if (access != null && access.containsKey("roles")) {
            // Casting explícito para evitar problemas de tipos
            List<String> roles = (List<String>) access.get("roles");
            
            List<GrantedAuthority> kcRoles = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                    .collect(Collectors.toList());

            authorities = Stream.concat(authorities.stream(), kcRoles.stream()).collect(Collectors.toSet());
        }

        return new JwtAuthenticationToken(source, authorities);
    }
}