package org.eea.recordstore.docker;

import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;


/**
 * The Class DockerClientBuilderBeanTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DockerClientBuilderBeanTest {

  /** The docker client builder bean. */
  @InjectMocks
  private DockerClientBuilderBean dockerClientBuilderBean;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(dockerClientBuilderBean, "dockerServerUrl",
        "tcp://localhost:2375");
  }

  /**
   * Test docker client.
   */
  @Test
  public void testDockerClient() {
    assertNotNull("assertion error", dockerClientBuilderBean.dockerClient());
  }

}
