package com.marut;

import com.marut.verticle.Deployer;
import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runner for the vertx-spring sample
 *
 */
@SpringBootApplication
public class DistributedTaskProcessor {

    static Vertx vertx;

    public static void main( String[] args ) throws Exception {
        Options cmdoptions = new Options();
        cmdoptions.addOption("mode", true, "Mode of application");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( cmdoptions, args);
        String mode = cmd.getOptionValue("mode");
        System.out.println(String.format("Starting task scheduler in %s mode",mode));

        ConfigurableApplicationContext context =
                SpringApplication.run(DistributedTaskProcessor.class, args);
        Deployer deployer = context.getBean(Deployer.class);

        Config hazelcastConfig = new Config();
        hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
        hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);

        mgr.nodeListener(new NodeListener() {
            @Override
            public void nodeAdded(String s) {

            }

            @Override
            public void nodeLeft(String s) {

            }
        });


        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                vertx = res.result();
                List<AbstractVerticle> abstractVerticleList = new ArrayList<>();

                Collection<Object> verticles = context.getBeansWithAnnotation(NonBlockingVerticle.class).values();
                abstractVerticleList = verticles.stream().map(t -> (AbstractVerticle) t)
                        .collect(Collectors.<AbstractVerticle>toList());

                Collection<Object> workerVerticles = context.getBeansWithAnnotation(WorkerVerticle.class).values();
                List<AbstractVerticle> workerVerticleList = workerVerticles.stream().map(t -> (AbstractVerticle) t)
                        .collect(Collectors.<AbstractVerticle>toList());

                DeploymentOptions deployOptions = new DeploymentOptions().setWorker(true);

                if(mode == null){
                        deployer.deployVerticles(abstractVerticleList,vertx);
                        deployer.deployVerticles(workerVerticleList,vertx,deployOptions);
                }
                else if (mode.equals("producer")){
                    deployer.deployVerticles(abstractVerticleList,vertx);
                }else if (mode.equals("consumer")){
                    deployer.deployVerticles(workerVerticleList,vertx,deployOptions);
                }

            }
        });
    }
}
