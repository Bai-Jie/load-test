package gq.baijie.loadtest.business;

public class Configuration {

  private long taskTotalNumber;

  private long concurrencyNumber;

  public long getTaskTotalNumber() {
    return taskTotalNumber;
  }

  public void setTaskTotalNumber(long taskTotalNumber) {
    this.taskTotalNumber = taskTotalNumber;
  }

  public long getConcurrencyNumber() {
    return concurrencyNumber;
  }

  public void setConcurrencyNumber(long concurrencyNumber) {
    this.concurrencyNumber = concurrencyNumber;
  }

}
