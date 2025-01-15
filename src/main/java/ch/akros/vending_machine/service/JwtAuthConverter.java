package ch.akros.vending_machine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthConverter.class);
  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
  private final String principalAttributeName;
  private final String resourceId;

  public JwtAuthConverter(@Value("${jwt.auth.converter.principal-attribute}") String principalAttributeName,
                          @Value("${jwt.auth.converter.resource-id}") String resourceId) {
    this.principalAttributeName = principalAttributeName;
    this.resourceId = resourceId;
  }

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt source) {
    var value = jwtGrantedAuthoritiesConverter.convert(source).stream();
    Collection<GrantedAuthority> authorities = Stream.concat(value, extractResourceRoles(source).stream()).collect(Collectors.toSet());
    return new JwtAuthenticationToken(source, authorities, getPrincipalClaimName(source));
  }

  private String getPrincipalClaimName(Jwt source) {
    String claimName = JwtClaimNames.SUB;
    if (principalAttributeName != null) {
      claimName = principalAttributeName;
    }
    String username = source.getClaim(claimName);
    LOGGER.info("Jwt claim {} is: {}", claimName, username);
    return username;
  }

  private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt source) {
    Map<String, Object> resourceAccess;
    Map<String, Object> resource;
    Collection<String> resourceRoles;

    if (source.getClaim("resource_access") == null) {
      return Set.of();
    }

    resourceAccess = source.getClaim("resource_access");
    if (resourceAccess.get(resourceId) == null) {
      return Set.of();
    }

    resource = (Map<String, Object>) resourceAccess.get(resourceId);
    resourceRoles = (Collection<String>) resource.get("roles");
    var convertedRoles = resourceRoles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toSet());
    LOGGER.info("Jwt Raw role(s) {} Jwt converted Role(s) {}", resourceRoles, convertedRoles);
    return convertedRoles;
  }
}
