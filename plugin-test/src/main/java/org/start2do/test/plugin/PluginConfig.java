package org.start2do.test.plugin;

import java.util.Collection;
import java.util.List;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.start2do.plugin.api.spring.SpringMvcControllerExtension;
import org.start2do.plugin.api.spring.SpringPluginBeansExtension;
import org.start2do.test.plugin.controller.TestController;
import org.start2do.test.plugin.service.TestService;

@Extension
public class PluginConfig extends Plugin implements SpringMvcControllerExtension, SpringPluginBeansExtension {

    @Override
    public Collection<Class<?>> getControllerClasses() {
        return List.of(TestController.class);
    }

    @Override
    public Collection<Class<?>> getBeanClasses() {
        return List.of(TestService.class);
    }
}
