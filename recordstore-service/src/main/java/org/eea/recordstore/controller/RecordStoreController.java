package org.eea.recordstore.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import java.util.concurrent.TimeUnit;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.service.DockerInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recordstore")
public class RecordStoreController implements
    org.eea.interfaces.controller.recordstore.RecordStoreController {

  @Autowired
  private DockerInterfaceService dockerInterfaceService;

  @Override
  @RequestMapping(value = "/dataset/create", method = RequestMethod.POST)
  public void createEmptyDataset(String containerName) {
    DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://localhost:2375")
        .build();

    //line to run a crunchy container
    //docker run -d -e PG_DATABASE=datasets -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary -e PG_USER=root -e PG_PASSWORD=root -e PG_PRIMARY_USER=root -e PG_PRIMARY_PASSWORD=root
    // -e PG_ROOT_PASSWORD=root -e PGBACKREST=true -p 5432:5432 --name crunchy-postgr
    //es crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1

    Container oldContainer = dockerInterfaceService.getContainer(containerName);

    dockerInterfaceService.stopAndRemoveContainer(oldContainer);

    Container container = dockerInterfaceService
        .createContainer(containerName, "crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1",
            "5432:5432");

    dockerInterfaceService.startContainer(container, 10l, TimeUnit.SECONDS);
    //create init file in container

    dockerInterfaceService
        .copyFileFromHostToContainer(containerName, "C:\\opt\\dump\\init.sql", "/pgwal");
    //"psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql "
    dockerInterfaceService.executeCommandInsideContainer(
        "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql ", container, 0l,
        TimeUnit.SECONDS);

  }

  @Override
  public ConnectionDataVO getConnectionToDataset(String s) {
    return null;
  }
}
