package gq.baijie.loadtest.business;

import rx.Completable;

public interface TestCaseFactory {

  void init();

  Completable getNextTestCase();

  void destory();

}
