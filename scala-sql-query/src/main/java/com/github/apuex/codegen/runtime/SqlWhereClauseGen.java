package com.github.apuex.codegen.runtime;

import java.util.List;

public class SqlWhereClauseGen {
  private SqlWhereClause clause;

  public SqlWhereClauseGen(SymbolConverter converter) {
    this.clause = new SqlWhereClause(converter.converter());
  }

  String toWhereClause(FilterPredicate criteria, int indent) {
    return this.clause.toWhereClause(criteria, indent);
  }

  List<String> toUnnamedParamList(QueryCommand q) {
    return this.clause.toUnnamedParamList(q);
  }
}
