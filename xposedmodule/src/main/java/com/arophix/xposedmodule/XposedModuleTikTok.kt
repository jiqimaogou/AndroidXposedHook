package com.arophix.xposedmodule

import android.app.AndroidAppHelper
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Created by shizhen on 06/10/18. */
class XposedModuleTikTok : IXposedHookLoadPackage {
    val startTime = System.currentTimeMillis()

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        XposedBridge.log("Arophix XposedModule handleLoadPackage: " + lpparam.packageName)

        // The `applicationId` configured inside `app/build.gradle`
        if (
            lpparam.packageName != "com.zhiliaoapp.musically" &&
                lpparam.packageName != "com.arophix.xposedhooking"
        ) {
            XposedBridge.log("1 findAndHookMethod for " + lpparam.packageName)
            return
        }
        XposedBridge.log("2 findAndHookMethod for " + lpparam.packageName)
        //        findAndHookConstructor(Any::class.java, 10 * 10000) { it.thisObject.javaClass.name
        // }
        val stackTraceStringKey: (MethodHookParam) -> String = {
            Log.getStackTraceString(Throwable())
        }
        val callerStackTraceStringKey: (MethodHookParam) -> String =
            fun(it: MethodHookParam): String {
                val stackTrace = Throwable().stackTrace
                for (i in 5 until stackTrace.size) {
                    val stackTraceElement = stackTrace[i]
                    val clazz =
                        try {
                            XposedHelpers.findClass(
                                stackTraceElement.className,
                                lpparam.classLoader
                            )
                        } catch (e: XposedHelpers.ClassNotFoundError) {
                            return stackTraceElement.toString()
                        }
                    if (clazz.classLoader == lpparam.classLoader) {
                        return stackTraceElement.toString()
                    }
                }
                return stackTrace.getOrNull(5).toString()
            }
        findAndHookConstructor(StringBuilder::class.java, 5 * 10000, callerStackTraceStringKey)
        //        findAndHookConstructor(
        //            "java.util.ArrayList\$Itr",
        //            lpparam.classLoader,
        //            3 * 10000,
        //            callerCallerStackTraceStringKey
        //        )
        //        findAndHookConstructor(Matcher::class.java, 1 * 10000, stackTraceStringKey)
        //        findAndHookConstructor(ArrayList::class.java, 5 * 10000, stackTraceStringKey)
        //        findAndHookConstructor(java.lang.Float::class.java, 2 * 10000,
        // stackTraceStringKey)
        //        findAndHookConstructor(LinkedHashMap::class.java, 2 * 10000, stackTraceStringKey)
        //        findAndHookConstructor(HashSet::class.java, 2 * 10000, stackTraceStringKey)
        //        findAndHookConstructor(HashMap::class.java, 2 * 10000, stackTraceStringKey)
        //        findAndHookConstructor(JSONObject::class.java, 2 * 10000, stackTraceStringKey)
        //        findAndHookConstructor(java.lang.Long::class.java, 2 * 10000, stackTraceStringKey)
        //        findAndHookConstructor(File::class.java, 2 * 1000, stackTraceStringKey)
        //        findAndHookMethod(View::class.java, "requestLayout", 1 * 10000,
        // stackTraceStringKey)
        //        findAndHookConstructor(Thread::class.java, 10, stackTraceStringKey)
        //        findAndHookMethod(
        //            "android.os.BinderProxy",
        //            lpparam.classLoader,
        //            "transact",
        //            3 * 100,
        //            stackTraceStringKey
        //        )

        //        findAndHookConstructor(Pattern::class.java, 1 * 1000, stackTraceStringKey)
        //        findAndHookConstructor(Thread::class.java, 1 * 100, stackTraceStringKey)

