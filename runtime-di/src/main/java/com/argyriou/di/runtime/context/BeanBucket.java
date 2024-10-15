package com.argyriou.di.runtime.context;

import java.util.HashMap;
import java.util.Map;

public final class BeanBucket implements Bucket {
  private Map<String, Object> beans = new HashMap<>();

  @Override
  public <T> T get(Class<T> type) {
    return (T) beans.get(type.getName());
  }

  @Override
  public <T> void add(T bean) {
    beans.put(bean.getClass().getName(), bean);
  }
}
