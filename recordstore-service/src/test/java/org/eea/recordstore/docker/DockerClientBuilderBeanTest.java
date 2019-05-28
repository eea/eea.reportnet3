package org.eea.recordstore.docker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;


@RunWith(MockitoJUnitRunner.class)
public class DockerClientBuilderBeanTest {

  @InjectMocks
  DockerClientBuilderBean dockerClientBuilderBean;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(dockerClientBuilderBean, "dockerServerUrl",
        "tcp://localhost:2375");
  }

  @Test
  public void testDockerClient() {
    dockerClientBuilderBean.dockerClient();
  }

}
