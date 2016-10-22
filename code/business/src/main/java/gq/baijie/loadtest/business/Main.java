package gq.baijie.loadtest.business;

import java.util.concurrent.CountDownLatch;

public class Main {

  public static void main(String[] args) {
    Configuration configuration = new Configuration();
    configuration.setConcurrencyNumber(192);
    configuration.setTaskTotalNumber(1024*1024);

    TestCaseFactory taskFactory = new SampleTestCaseFactory();

    CountDownLatch latch = new CountDownLatch(1);

    final TaskRunner taskRunner = new TaskRunner(configuration, taskFactory);
    taskRunner.getFinishedTaskNumber()
        .map(finishedNum -> finishedNum / (16*1024))
        .distinctUntilChanged()
        .subscribe(finishedNum->System.out.println(finishedNum*16+" K"));
    taskRunner.getFinishedTaskNumber()
        .map(finished->finished>=configuration.getTaskTotalNumber())
        .filter(finished->finished)
        .first()
        .toSingle()
        .subscribe(finished->latch.countDown());
    taskRunner.start();

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
