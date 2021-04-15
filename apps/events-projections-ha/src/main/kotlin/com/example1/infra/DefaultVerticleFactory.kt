package com.example1.infra

import io.vertx.core.Promise
import io.vertx.core.Verticle
import io.vertx.core.impl.verticle.CompilingClassLoader
import io.vertx.core.spi.VerticleFactory
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable

class DefaultVerticleFactory<V: Verticle>(private val injector: (V) -> V) : VerticleFactory {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultVerticleFactory::class.java)
        private const val prefix = "v"
    }

    override fun prefix(): String = prefix

    override fun createVerticle(
        verticleName: String,
        classLoader: ClassLoader,
        promise: Promise<Callable<Verticle>>
    ) {
        fun getClass(verticleClassName: String): Class<*> {
            if (verticleClassName.endsWith(".java")) {
                val compilingLoader = CompilingClassLoader(classLoader, verticleClassName)
                val className = compilingLoader.resolveMainClassName()
                return compilingLoader.loadClass(className)
            } else {
                return classLoader.loadClass(verticleClassName)
            }
        }
        log.info("Will find for $verticleName")
        val verticleClassName = VerticleFactory.removePrefix(verticleName)
        try {
            val clazz: Class<*> = getClass(verticleClassName)
            log.info("Found clazz ${clazz.name}")
            val verticle = clazz.newInstance() as V
            val injectedVerticle = injector.invoke(verticle)
            promise.complete { injectedVerticle}
        } catch (e: Exception) {
            log.error("Did not found $verticleClassName", e)
            promise.fail(e)
        }

    }
}
// example
//        val injectorFn = { v: CustomerVerticle ->
//            v.controller = controller
//            v
//        }
//        vertx.registerVerticleFactory(DefaultVerticleFactory(injectorFn))
//        vertx.deployVerticle("v:${CustomerVerticle::class.java.name}", deploymentOptions)
//            .onSuccess { log.info("Successfully started") }
//            .onFailure { log.error("When starting", it) }