package io.github.sullis.s3.playground;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;


public class CloudflareLocalContainer extends GenericContainer {
  private static final int DEFAULT_WRANGLER_PORT = 8787;
  private CloudflareLocalContainer(ImageFromDockerfile imageFromDockerfile) {
    super(imageFromDockerfile);
    withExposedPorts(DEFAULT_WRANGLER_PORT);
    /* waitingFor(
        Wait
            .forHttp("/")
            .forPort(DEFAULT_WRANGLER_PORT)
            .forStatusCode(200)
            .withStartupTimeout(Duration.of(5, ChronoUnit.SECONDS))
    ); */
  }

  public static final CloudflareLocalContainer create() {
    ImageFromDockerfile imageFromDockerfile = new ImageFromDockerfile()
        .withFileFromClasspath("index.js", "cloudflare-local/index.js")
        .withFileFromClasspath("run.sh", "cloudflare-local/run.sh")
        .withDockerfileFromBuilder(builder ->
            builder
                .from("node:22-slim")
                .run("npm install -g wrangler")
                .add("index.js", "index.js")
                .add("run.sh", "run.sh")
                .run("chmod +x ./run.sh")
                .entryPoint("./run.sh")
                .build());
    return new CloudflareLocalContainer(imageFromDockerfile);
  }
}
