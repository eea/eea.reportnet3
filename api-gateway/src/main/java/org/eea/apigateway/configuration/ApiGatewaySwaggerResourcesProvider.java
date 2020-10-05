package org.eea.apigateway.configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * The type Api gateway  swagger resources provider.
 */
@Component
@Primary
public class ApiGatewaySwaggerResourcesProvider implements SwaggerResourcesProvider {


  @Autowired
  private RouteLocator routeLocator;


  @Override
  public List<SwaggerResource> get() {
    List<SwaggerResource> resources = new ArrayList<>();

    //Add the default swagger resource that correspond to the gateway's own swagger doc
    resources.add(swaggerResource("default", "/v2/api-docs"));

    //Add the registered microservices swagger docs as additional swagger resources. Get all the routes registered in the ApiGateway configuration
    List<Route> routes = routeLocator.getRoutes();
    //Given that it is possible that a microservice has several Rest interfaces exposed under the same endpoint it's necessary to avoid duplicated configurations
    Set<String> registeredResources = new HashSet<>();
    registeredResources.add(
        "consul"); //avoid showing configuration for consul. It should not do it but routeLocator retrieves it as well
    routes.forEach(route -> {
      //if the microservice (route) has not been already added to the Swagger resources
      if (!registeredResources.contains(route.getLocation())) {

        //finally adding the Swagger Resource as pair "Name","Origin of JSON Swagger Rest Document".
        // In this case, the microservices rest api swagger documentation will come from the rest of the microservices
        StringBuilder endpoint = new StringBuilder("/" + route.getLocation())
            .append("/v2/api-docs");
        resources
            .add(swaggerResource(route.getLocation(),
                endpoint.toString()));
        registeredResources.add(route.getLocation());
      }

    });

    return resources;
  }


  private SwaggerResource swaggerResource(String name, String location) {
    SwaggerResource swaggerResource = new SwaggerResource();
    swaggerResource.setName(name);
    swaggerResource.setLocation(location);
    swaggerResource.setSwaggerVersion("2.0");
    return swaggerResource;
  }
}

