package org.start2do.plugin.manager;

import java.nio.file.Path;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PropertiesPluginDescriptorFinder;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public class PropertiesOnlyJarPluginManager extends JarPluginManager {

    private final AutowireCapableBeanFactory beanFactory;

    public PropertiesOnlyJarPluginManager(Path pluginsRoot, AutowireCapableBeanFactory beanFactory) {
        super(pluginsRoot);
        this.beanFactory = beanFactory;
        this.extensionFactory = new SpringLifecycleExtensionFactory(this, beanFactory);
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new CompoundPluginDescriptorFinder()
            // 如果你完全不想理 manifest，就只加 Properties
            .add(new PropertiesPluginDescriptorFinder());
    }

}
