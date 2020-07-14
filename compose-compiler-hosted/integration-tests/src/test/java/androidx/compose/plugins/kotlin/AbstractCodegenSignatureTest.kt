/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.plugins.kotlin

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import org.jetbrains.kotlin.backend.common.output.OutputFile
import org.robolectric.Robolectric
import java.net.URLClassLoader

fun printPublicApi(classDump: String, name: String): String {
    return classDump
        .splitToSequence("\n")
        .filter {
            if (it.contains("INVOKESTATIC kotlin/internal/ir/Intrinsic")) {
                // if instructions like this end up in our generated code, it means something
                // went wrong. Usually it means that it just can't find the function to call,
                // so it transforms it into this intrinsic call instead of failing. If this
                // happens, we want to hard-fail the test as the code is definitely incorrect.
                error(
                    buildString {
                        append("An unresolved call was found in the generated bytecode of '")
                        append(name)
                        append("'")
                        appendLine()
                        appendLine()
                        appendLine("Call was: $it")
                        appendLine()
                        appendLine("Entire class file output:")
                        appendLine(classDump)
                    }
                )
            }
            if (it.startsWith("  ")) {
                if (it.startsWith("   ")) false
                else it[2] != '/' && it[2] != '@'
            } else {
                it == "}" || it.endsWith("{")
            }
        }
        .joinToString(separator = "\n")
        .replace('$', '%') // replace $ to % to make comparing it to kotlin string literals easier
}

abstract class AbstractCodegenSignatureTest : AbstractCodegenTest() {

    private var isSetup = false
    override fun setUp() {
        isSetup = true
        super.setUp()
    }

    private fun <T> ensureSetup(block: () -> T): T {
        if (!isSetup) setUp()
        return block()
    }

    private fun OutputFile.printApi(): String {
        return printPublicApi(asText(), relativePath)
    }

    fun checkApi(src: String, expected: String, dumpClasses: Boolean = false): Unit = ensureSetup {
        val className = "Test_REPLACEME_${uniqueNumber++}"
        val fileName = "$className.kt"

        val loader = classLoader("""
           import androidx.compose.*

           $src
        """, fileName, dumpClasses)

        val apiString = loader
            .allGeneratedFiles
            .filter { it.relativePath.endsWith(".class") }
            .map { it.printApi() }
            .joinToString(separator = "\n")
            .replace(className, "Test")

        val expectedApiString = expected
            .trimIndent()
            .split("\n")
            .filter { it.isNotBlank() }
            .joinToString("\n")

        assertEquals(expectedApiString, apiString)
    }

    fun validateBytecode(
        src: String,
        dumpClasses: Boolean = false,
        validate: (String) -> Unit
    ): Unit = ensureSetup {
        val className = "Test_REPLACEME_${uniqueNumber++}"
        val fileName = "$className.kt"

        val loader = classLoader("""
           @file:OptIn(
             ExperimentalComposeApi::class,
             InternalComposeApi::class,
             ComposeCompilerApi::class
           )
           package test

           import androidx.compose.*

           $src
        """, fileName, dumpClasses)

        val apiString = loader
            .allGeneratedFiles
            .filter { it.relativePath.endsWith(".class") }
            .map {
                it.asText().replace('$', '%').replace(className, "Test")
            }.joinToString("\n")

        validate(apiString)
    }

    fun checkComposerParam(src: String, dumpClasses: Boolean = false): Unit = ensureSetup {
        val className = "Test_REPLACEME_${uniqueNumber++}"
        val compiledClasses = classLoader(
            """
                import androidx.compose.*
                import android.widget.LinearLayout
                import android.content.Context
                import androidx.ui.node.UiApplier

                $src

                @Composable fun assertComposer(expected: Composer<*>?) {
                    val actual = currentComposer
                    assert(expected === actual)
                }

                private var __context: Context? = null

                @OptIn(ExperimentalComposeApi::class)
                fun makeComposer(): Composer<*> {
                    val container = LinearLayout(__context!!)
                    return Composer(
                        SlotTable(),
                        UiApplier(container),
                        Recomposer.current()
                    )
                }

                fun invokeComposable(composer: Composer<*>?, fn: @Composable () -> Unit) {
                    if (composer == null) error("Composer was null")
                    val composition = compositionFor(composer, Recomposer.current()) { a, b ->
                        composer
                    }
                    composition.setContent(fn)
                }

                class Test {
                  fun test(context: Context) {
                    __context = context
                    run()
                    __context = null
                  }
                }
            """,
            fileName = className,
            dumpClasses = dumpClasses
        )

        val allClassFiles = compiledClasses.allGeneratedFiles.filter {
            it.relativePath.endsWith(".class")
        }

        val loader = URLClassLoader(emptyArray(), this.javaClass.classLoader)

        val instanceClass = run {
            var instanceClass: Class<*>? = null
            var loadedOne = false
            for (outFile in allClassFiles) {
                val bytes = outFile.asByteArray()
                val loadedClass = loadClass(loader, null, bytes)
                if (loadedClass.name == "Test") instanceClass = loadedClass
                loadedOne = true
            }
            if (!loadedOne) error("No classes loaded")
            instanceClass ?: error("Could not find class $className in loaded classes")
        }

        val instanceOfClass = instanceClass.newInstance()
        val testMethod = instanceClass.getMethod("test", Context::class.java)

        val controller = Robolectric.buildActivity(TestActivity::class.java)
        val activity = controller.create().get()
        testMethod.invoke(instanceOfClass, activity)
    }

    private class TestActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(LinearLayout(this))
        }
    }

    fun codegen(text: String, dumpClasses: Boolean = false): Unit = ensureSetup {
        codegenNoImports(
            """
           import android.content.Context
           import android.widget.*
           import androidx.compose.*

           $text

        """, dumpClasses)
    }

    fun codegenNoImports(text: String, dumpClasses: Boolean = false): Unit = ensureSetup {
        val className = "Test_${uniqueNumber++}"
        val fileName = "$className.kt"

        classLoader(text, fileName, dumpClasses)
    }
}