import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PruebaDocker {

  @Test
  public void pruebaDocker() {
//    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//        .withDockerHost("tcp://localhost:2375").build();
    DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://localhost:2375")
        .build();
    List<Container> containers = dockerClient.listContainersCmd()
        .withShowSize(true)
        .withShowAll(true)
        .withNameFilter(Arrays.asList(new String[]{"crunchy-postgres"}))
        .exec();
    containers.stream().forEach(System.out::println);
    containers.get(0);
    //dockerClient.stopContainerCmd(containers.get(0).getId()).exec();
    dockerClient.startContainerCmd(containers.get(0).getId()).exec();
  }
}
