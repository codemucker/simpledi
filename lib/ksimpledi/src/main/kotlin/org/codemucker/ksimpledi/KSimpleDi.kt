package org.codemucker.ksimpledi

/**
 * Copied and modified from https://github.com/klukwist/SimpleDi/tree/main
 *
 * Also see https://proandroiddev.com/simplest-dependency-injection-tool-using-kotlin-fun-interfaces-and-sealed-classes-988fec67b8ff
 *
 * Original author Aleksei Cherniaev, klukwist@gmail.com
 *
 * THis version has been heavily modified to also support starting/stopping 'services', configurable 'modules', eager
 * loading etc, and couroutine support
 */

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * val instanceSomeClass = get<SomeClass> {
 *    params(someParam1ToSomeClass, someParam2ToSomeClass)
 * }
 */
inline fun <reified T : Any> getInstance(noinline params: (InstanceType.ParamFactory.Params.() -> Unit)? = null): T =
    GlobalScope.getInstance(params)


/**
 * module {
 *     factory<C> { CImpl(a = get()) } // for register Factory.
 *     single<A> { AImpl() } // for register Singleton
 * }
 */
fun module(scope: GlobalScope.() -> Unit) {
    scope.invoke(GlobalScope)
}

object GlobalScope : KSimpleDiScope()

interface Instance<T> {
    fun get(): T
}

@JvmInline
value class FactoryInstance<T>(private val factory: InstanceType.Factory<T>) {
    fun get(): T = factory.build()
}

@JvmInline
value class FactoryParamsInstance<T>(private val factory: InstanceType.ParamFactory<T>) {
    fun get(vararg params: Any): T = factory.build(params)
}

@JvmInline
value class FactoryParam1Instance<T, P>(private val factory: InstanceType.ParamFactory1<T, P>) {
    fun get(param: P): T = factory.build(param)
}

@JvmInline
value class FactoryParams2Instance<T, P1, P2>(private val factory: InstanceType.ParamFactory2<T, P1, P2>) {
    fun get(param1: P1, param2: P2): T = factory.build(param1, param2)
}

@JvmInline
value class FactoryParam3Instance<T, P1, P2, P3>(private val factory: InstanceType.ParamFactory3<T, P1, P2, P3>) {
    fun get(param1: P1, param2: P2, param3: P3): T = factory.build(param1, param2, param3)
}

@JvmInline
value class FactoryParam4Instance<T, P1, P2, P3, P4>(private val factory: InstanceType.ParamFactory4<T, P1, P2, P3, P4>) {
    fun get(param1: P1, param2: P2, param3: P3, param4: P4): T = factory.build(param1, param2, param3, param4)
}

sealed interface InstanceType<T> {

    /**
     * Invoked once by default when scope started
     */
    suspend fun init() {}

    /**
     * Invoked if the scope is marked to be force inited (so not lazy)
     */
    suspend fun forceInit() {}

    /**
     * Invoked when the object is disposed (on scope close/stop)
     */
    suspend fun dispose(join: Boolean = false) {}

    @FunctionalInterface
    fun interface Factory<T> : InstanceType<T> {
        fun build(): T
    }

    @FunctionalInterface
    interface ParamFactory<T> : InstanceType<T> {
        fun build(vararg params: Any): T

        //override fun get(vararg params: Any): T = build(params)

        class Params {
            var parameters: Array<out Any> = arrayOf()
                private set

            fun params(vararg parameters: Any) {
                this.parameters = parameters
            }
        }
    }

    @FunctionalInterface
    fun interface ParamFactory1<T, TParam1> : InstanceType<T> {
        fun build(param: TParam1): T
    }

    @FunctionalInterface
    fun interface ParamFactory2<T, TParam1, TParam2> : InstanceType<T> {
        fun build(param1: TParam1, param2: TParam2): T
    }

    @FunctionalInterface
    fun interface ParamFactory3<T, TParam1, TParam2, TParam3> : InstanceType<T> {
        fun build(param1: TParam1, param2: TParam2, param3: TParam3): T
    }

    @FunctionalInterface
    fun interface ParamFactory4<T, TParam1, TParam2, TParam3, TParam4> : InstanceType<T> {
        fun build(param1: TParam1, param2: TParam2, param3: TParam3, param4: TParam4): T
    }

