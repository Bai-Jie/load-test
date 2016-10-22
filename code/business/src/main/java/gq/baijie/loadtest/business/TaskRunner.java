package gq.baijie.loadtest.business;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.Executors;

import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class TaskRunner {

  private final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor(r->{
    Thread thread = new Thread(r);
    thread.setDaemon(true);
    return thread;
  }));

  private Configuration configuration;

  private final BehaviorSubject<Integer> runningTaskNumber = BehaviorSubject.create(0);
  private final BehaviorSubject<Long> finishedTaskNumber = BehaviorSubject.create(0L);

  private TestCaseFactory taskFactory;

  public TaskRunner(Configuration configuration,
                    TestCaseFactory taskFactory) {
    this.configuration = configuration;
    this.taskFactory = taskFactory;
  }

  public Observable<Long> getFinishedTaskNumber() {
    return finishedTaskNumber.asObservable();
  }

  void start() {
    StopWatch stopWatch = new StopWatch();

    Observable<Completable> taskObservable =
        Observable.create(SyncOnSubscribe.createSingleState(() -> {
          taskFactory.init();
          System.out.println("start benchmark");
          stopWatch.start();
          return null;
        }, (s, observer) -> {
          observer.onNext(taskFactory.getNextTestCase());
        }, s -> {
          stopWatch.stop();
          System.out.println("all test run in: " + stopWatch);
          double tsPerS = (double) (configuration.getTaskTotalNumber() * 1000) / stopWatch.getTime();
          System.out.println("tasks/s: " + tsPerS);

          taskFactory.destory();
        }));

    taskObservable.observeOn(scheduler).subscribe(new Subscriber<Completable>() {
      @Override
      public void onStart() {
        request(configuration.getConcurrencyNumber());
      }

      @Override
      public void onCompleted() {

      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onNext(Completable completable) {
//        System.out.printf("[%s]onNext task%n", Thread.currentThread());
        runningTaskNumber.onNext(runningTaskNumber.getValue() + 1);
        completable.observeOn(scheduler).subscribe(() -> {
          onFinish();
        }, throwable -> {
          throwable.printStackTrace();
          onFinish();
        });
      }

      private void onFinish() {
        runningTaskNumber.onNext(runningTaskNumber.getValue() - 1);
        finishedTaskNumber.onNext(finishedTaskNumber.getValue() + 1);
        if (runningTaskNumber.getValue() + finishedTaskNumber.getValue() < configuration.getTaskTotalNumber()) {
          request(1);
        } else {
          if (finishedTaskNumber.getValue() >= configuration.getTaskTotalNumber()) {
            unsubscribe();
          }
        }
      }
    });
  }

}
