package com.argyriou.di.runtime.context;

public sealed interface Bucket permits BeanBucket {
  <T> T get(Class<T> type);

  <T> void add(T bean);
}
