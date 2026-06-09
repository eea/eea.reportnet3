package org.eea.interfaces.controller.dremio.controller;

import org.eea.interfaces.vo.dremio.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.Map;

@FeignClient(name = "dremioClient", url = "${spring.cloud.openfeign.client.config.dremioClient.url}")
public interface DremioApiController {

    @PostMapping(value = "apiv2/login", produces = MediaType.APPLICATION_JSON)
    DremioAuthResponse login(@RequestBody DremioCredentials dremioCredentials);

    @GetMapping(value = "api/v3/catalog/by-path/{path}", produces = MediaType.APPLICATION_JSON)
    DremioDirectoryItemsResponse getDirectoryItems(@RequestHeader(value = "Authorization") String token, @PathVariable("path") String path);

    @PostMapping(value = "api/v3/catalog/{id}", produces = MediaType.APPLICATION_JSON)
    void promote(@RequestHeader(value = "Authorization") String token, @PathVariable("id") String folderId, @RequestBody DremioPromotionRequestBody body);

    @DeleteMapping(value = "api/v3/catalog/{id}", produces = MediaType.APPLICATION_JSON)
    void demote(@RequestHeader(value = "Authorization") String token, @PathVariable("id") String folderId);

    @PostMapping(value = "api/v3/sql", produces = MediaType.APPLICATION_JSON)
    DremioSqlResponse sqlQuery(@RequestHeader(value = "Authorization") String token, @RequestBody DremioSqlRequestBody body);

    @GetMapping(value = "api/v3/job/{id}", produces = MediaType.APPLICATION_JSON)
    DremioJobStatusResponse pollForJobStatus(@RequestHeader(value = "Authorization") String token, @PathVariable("id") String id);

    @PostMapping(value = "api/v3/sql", produces = MediaType.APPLICATION_JSON)
    String sqlQueryString(@RequestHeader(value = "Authorization") String token, @RequestBody DremioSqlRequestBody body);

    @GetMapping(value = "/api/v3/job/{id}/results", produces = MediaType.APPLICATION_JSON)
    Object sqlApiResults(@RequestHeader(value = "Authorization") String token, @PathVariable("id") String id);

    /**
     * Checks the Dremio server status.
     *
     * <p>Calls {@code GET apiv2/server_status} to verify that the Dremio HTTP server
     * is reachable and operational. A successful response returns HTTP 200 with a
     * JSON-quoted string body of {@code "\"OK\""}.
     *
     * @return a {@link ResponseEntity} containing the response body as a quoted JSON
     *         string; HTTP 200 with body {@code "OK"} (after unquoting) indicates
     *         healthy, any other status or body value indicates a problem
     */
    @GetMapping(value = "apiv2/server_status", produces = MediaType.APPLICATION_JSON)
    ResponseEntity<String> getServerStatus();

}