        //        findAndHookMethod("android.widget.TextView", lpparam.classLoader, "setText",
        // "java.lang.String", new XC_MethodHook() {
        //            @Override
        //            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws
        // Throwable {
        //                // this will be called before the clock was updated by the original method
        //                XposedBridge.log("Before ===>>> android.widget.TextView$setText is hooked:
        // " + param.getClass().getSimpleName());
        //                XposedBridge.log("Before ===>>> param.args[0]:  " + param.args[0]);
        //                param.args[0] = "I am from Xposedmodule :) ";
        //            }
        //            @Override
        //            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        //                XposedBridge.log("After ===>>>  android.widget.TextView$setText is hooked:
        // " + param.getClass().getSimpleName());
        //                XposedBridge.log("After ===>>>  param.args[0]:  " + param.args[0]);
        //            }
        //        });
        XposedBridge.log("3 findAndHookMethod for " + lpparam.packageName)
    }

    private fun findAndHookConstructor(
        className: String,
        classLoader: ClassLoader?,
        writeThreshold: Int,
        key: (MethodHookParam) -> String
    ) = findAndHookConstructor(XposedHelpers.findClass(className, classLoader), writeThreshold, key)

    private fun findAndHookConstructor(
        clazz: Class<*>,
        writeThreshold: Int,
        key: (MethodHookParam) -> String
    ) = findAndHookConstructor(clazz, getCallback(clazz, writeThreshold, key))

    private fun findAndHookMethod(
        className: String,
        classLoader: ClassLoader?,
        methodName: String,
        writeThreshold: Int,
        key: (MethodHookParam) -> String
    ) =
        findAndHookMethod(
            XposedHelpers.findClass(className, classLoader),
            methodName,
            writeThreshold,
            key
        )

    private fun findAndHookMethod(
        clazz: Class<*>,
        methodName: String,
        writeThreshold: Int,
        key: (MethodHookParam) -> String
    ) = findAndHookMethod(clazz, methodName, getCallback(clazz, writeThreshold, key))

    private fun getCallback(
        clazz: Class<*>,
        writeThreshold: Int,
        key: (MethodHookParam) -> String
    ) =
        object : XC_MethodHook() {
            val counts = ConcurrentHashMap<String, Pair<MutableSet<String>, AtomicInteger>>()

            val allCount = AtomicInteger()

            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                // this will be called before the clock was updated by the original method
            }

            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                counts
                    .computeIfAbsent(key(param)) {
                        Pair(ConcurrentHashMap.newKeySet(), AtomicInteger())
                    }
                    .let { (threads, count) ->
                        threads.add(
                            Thread.currentThread().let { thread ->
                                "$thread@${Integer.toHexString(thread.hashCode())}"
                            }
                        )
                        count.incrementAndGet()
                    }
                if (allCount.incrementAndGet() % writeThreshold == 0) {
                    File(
                            AndroidAppHelper.currentApplication().externalCacheDir,
                            "${clazz.name}#${param.method.name}.log"
                        )
                        .also { XposedBridge.log(it.absolutePath) }
                        .printWriter()
                        .use { writer ->
                            val currentTime = System.currentTimeMillis()
                            val elapsed = currentTime - startTime
                            writer.println(elapsed)
                            writer.println(counts.size)
                            writer.println("===============================================")
                            counts
                                .toList()
                                .sortedByDescending { (_, value) ->
                                    val (_, count) = value
                                    count.get()
                                }
                                .forEachIndexed { index, (key, value) ->
                                    val (threads, count) = value
                                    writer.println(index)
                                    writer.println(count)
                                    writer.println(threads)
                                    writer.println("----------------------------------------------")
                                    if (key.last() == '\n') {
                                        writer.print(key)
                                    } else {
                                        writer.println(key)
                                    }
                                    writer.println(
                                        "==============================================="
                                    )
                                }
                        }
                }
            }
        }

    companion object {
        private fun findAndHookConstructor(clazz: Class<*>, callback: XC_MethodHook) =
            clazz.declaredConstructors.map { constructor ->
                XposedHelpers.findAndHookConstructor(clazz, *constructor.parameterTypes, callback)
            }

        private fun findAndHookMethod(
            clazz: Class<*>,
            methodName: String,
            callback: XC_MethodHook
        ) =
            clazz.declaredMethods
                .filter { method -> method.name == methodName }
                .map { method ->
                    XposedHelpers.findAndHookMethod(
                        clazz,
                        methodName,
                        *method.parameterTypes,
                        callback
                    )
                }
    }
}
