package org.eea.apigateway.configuration;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.REQUEST_URI_KEY;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

/**
 * Filter to rewrite Swagger api-docs requests uri's.
 */
@Component
public class SwaggerZuulFilter extends ZuulFilter {


  @Override
  public boolean shouldFilter() {
    RequestContext context = RequestContext.getCurrentContext();
    Object originalRequestPath = context.get(REQUEST_URI_KEY);
    return originalRequestPath.toString().endsWith("/v2/api-docs");
  }

  @Override
  public Object run() throws ZuulException {
    RequestContext context = RequestContext.getCurrentContext();
    String modifiedRequestPath = "/v2/api-docs";
    context.put(REQUEST_URI_KEY, modifiedRequestPath);

    return null;
  }

  @Override
  public String filterType() {
    //setting this filter as prerouting as we need to modify routes
    return "pre";
  }

  @Override
  public int filterOrder() {
    //this filter will run right after PRE_DECORATION filter
    return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
  }
}
