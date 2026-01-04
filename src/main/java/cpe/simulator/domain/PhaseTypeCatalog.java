package cpe.simulator.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Catalogue des types de phases indexé par code. */
public final class PhaseTypeCatalog {

  private final Map<String, PhaseType> byCode;

  public PhaseTypeCatalog(List<PhaseType> phaseTypes) {
    Map<String, PhaseType> index = new HashMap<>();
    for (PhaseType pt : phaseTypes) {
      if (pt != null && pt.code() != null && !pt.code().isBlank()) {
        index.put(pt.code(), pt);
      }
    }
    this.byCode = Collections.unmodifiableMap(index);
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
