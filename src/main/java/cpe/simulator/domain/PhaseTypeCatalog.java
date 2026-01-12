package cpe.simulator.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Catalogue des types de phases indexé par code. */
public final class PhaseTypeCatalog {

  private final Map<String, PhaseType> byCode;

  public PhaseTypeCatalog(List<PhaseType> phaseTypes) {
    this.byCode = phaseTypes.stream()
        .filter(pt -> pt != null && pt.code() != null && !pt.code().isBlank())
        .collect(Collectors.toUnmodifiableMap(
            PhaseType::code,
            Function.identity(),
            (existing, replacement) -> existing
        ));
  }

  public PhaseType byCode(String code) {
    return byCode.get(code);
  }

  public Set<String> codes() {
    return byCode.keySet();
  }

  public int size() {
    return byCode.size();
  }
}
