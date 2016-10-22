package gq.baijie.loadtest.business;

import java.io.IOException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import rx.Completable;

public class SampleTestCaseFactory implements TestCaseFactory {

  private Vertx vertx;
  private HttpClient client;

  @Override
  public void init() {
    vertx = Vertx.vertx();
    client = vertx.createHttpClient();
  }

  @Override
  public Completable getNextTestCase() {
    return Completable.create((Completable.OnSubscribe) subscriber -> {
//      client.getNow("www.baidu.com", "/", event -> {
      client.getNow("localhost", "/api/blogs/1", event -> {
//      client.getNow("blog.baijie.gq", "/api/blogs/1", event -> {
//      client.getNow("blog.baijie.gq", "/", event -> {
//        print(String.format("[%s]", Thread.currentThread()));
        final int statusCode = event.statusCode();
//        println("Received response with status code " + statusCode);
        if (statusCode == 200) {
          subscriber.onCompleted();
        } else {
          subscriber.onError(new IOException("failure statusCode: " + statusCode));
        }
      });
    });
  }

  @Override
  public void destory() {
    vertx.close();
  }

  private static void print(Object x) {
    System.out.print(x);
  }

  private static void println(Object x) {
    System.out.println(x);
  }

}
