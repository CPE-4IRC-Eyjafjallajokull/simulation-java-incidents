package cpe.simulator.api;

/** Interface de logging injectable. */
public interface Logger {

  void info(String message);

  void error(String message);
}
