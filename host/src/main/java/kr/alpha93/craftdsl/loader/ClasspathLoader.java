package kr.alpha93.craftdsl.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import kr.alpha93.ph.paper.plugin.SimpleLibraryResolver;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public final class ClasspathLoader implements PluginLoader {

    @Override
    public void classloader(final PluginClasspathBuilder builder) {
        final SimpleLibraryResolver mvn = new SimpleLibraryResolver()
                .mavenCentral()

                .kotlin("stdlib")
                .kotlin("reflect")
                .kotlin("scripting-common")
                .kotlin("scripting-jvm")
                .kotlin("scripting-jvm-host")
                .kotlin("scripting-dependencies")
                .kotlin("scripting-dependencies-maven")

                .kotlinx("coroutines-core", "1.10.1")
                .kotlinx("datetime", "0.6.2");

        builder.addLibrary(mvn);
    }

}