    class Singleton<T>(private val scope: KSimpleDiScope,
                       private val factory: Factory<T>,
                       private val eager: Boolean) :
        InstanceType<T>, Instance<T> {
        private val created = AtomicBoolean(false)
        val instance: T by lazy {
            scope.trackDispose(this)
            val obj = factory.build()
            created.set(true)
            obj
        }

        override fun get() = instance

        override suspend fun init() {
            if (eager) {
                instance
            }
        }

        override suspend fun forceInit() {
            instance
        }

        override suspend fun dispose(join: Boolean) {
            if (created.get()) {
                val obj = instance
                if (obj is AutoCloseable) {
                    obj.close()
                }
            }
        }
    }

    class Service<T>(
        private val scope: KSimpleDiScope,
        private val factory: Factory<T>,
        private val startScope: CoroutineScope,
        val eager: Boolean,
        val startAsync: Boolean,
        val startFun: (suspend (T) -> Unit)?,
        val stopFun: (suspend (T) -> Unit)?,
    ) : InstanceType<T>, Instance<T> {

        private val started = AtomicBoolean(false)
        private var job: Job? = null
        val instance: T by lazy {
            scope.trackDispose(this)
            val obj = factory.build()
            val runner = getRunner(obj)
            if (runner != null) {
                if (startAsync) {
                    job = startScope.launch {
                        runner(obj)
                    }
                } else {
                    started.set(true)
                }
            }
            started.set(true)
            obj
        }

        override fun get() = instance

        private fun getRunner(obj: T): (suspend (T) -> Unit)? {
            if (startFun != null) {
                return startFun
            }
            if (obj is Runnable) {
                return { obj.run() }
            }
            return null
        }

        override suspend fun init() {
            if (eager) {
                instance
            }
        }

        override suspend fun forceInit() {
            instance
        }

        override suspend fun dispose(join: Boolean) {
            if (!started.get()) {
                return
            }
            started.set(false)
            val obj = instance
            val j = job

            safeInvoke { stopFun?.invoke(obj) }
            if (obj is AutoCloseable) {
                safeInvoke { obj.close() }
            }

            if (j != null) {
                j.cancel()
                if (join) {
                    j.join()
                }
            }
        }

        private inline fun safeInvoke(block:()->Unit) : Exception?{
            return try {
                block()
                null
            } catch(e:Exception){
                e
            }
        }

    }
}

