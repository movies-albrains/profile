package io.albrains.moviebrains.profile.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                    jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                    extractResourceRoles(jwt).stream()
                ).collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {

        Map<String, Collection<String>> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        Map<String, Map<String, Collection<String>>> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);

        Collection<String> allRoles = new ArrayList<>();
        Collection<String> resourceRoles;
        Collection<String> realmRoles ;

        if(resourceAccess != null && resourceAccess.get("account") != null){
            var account =  (Map<String, Collection<String>>) resourceAccess.get("account");
            if(account.containsKey(ROLES_CLAIM) ){
                resourceRoles = account.get(ROLES_CLAIM);
                allRoles.addAll(resourceRoles);
            }
        }

        if(realmAccess != null && realmAccess.containsKey(ROLES_CLAIM)){
            realmRoles = realmAccess.get(ROLES_CLAIM);
            allRoles.addAll(realmRoles);
        }

        // TODO verify the client
        /*if (allRoles.isEmpty() || !Objects.equals(resourceId,jwt.getClaim("azp")) ) {
            return Set.of();
        }*/

        return allRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

}
