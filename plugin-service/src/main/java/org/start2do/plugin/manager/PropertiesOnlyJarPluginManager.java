package org.start2do.plugin.manager;

import java.nio.file.Path;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginLoader;
import org.pf4j.PropertiesPluginDescriptorFinder;

public class PropertiesOnlyJarPluginManager extends JarPluginManager {

    public PropertiesOnlyJarPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new CompoundPluginDescriptorFinder()
            // 如果你完全不想理 manifest，就只加 Properties
            .add(new PropertiesPluginDescriptorFinder());
    }

    @Override
    protected PluginLoader createPluginLoader() {
        return new CompoundPluginLoader()
            .add(new DevelopmentPluginLoader(this), this::isDevelopment)
            .add(new SpringBootAwarePluginLoader(this), this::isNotDevelopment);
    }

}
