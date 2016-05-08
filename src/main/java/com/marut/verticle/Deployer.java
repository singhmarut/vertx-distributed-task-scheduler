package com.marut.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.List;

/**
 * Created by marutsingh on 3/21/16.
 */
public interface Deployer {
    void deployVerticles(Vertx vertx);
    void deployVerticles(List<AbstractVerticle> verticleList, Vertx vertx);
    void deployVerticles(List<AbstractVerticle> verticleList, Vertx vertx, DeploymentOptions deploymentOptions);
}
