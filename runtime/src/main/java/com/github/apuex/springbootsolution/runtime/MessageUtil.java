package com.github.apuex.springbootsolution.runtime;

import java.util.List;
import java.util.Map;

public class MessageUtil {
  public static FilterPredicate createPredicate(PredicateType predicateType, String name, String value, Map<String, String> params) {
    return Message.createPredicate(predicateType, name, value, params);
  }

  public static FilterPredicate createConnection(LogicalConnectionType connectionType, List<FilterPredicate> predicates) {
    return Message.createConnection(connectionType, predicates);
  }
}
