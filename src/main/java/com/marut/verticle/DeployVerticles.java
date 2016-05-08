package com.marut.verticle;

import com.marut.NonBlockingVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by marutsingh on 3/21/16.
 */
@Service
public class DeployVerticles implements Deployer {

    @Autowired
    ApplicationContext applicationContext;

    public void deployVerticles(Vertx vertx,Collection<Object> verticleClasses) {
        for (Object verticleClass: verticleClasses){
            vertx.deployVerticle((AbstractVerticle)verticleClass);
        }
    }

    @Override
    public void deployVerticles(Vertx vertx) {
        Map<String,Object> beans = applicationContext.getBeansWithAnnotation(NonBlockingVerticle.class);
        deployVerticles(vertx,beans.values());
    }

    @Override
    public void deployVerticles(List<AbstractVerticle> verticleList, Vertx vertx) {
        verticleList.stream().forEach(r -> vertx.deployVerticle(r));
    }

    @Override
    public void deployVerticles(List<AbstractVerticle> verticleList, Vertx vertx,DeploymentOptions deploymentOptions) {
        verticleList.stream().forEach(r -> vertx.deployVerticle(r,deploymentOptions));
    }
}
