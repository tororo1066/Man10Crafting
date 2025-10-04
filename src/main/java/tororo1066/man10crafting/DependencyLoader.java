package tororo1066.man10crafting;

import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import tororo1066.tororopluginapi.AbstractDependencyLoader;
import tororo1066.tororopluginapi.Library;
import tororo1066.tororopluginapi.LibraryType;

public class DependencyLoader extends AbstractDependencyLoader {
    public DependencyLoader() {}

    @Override
    public Library[] getDependencies() {
        return new Library[]{
                LibraryType.KOTLIN.createLibrary("2.2.0"),
                new Library(
                        "org.jetbrains.kotlin:kotlin-reflect",
                        "2.2.0",
                        MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR,
                        "compile"
                )
        };
    }
}
