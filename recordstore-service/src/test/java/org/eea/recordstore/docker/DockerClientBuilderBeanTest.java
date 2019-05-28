package org.eea.recordstore.docker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DockerClientBuilderBeanTest {

  @InjectMocks
  DockerClientBuilderBean dockerClientBuilderBean;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testDockerClient() {
    dockerClientBuilderBean.dockerClient();
  }

}
