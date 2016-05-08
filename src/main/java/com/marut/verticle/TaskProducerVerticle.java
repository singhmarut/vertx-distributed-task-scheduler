package com.marut.verticle;

import com.marut.NonBlockingVerticle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marut.entity.Task;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Simple verticle to wrap a Spring productService bean - note we wrap the productService call
 * in executeBlocking, because we know it's going to be a JDBC call which blocks.
 * As a general principle with Spring beans, the default assumption should be that it will block unless you
 * know for sure otherwise (in other words use executeBlocking unless you know for sure your productService call will be
 * extremely quick to respond)
 */
@Service
@NonBlockingVerticle
public class TaskProducerVerticle extends AbstractVerticle implements Handler<Message<String>>  {

    public static final String TASK_PULL_EVENT = "task.pull";
    private final ObjectMapper mapper = new ObjectMapper();
    //Queue<Task> taskList = new LinkedBlockingQueue<>();
    Queue<Task> taskList = new LinkedList<>();

    public TaskProducerVerticle() {
        for (int i = 0; i < 10; i++){
            taskList.add(new Task());
        }
    }

    private Handler<Message<String>> handOverTask(){

        return new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> stringMessage) {
                System.out.println("Received Request");
                try {
                    Task receivedTask = new ObjectMapper().readValue(stringMessage.body(),Task.class);
//                    if (receivedTask.getStatus() == Task.TaskStatus.PROCESSED){
//                        //Remove this task
//                        System.out.println("Task Processed " + receivedTask.getUuid());
//                        taskList.remove(receivedTask);
//                    }
                    if (!taskList.isEmpty()){
                        stringMessage.reply(new ObjectMapper().writeValueAsString(taskList.remove()));
                    }else{
                        stringMessage.reply("Empty");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().<String>consumer(TASK_PULL_EVENT).handler(handOverTask());
    }

    @Override
    public void handle(Message<String> stringMessage) {

    }
}
