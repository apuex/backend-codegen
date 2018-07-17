package com.github.apuex.codegen.runtime;

public class IdentityConverter implements SymbolConverter {
  @Override
  public String convert(String s) {
    return s;
  }
}
