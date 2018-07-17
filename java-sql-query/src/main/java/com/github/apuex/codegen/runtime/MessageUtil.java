package com.github.apuex.codegen.runtime;

import java.util.List;
import java.util.Map;

import static com.github.apuex.codegen.runtime.Messages.*;

public class MessageUtil {
  public static FilterPredicate createPredicate(PredicateType predicateType, String name, String value, Map<String, String> params) {
    return Message.createPredicate(predicateType, name, value, params);
  }

  public static FilterPredicate createConnection(LogicalConnectionType connectionType, List<FilterPredicate> predicates) {
    return Message.createConnection(connectionType, predicates);
  }
}
