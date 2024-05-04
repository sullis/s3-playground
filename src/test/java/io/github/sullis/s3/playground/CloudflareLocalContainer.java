package io.github.sullis.s3.playground;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;


public class CloudflareLocalContainer extends GenericContainer {
  private CloudflareLocalContainer(ImageFromDockerfile imageFromDockerfile) {
    super(imageFromDockerfile);
  }

  public static final CloudflareLocalContainer create() {
    ImageFromDockerfile imageFromDockerfile = new ImageFromDockerfile()
        .withDockerfileFromBuilder(builder ->
            builder
                .from("node")
                .run("npm install -g wrangler")
                .entryPoint("wrangler")
                .build());
    return new CloudflareLocalContainer(imageFromDockerfile);
  }
}
