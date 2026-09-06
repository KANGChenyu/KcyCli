package com.paicli.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainConfigBootstrapTest {

    @Test
    void createsDefaultChromeDevtoolsMcpConfigWhenMissing(@TempDir Path tempHome) throws Exception {
        Main.McpConfigBootstrapResult result = Main.ensureDefaultMcpConfig(tempHome);

        Path config = tempHome.resolve(".paicli").resolve("mcp.json");
        assertTrue(result.created());
        assertTrue(Files.exists(config));
        String content = Files.readString(config);
        assertTrue(content.contains("\"chrome-devtools\""));
        assertTrue(content.contains("chrome-devtools-mcp@latest"));
        assertTrue(content.contains("--isolated=true"));
    }

    @Test
    void defaultChromeDevtoolsMcpConfigUsesNpxCmdOnWindows() {
        String oldOs = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 11");

            String content = Main.defaultChromeDevtoolsMcpJson();

            assertTrue(content.contains("\"command\": \"npx.cmd\""));
        } finally {
            restoreProperty("os.name", oldOs);
        }
    }

    @Test
    void defaultChromeDevtoolsMcpConfigUsesNpxOnNonWindows() {
        String oldOs = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Linux");

            String content = Main.defaultChromeDevtoolsMcpJson();

            assertTrue(content.contains("\"command\": \"npx\""));
        } finally {
            restoreProperty("os.name", oldOs);
        }
    }

    @Test
    void doesNotOverwriteExistingUserConfig(@TempDir Path tempHome) throws Exception {
        Path config = tempHome.resolve(".paicli").resolve("mcp.json");
        Files.createDirectories(config.getParent());
        String original = """
                {
                  "mcpServers": {
                    "filesystem": {
                      "command": "npx",
                      "args": ["-y", "@modelcontextprotocol/server-filesystem"]
                    }
                  }
                }
                """;
        Files.writeString(config, original);

        Main.McpConfigBootstrapResult result = Main.ensureDefaultMcpConfig(tempHome);

        assertFalse(result.created());
        assertEquals(original, Files.readString(config));
        assertTrue(result.message().contains("未配置 chrome-devtools"));
    }
    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