//@PublishedApi
sealed class KSimpleDiStorage(
    val couroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),

) : AutoCloseable {

    val instances = mutableMapOf<KClass<*>, InstanceType<*>>()

    @PublishedApi
    internal inline fun <reified T : Any> instance(factory: InstanceType<T>) {
        check(instances[T::class] == null) {
            "Definition for ${T::class} already added."
        }
        instances[T::class] = factory
    }

    inline fun <reified T : Any> getInstanceWith(vararg parameters:Any): T {
        return getInstance { params(*parameters) }
    }

    inline fun <reified T : Any> getInstance(noinline parameters: (InstanceType.ParamFactory.Params.() -> Unit)? = null): T {
        return getOrRun(parameters) {
            error("No factory provided for class: ${T::class.java}")
        }
    }

    inline fun <reified T : Any> getOr(
        noinline parameters: (InstanceType.ParamFactory.Params.() -> Unit)? = null,
        crossinline factory: () -> InstanceType<T>,
    ): T {
        return getOrRun(parameters) {
            instance(factory())
            getInstance(parameters)
        }
    }

    inline fun <reified T : Any> getOrRun(
        noinline parameters: (InstanceType.ParamFactory.Params.() -> Unit)? = null,
        noinline block: (KClass<T>) -> T,
    ): T {
        return getOrRun(T::class, parameters, block)
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T : Any> getOrRun(
        type:KClass<T>,
        parameters: (InstanceType.ParamFactory.Params.() -> Unit)? = null,
        block: (KClass<T>) -> T,
    ): T {
        return when (val factory = instances[type]) {
            is InstanceType.Singleton -> factory.instance as T
            is InstanceType.Service -> factory.instance as T
            is InstanceType.Factory -> factory.build() as T
            is InstanceType.ParamFactory -> {
                val factoryParams = InstanceType.ParamFactory.Params().apply(requireNotNull(parameters)).parameters
                factory.build(*factoryParams) as T
            }
            is InstanceType.ParamFactory1<*, *> -> {
                val factoryParams = InstanceType.ParamFactory.Params().apply(requireNotNull(parameters)).parameters
                ensureSize(factoryParams,1)
                (factory as InstanceType.ParamFactory1<T, Any>).build(
                    factoryParams[0],
                )
            }
            is InstanceType.ParamFactory2<*, *, *> -> {
                val factoryParams = InstanceType.ParamFactory.Params().apply(requireNotNull(parameters)).parameters
                ensureSize(factoryParams,2)
                (factory as InstanceType.ParamFactory2<T, Any, Any>).build(
                    factoryParams[0],
                    factoryParams[1],
                )
            }
            is InstanceType.ParamFactory3<*, *, *, *> -> {
                val factoryParams = InstanceType.ParamFactory.Params().apply(requireNotNull(parameters)).parameters
                ensureSize(factoryParams,3)
                (factory as InstanceType.ParamFactory3<T, Any, Any, Any>).build(
                    factoryParams[0],
                    factoryParams[1],
                    factoryParams[2],
                )
            }
            is InstanceType.ParamFactory4<*, *, *, *, *> -> {
                val factoryParams = InstanceType.ParamFactory.Params().apply(requireNotNull(parameters)).parameters
                ensureSize(factoryParams,4)
                (factory as InstanceType.ParamFactory4<T, Any, Any, Any, Any>).build(
                    factoryParams[0],
                    factoryParams[1],
                    factoryParams[2],
                    factoryParams[4],
                )
            }
            //TODO: look in child scopes?
            null -> block(type)
        }
    }
    private fun ensureSize(params:Array<out Any>, size:Int){
        if(params.size != size){
            throw IllegalArgumentException("Expected $size parameter${if(size < 1) "s" else ""}, but got ${params.size}")
        }
    }
}

private enum class Phase {
    BeforeStart,
    AfterStart,
    BeforeStop,
    AfterStop
}

open class KSimpleDiScope() : KSimpleDiStorage(), AutoCloseable {

    //TODO: track order of init, so we can close in reverse order!
    private val factoriesInInitOrder = mutableListOf<InstanceType<*>>()
    private val childScopes = mutableListOf<ChildScopeInstance<KSimpleDiScope>>()
    private val phasesFunctions = mutableMapOf<Phase, MutableList<suspend () -> Unit>>()

    /**
     * Add a function to run before scope start
     */
    fun beforeStart(f: suspend () -> Unit) {
        on(Phase.BeforeStart, f)
    }

    /**
     * Add a function to run after scope start
     */
    fun afterStart(f: suspend () -> Unit) {
        on(Phase.AfterStart, f)
    }

    /**
     * Add a function to run before scope stop
     */
    fun beforeStop(f: suspend () -> Unit) {
        on(Phase.BeforeStop, f)
    }

    /**
     * Add a function to run after scope stop
     */
    fun afterStop(f: suspend () -> Unit) {
        on(Phase.BeforeStop, f)
    }

    /**
     * Register a function to run for the given phase
     */
    private fun on(phase: Phase, f: suspend () -> Unit) {
        phasesFunctions.getOrPut(phase) { mutableListOf() }.add(f)
    }

    /**
     * Run all the functions registered for the given phase
     */
    private suspend fun invokePhaseFuncs(phase: Phase) {
        phasesFunctions[phase]?.forEach { it() }
        invokeChildScopes { it.invokePhaseFuncs(phase) }
    }

    /**
     * Register the given factory to be disposed when the scope is stopped
     */
    internal fun trackDispose(factory: InstanceType<*>) {
        synchronized(factoriesInInitOrder) {
            factoriesInInitOrder.add(factory)
        }
    }

    /**
     * Register a child scope to participate in this scopes lifecycle phases, and potentially object lookups
     */
    fun <TScope : KSimpleDiScope> childScope(scopeFactory: () -> TScope): ChildScopeInstance<TScope> {
        val instance = ChildScopeInstance(scopeFactory)
        childScopes.add(instance as ChildScopeInstance<KSimpleDiScope>)
        return instance
    }

    class ChildScopeInstance<TScope : KSimpleDiScope>(val scopeFactory: () -> TScope) {

        val scope:TScope by lazy {
            scopeFactory()
        }

        inline fun <T> get(getter: TScope.() -> T): T {
            return getter.invoke(scope)
        }

        fun <T> get(): TScope {
            return scope
        }
    }

    private fun getDisposables(): Collection<InstanceType<*>> {
        synchronized(factoriesInInitOrder) {
            return factoriesInInitOrder.reversed().copyOf()
        }
    }

    inline fun <T> get(instance: () -> Instance<T>): T {
        return instance().get()
    }

    inline fun <reified T : Any> factory(factory: InstanceType.Factory<T>) : FactoryInstance<T> {
        instance(factory)
        return FactoryInstance(factory)
    }

    inline fun <reified T : Any> factoryWithParams(factory: InstanceType.ParamFactory<T>): FactoryParamsInstance<T> {
        instance(factory)
        return FactoryParamsInstance(factory)
    }

    inline fun <reified T : Any, P1> factoryWithParams(factory: InstanceType.ParamFactory1<T, P1>): FactoryParam1Instance<T, P1> {
        instance(factory)
        return FactoryParam1Instance(factory)
    }

    inline fun <reified T : Any, P1, P2> factoryWithParams(factory: InstanceType.ParamFactory2<T, P1, P2>): FactoryParams2Instance<T, P1, P2> {
        instance(factory)
        return FactoryParams2Instance(factory)
    }

    inline fun <reified T : Any, P1, P2, P3> factoryWithParams(factory: InstanceType.ParamFactory3<T, P1, P2, P3>): FactoryParam3Instance<T, P1, P2, P3> {
        instance(factory)
        return FactoryParam3Instance(factory)
    }

    inline fun <reified T : Any, P1, P2, P3, P4> factoryWithParams(factory: InstanceType.ParamFactory4<T, P1, P2, P3, P4>): FactoryParam4Instance<T, P1, P2, P3, P4> {
        instance(factory)
        return FactoryParam4Instance(factory)
    }

    inline fun <reified T : Any> singleton(
        eager: Boolean = false,
        factory: InstanceType.Factory<T>,
    ): Instance<T> {
        val instance = InstanceType.Singleton(this, factory, eager)
        instance(instance)
        return instance
    }

    /**
     * A singleton which can be started/stopped
     */
    inline fun <reified T : Any> service(
        //if true, then start the service on scope start
        eager: Boolean = true,
        //if true, run the service start function in an async manner
        startAsync: Boolean = true,
        //the scope to run the service 'start' in. If null, uses the scopes default
        startScope: CoroutineScope? = null,
        // the function to start the service. Null means look for the defaults (e.g. Runnable)
        noinline start: (suspend (T) -> Unit)? = null,
        // the function to stop the service. Null means look for the defaults (e.g. Closable/Autoclosable)
        noinline stop: (suspend (T) -> Unit)? = null,
        // the factory to create the service instance
        factory: InstanceType.Factory<T>,
    ): Instance<T> {
        val instance = InstanceType.Service(
            this,
            factory,
            startScope = startScope ?: this.couroutineScope,
            eager = eager,
            startAsync = startAsync,
            startFun = start,
            stopFun = stop
        )
        instance(instance)
        return instance
    }

    /**
     * Synonym for stop
     */
    override fun close() {
        couroutineScope.launch {
            stop()
        }
    }

    /**
     * Stop this scope (and it's children)
     */
    suspend fun stop(join: Boolean = false, waitFor: Duration? = null) {
        invokePhaseFuncs(Phase.BeforeStop)

        for (factory in getDisposables()) {
            factory.dispose(join = join)
        }
        invokeChildScopes { it.close() }
        invokePhaseFuncs(Phase.AfterStop)
    }

    fun startAsync(initSingletons: Boolean = false, initServices: Boolean = true) {
        CoroutineScope(Dispatchers.Default).launch {
            start(initSingletons = initSingletons, initServices = initServices)
        }
    }

    /**
     * Start required services. If this is not called, then instances are only generated via calls to 'get'
     */
    suspend fun start(initSingletons: Boolean = false, initServices: Boolean = true) {
        invokePhaseFuncs(Phase.BeforeStart)

        initAll()
        if (initSingletons) {
            forceInitSingletons()
        }
        if (initServices) {
            forceInitServices()
        }

        invokePhaseFuncs(Phase.AfterStart)
    }

    private suspend fun initAll() {
        instances.forEach { it.value.init() }
        invokeChildScopes { it.initAll() }
    }

    private suspend fun forceInitSingletons() {
        instances.forEach {
            val factory = it.value
            if (factory is InstanceType.Singleton) {
                factory.forceInit()
            }
        }
        invokeChildScopes { it.forceInitSingletons() }
    }

    private suspend fun forceInitServices() {
        instances.forEach {
            val factory = it.value
            if (factory is InstanceType.Service) {
                factory.forceInit()
            }
        }
        invokeChildScopes { it.forceInitServices() }
    }

    private suspend fun invokeChildScopes(block: suspend (KSimpleDiScope) -> Unit) {
        for (child in childScopes) {
            block(child.scopeFactory.invoke())
        }
    }
}

private fun <T> List<T>.copyOf(): List<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}
