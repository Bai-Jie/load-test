package gq.baijie.loadtest.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class Main {

  public static void main(String[] args) {
    Configuration configuration = new Configuration();
    configuration.setConcurrencyNumber(192);
    configuration.setTaskTotalNumber(1024*1024);

    TestCaseFactory taskFactory = new SampleTestCaseFactory();

    CountDownLatch latch = new CountDownLatch(1);

    final TaskRunner taskRunner = new TaskRunner(configuration, taskFactory);

    final Observable<Boolean> finishedObservable = taskRunner.getFinishedTaskNumberObservable()
        .map(finished -> finished >= configuration.getTaskTotalNumber())
        .filter(finished -> finished)
        .first()
        .publish()
        .refCount();
    Observable.interval(0, 1, TimeUnit.SECONDS)
        .takeUntil(finishedObservable.delay(1, TimeUnit.SECONDS))
        .map(counter->taskRunner.getFinishedTaskNumber())
        .scan(Pair.of(0L, 0L), (previous, current) -> Pair.of(previous.getRight(), current))
        .map(pairWithPrevious->pairWithPrevious.getRight() - pairWithPrevious.getLeft())
        .subscribe(finishedPerSecond->{
          System.out.print(StringUtils.repeat(' ', (int) (finishedPerSecond / 250)) + "| ");
          System.out.println(finishedPerSecond + " tasks/s");
        }, Throwable::printStackTrace, latch::countDown);

    taskRunner.start();

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
