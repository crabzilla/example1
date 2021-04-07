package com.example1.infra

import com.hazelcast.config.Config
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.kotlin.coroutines.await
import io.vertx.spi.cluster.hazelcast.ConfigUtil
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager


class VertxFactory {

    // https://medium.com/halofina-techology/high-performance-web-app-with-vert-x-cluster-ce709d2cc804
    suspend fun clusteredVertx(): Vertx {
        val hazelcastConfig: Config = ConfigUtil.loadConfig().setLiteMember(false)
        hazelcastConfig.cpSubsystemConfig.cpMemberCount = 3
        val mgr: ClusterManager = HazelcastClusterManager(hazelcastConfig)
        val options = VertxOptions().setClusterManager(mgr).setHAEnabled(true)
        return Vertx.clusteredVertx(options).await()
    }

    //fun getConfig(vertx: Vertx): Future<JsonObject> {
//	val envOptions = ConfigStoreOptions()
//		.setType("file")
//		.setFormat("properties")
//		.setConfig(JsonObject().put("path", "../example1.env"))
//	val options = ConfigRetrieverOptions().addStore(envOptions)
//	val retriever = ConfigRetriever.create(vertx, options)
//	return retriever.config
//}

}