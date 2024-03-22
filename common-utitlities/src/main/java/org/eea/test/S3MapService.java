package org.eea.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class S3MapService {
  private final Map<String, S3Configuration> clientMap = new HashMap<>();

  @Autowired
  public S3MapService(Set<S3Configuration> s3Clients) {
    s3Clients.forEach(s3Client -> clientMap.put(s3Client.getClass().getSimpleName(), s3Client));
  }

  public S3Configuration getS3Configuration(String name) {
    return clientMap.get(name);
  }
}
