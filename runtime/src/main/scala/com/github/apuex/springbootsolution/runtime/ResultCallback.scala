package com.github.apuex.springbootsolution.runtime

trait ResultCallback[T] {
  def add(row: T): Unit
}
