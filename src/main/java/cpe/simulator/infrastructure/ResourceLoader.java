package cpe.simulator.infrastructure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Charge des ressources depuis le classpath ou le filesystem. */
public final class ResourceLoader {

  private ResourceLoader() {}

  public static InputStream open(String path) throws IOException {
    Path filePath = Path.of(path);
    if (Files.exists(filePath)) {
      return Files.newInputStream(filePath);
    }
    InputStream input = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
    if (input == null) {
      throw new FileNotFoundException("Resource not found: " + path);
    }
    return input;
  }
}
