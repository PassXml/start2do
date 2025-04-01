package org.start2do.script.util;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScriptFormatUtil {

    private static final Pattern ARROW_FUNC_PATTERN = Pattern.compile("^\\s*\\(\\s*args\\s*\\)\\s*=>\\s*\\{.*\\}\\s*$",
        Pattern.DOTALL);

    private static String expandArgsInScript(String script) {
        // 替换 args.xxx 为直接变量访问
        return script.replaceAll("args\\.(\\w+)", "$1");
    }

    public static String formatScript(String script) {
        if (script == null || script.trim().isEmpty()) {
            return script;
        }

        try {
            script = script.trim();
            // 检查基本语法
            if (!isBalancedBraces(script)) {
                throw new IllegalArgumentException("Script has unbalanced braces");
            }

            // 检查是否已经是箭头函数格式
            if (ARROW_FUNC_PATTERN.matcher(script).matches()) {
                return expandArgsInScript(script);
            }

            // 不是箭头函数格式，进行转换
            return wrapToArrowFunction(script);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid script format: " + e.getMessage());
        }
    }

    private static boolean isBalancedBraces(String script) {
        int count = 0;
        for (char c : script.toCharArray()) {
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
            }
            if (count < 0) {
                return false;
            }
        }
        return count == 0;
    }

    private static String wrapToArrowFunction(String script) {
        script = script.trim();

        // 移除可能存在的外层函数包装
        if (script.startsWith("function")) {
            int openBrace = script.indexOf("{");
            int closeBrace = findMatchingBrace(script, openBrace);
            if (openBrace != -1 && closeBrace != -1) {
                script = script.substring(openBrace + 1, closeBrace).trim();
            }
        }

        // 确保脚本不以分号结尾
        if (script.endsWith(";")) {
            script = script.substring(0, script.length() - 1);
        }

        return String.format("((args) => {  const {...variables} = args; with(variables) {%s}})", script);
    }

    private static int findMatchingBrace(String script, int openBraceIndex) {
        if (openBraceIndex == -1) {
            return -1;
        }

        int count = 1;
        for (int i = openBraceIndex + 1; i < script.length(); i++) {
            char c = script.charAt(i);
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
            }

            if (count == 0) {
                return i;
            }
        }
        return -1;
    }

}
