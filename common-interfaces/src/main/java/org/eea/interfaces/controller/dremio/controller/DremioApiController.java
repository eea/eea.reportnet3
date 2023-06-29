package org.eea.interfaces.controller.dremio.controller;

import org.eea.interfaces.vo.dremio.DremioAuthResponse;
import org.eea.interfaces.vo.dremio.DremioCredentials;
import org.eea.interfaces.vo.dremio.DremioDirectoryItemsResponse;
import org.eea.interfaces.vo.dremio.DremioFolderPromotionRequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;

@FeignClient(name = "dremioClient", url = "${spring.cloud.openfeign.client.config.dremioClient.url}")
public interface DremioApiController {

    @PostMapping(value = "apiv2/login", produces = MediaType.APPLICATION_JSON)
    DremioAuthResponse login(@RequestBody DremioCredentials dremioCredentials);

    @GetMapping(value = "api/v3/catalog/by-path/{path}", produces = MediaType.APPLICATION_JSON)
    DremioDirectoryItemsResponse getDirectoryItems(@RequestHeader(value = "Authorization") String token, @PathVariable("path") String path);

    @PostMapping(value = "api/v3/catalog/{id}", produces = MediaType.APPLICATION_JSON)
    void promoteFolder(@RequestHeader(value = "Authorization") String token, @PathVariable("id") String folderId, @RequestBody DremioFolderPromotionRequestBody body);

    @DeleteMapping(value = "api/v3/catalog/{id}", produces = MediaType.APPLICATION_JSON)
    void demoteFolder(@RequestHeader(value = "Authorization") String token, @PathVariable("id") String folderId);
}
