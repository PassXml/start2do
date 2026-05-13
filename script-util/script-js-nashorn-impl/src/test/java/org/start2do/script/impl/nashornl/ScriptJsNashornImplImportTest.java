package org.start2do.script.impl.nashornl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 ScriptJsNashornImpl 的 import 解析逻辑。
 * <p>
 * 测试聚焦在 import 解析的核心逻辑（模式匹配、库注册、错误处理）上，
 * 不依赖实际 Nashorn 脚本引擎执行。
 */
class ScriptJsNashornImplImportTest {

    private ScriptJsNashornImpl runner;

    @BeforeEach
    void setUp() {
        runner = new ScriptJsNashornImpl();
        runner.registerLibrary("utils",
            "function add(a, b) { return a + b; }\n" +
            "function multiply(a, b) { return a * b; }\n" +
            "function greet(name) { return 'Hello, ' + name; }");
        runner.registerLibrary("math",
            "function factorial(n) { return n <= 1 ? 1 : n * factorial(n - 1); }\n" +
            "function square(x) { return x * x; }");
    }

    // ========== import('name') 语法测试 ==========

    @Test
    void resolveImport_shouldReplaceImportWithLibrarySource() {
        String result = runner.resolveImports("import('utils'); var x = add(1, 2);");
        assertFalse(result.contains("import("));
        assertTrue(result.contains("function add"));
        assertTrue(result.contains("var x = add(1, 2);"));
    }

    @Test
    void resolveImport_shouldSupportDoubleQuotes() {
        String result = runner.resolveImports("import(\"math\"); square(9);");
        assertFalse(result.contains("import("));
        assertTrue(result.contains("function square"));
    }

    @Test
    void resolveImport_shouldSupportNoSemicolon() {
        String result = runner.resolveImports("import('utils')\nadd(1, 2);");
        assertFalse(result.contains("import("));
        assertTrue(result.contains("function add"));
    }

    @Test
    void resolveImport_shouldSupportMultipleImports() {
        String result = runner.resolveImports(
            "import('utils');\n" +
            "import('math');\n" +
            "add(1, 2) + square(3);");
        assertTrue(result.contains("function add"));
        assertTrue(result.contains("function square"));
        assertTrue(result.contains("add(1, 2) + square(3);"));
        assertFalse(result.contains("import("));
    }

    @Test
    void resolveImport_shouldHandleScriptWithoutImport() {
        String original = "var x = 1 + 1;";
        String result = runner.resolveImports(original);
        assertEquals(original, result);
    }

    @Test
    void resolveImport_shouldHandleEmptyOrNull() {
        assertNull(runner.resolveImports(null));
        assertEquals("", runner.resolveImports(""));
    }

    @Test
    void resolveImport_shouldThrowOnMissingLibrary() {
        assertThrows(IllegalArgumentException.class,
            () -> runner.resolveImports("import('nonexistent'); 1;"));
    }

    @Test
    void resolveImport_shouldDetectCircularImports() {
        runner.registerLibrary("a", "import('b');");
        runner.registerLibrary("b", "import('a');");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> runner.resolveImports("import('a');"));
        assertTrue(ex.getMessage().contains("Circular import"));
    }

    // ========== import {} from 语法测试 ==========

    @Test
    void resolveImport_fromSyntax() {
        String result = runner.resolveImports("import { add, greet } from \"utils\"; add(1, 2);");
        assertTrue(result.contains("function add"));
        assertTrue(result.contains("function multiply"));
        assertFalse(result.contains("import("));
    }

    @Test
    void resolveImport_fromSyntaxMultipleLibraries() {
        String result = runner.resolveImports(
            "import { add } from \"utils\";\n" +
            "import { square } from \"math\";\n" +
            "add(1, 2) + square(3);");
        assertTrue(result.contains("function add"));
        assertTrue(result.contains("function square"));
    }

    @Test
    void resolveImport_mixedSyntax() {
        String result = runner.resolveImports(
            "import('utils');\n" +
            "import { square } from \"math\";\n" +
            "add(1, 2) + square(3);");
        assertTrue(result.contains("function add"));
        assertTrue(result.contains("function square"));
        assertFalse(result.contains("import('utils')"));
        assertFalse(result.contains("import { square } from \"math\""));
    }

    // ========== 库注册测试 ==========

    @Test
    void registerLibrary_shouldStoreLibrary() {
        assertTrue(runner.getLibraryNames().contains("utils"));
        assertTrue(runner.getLibraryNames().contains("math"));
    }

    @Test
    void registerLibrary_shouldRejectEmptyName() {
        assertThrows(IllegalArgumentException.class,
            () -> runner.registerLibrary("", "function f() {}"));
    }

    @Test
    void registerLibraries_shouldStoreAll() {
        java.util.Map<String, String> libs = new java.util.HashMap<>();
        libs.put("a", "function fa() {}");
        libs.put("b", "function fb() {}");
        runner.registerLibraries(libs);
        assertTrue(runner.getLibraryNames().contains("a"));
        assertTrue(runner.getLibraryNames().contains("b"));
    }

    @Test
    void removeLibrary_shouldRemoveIt() {
        runner.registerLibrary("temp", "function tf() {}");
        assertTrue(runner.getLibraryNames().contains("temp"));
        runner.removeLibrary("temp");
        assertFalse(runner.getLibraryNames().contains("temp"));
        assertThrows(IllegalArgumentException.class,
            () -> runner.resolveImports("import('temp');"));
    }

    @Test
    void registerLibraries_shouldHandleNull() {
        runner.registerLibraries(null);
    }

    // ========== 整体流程测试 ==========

    @Test
    void importShouldReplaceCorrectlyAndLeaveCodeIntact() {
        String result = runner.resolveImports(
            "import('utils');\n" +
            "import('math');\n" +
            "var result = add(3, 4) + square(5);");

        assertTrue(result.contains("function add"));
        assertTrue(result.contains("function square"));
        assertTrue(result.contains("function factorial"));
        assertTrue(result.contains("function multiply"));
        assertTrue(result.contains("var result = add(3, 4) + square(5);"));
        assertFalse(result.contains("import('utils')"));
        assertFalse(result.contains("import('math')"));
    }
}
