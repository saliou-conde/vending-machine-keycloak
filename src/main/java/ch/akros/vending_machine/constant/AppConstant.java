package ch.akros.vending_machine.constant;

/**
 * AppConstant for all constants in the application
 */
public class AppConstant {
  public static final String[] PUBLIC_URLS = {
          "/actuator/**",
          "/v2/api-docs",
          "/v3/api-docs",
          "/v3/api-docs/**",
          "/swagger-resources",
          "/swagger-resources/**",
          "/configuration/ui",
          "/configuration/security",
          "/swagger-ui/**",
          "/webjars/**",
          "/swagger-ui.html"
  };
  public static final String PRODUCT_API_PATH = "/api/v1/products/";
  public static final String PRODUCT_KEY = "product";
}
