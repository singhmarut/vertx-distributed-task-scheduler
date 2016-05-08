package com.marut.verticle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marut.entity.Task;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Simple web server verticle to expose the results of the Spring service bean call (routed via a verticle - see
 * SpringDemoVerticle)
 */
@com.marut.WorkerVerticle
@Service
public class WorkerVerticle extends AbstractVerticle {

    final int timeOut = 5000;
    public WorkerVerticle(){
        super();
    }

    @Override
    public void start() throws Exception {
        super.start();
        System.out.println("I am ready..Give me some work");
        requestNewTask(new Task());
    }

    private Handler<AsyncResult<Message<Object>>> receiveTask(){

       return new Handler<AsyncResult<Message<Object>>>() {
            @Override
            public void handle(AsyncResult<Message<Object>> result) {
                if (result.succeeded()) {
                    try {
                        if (result.result().body().toString().equals("Empty")){

                        }else{
                            Task newTask = new ObjectMapper().readValue(result.result().body().toString(), Task.class);
                            System.out.println("I received a new Task " + result.result().body() + "Worker: " +
                                    Long.toString(Thread.currentThread().getId()));

                            vertx.executeBlocking(future -> {
                                executeBlocking();
                                future.complete();
                            }, res -> {
                                if (res.succeeded()) {
                                    System.out.println(String.format("%s, %s", "I processed a new Task ", "Requesting a new one"));
                                    //Just inform that task is completed
                                    newTask.setStatus(Task.TaskStatus.PROCESSED);
                                    requestNewTask(newTask);
                                }
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println(String.format("%s", "Err..did not get a new task..try again "));
                    requestNewTask(new Task());
                }
            }
        };
    }

    void requestNewTask(Task task){
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSendTimeout(timeOut);
        try {
            String taskStr = new ObjectMapper().writeValueAsString(task);
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("status",task.getStatus());
            jsonObject.put("key",task.getUuid());

            vertx.eventBus().send(TaskProducerVerticle.TASK_PULL_EVENT,taskStr,deliveryOptions, receiveTask());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulate a long running operation
     */
    void executeBlocking(){
        java.util.Date date= new java.util.Date();
        long startTime = date.getTime();
        long diff = 0;
        while (diff < 5000){
            date= new java.util.Date();
            long endTime = date.getTime();
            diff = endTime - startTime;
        }
    }
}
