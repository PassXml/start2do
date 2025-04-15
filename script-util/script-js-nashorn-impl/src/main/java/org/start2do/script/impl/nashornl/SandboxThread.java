package org.start2do.script.impl.nashornl;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SandboxThread implements Runnable {

    /**
     * 沙箱线程监控器
     */
    private final SandboxThreadMonitor sandboxThreadMonitor;

    private final ScriptEngine scriptEngine;

    private final String script;
    private final CompiledScript compiledScript;

    private Exception exception;

    private Object result;
    private final Bindings bindings;

    public SandboxThread(ScriptEngine scriptEngine, String script, Bindings bindings, final long maxCPUTime,
        final long maxMemory) {
        this.scriptEngine = scriptEngine;
        this.script = script;
        this.compiledScript = null;
        this.bindings = bindings;
        this.sandboxThreadMonitor = new SandboxThreadMonitor(maxCPUTime, maxMemory);
    }

    public SandboxThread(ScriptEngine scriptEngine, CompiledScript script, String scriptText, Bindings bindings,
        final long maxCPUTime,
        final long maxMemory) {
        this.scriptEngine = scriptEngine;
        this.script = scriptText;
        this.compiledScript = script;
        this.bindings = bindings;
        this.sandboxThreadMonitor = new SandboxThreadMonitor(maxCPUTime, maxMemory);
    }

    @Override
    public void run() {
        this.sandboxThreadMonitor.setThreadToMonitor(Thread.currentThread());
        try {
            if (compiledScript != null) {
                result = compiledScript.eval(bindings);
            } else {
                result = scriptEngine.eval(script, bindings);
            }
        } catch (Exception e) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            this.exception = e;
        } finally {
            this.sandboxThreadMonitor.stop();
        }
    }

    /**
     * 启动监控
     */
    public void monitoring() throws InterruptedException {
        this.sandboxThreadMonitor.run();
    }

    public boolean isCpuTimeExceeded() {
        return this.sandboxThreadMonitor.isCpuTimeExceeded();
    }

    public boolean isMemoryExceeded() {
        return this.sandboxThreadMonitor.isMemoryExceeded();
    }

    public final long getCpuRuntime() {
        return this.sandboxThreadMonitor.getCpuRuntime();
    }

    public final long getAllocatedMemory() {
        return this.sandboxThreadMonitor.getAllocatedMemory();
    }

    public Exception getException() {
        return exception;
    }

    public Object getResult() {
        return result;
    }
}
