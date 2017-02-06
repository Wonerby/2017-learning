package com.demo.wonerby;

public class Test {
  public void multiply(int to) {
    for(int i = 1; i < to + 1; i ++) {
      System.out.printf("%d * %d = %d", i, to, i * to);
    }
    System.out.println("");
  }
  public static void main(String[] args) {
    for(int i = 1; i < 10; i ++){
      multiply(i);
    }
  }
}
