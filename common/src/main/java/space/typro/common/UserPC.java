package space.typro.common;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@Slf4j
@ToString
public class UserPC {
    /**
     * Физическая память ПК в мегабайтах
     */
    private static final int RAM = getTotalPhysicalMemory();

    public static final OS USER_OS = getPlatform();
    public static final String JAVA_BIT = System.getProperty("sun.arch.data.model");
    public static final int MONITOR_WIDTH = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode()
            .getWidth();
    public static final int MONITOR_HEIGHT = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode()
            .getHeight();

    private static OS getPlatform(){
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OS.WINDOWS;
        else if (os.contains("linux") || os.contains("unix")) return OS.LINUX;
        else if (os.contains("mac")) return OS.MACOS;  // исправлено с macos на mac
        else return OS.UNKNOWN;
    }

    /**
     * Получает общую физическую память через reflection (кросс-платформенно)
     */
    private static int getTotalPhysicalMemory() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

            // Пробуем получить память через com.sun.management если доступно
            Class<?> clazz = Class.forName("com.sun.management.OperatingSystemMXBean");
            if (clazz.isInstance(osBean)) {
                java.lang.reflect.Method method = clazz.getMethod("getTotalPhysicalMemorySize");
                long totalMemory = (Long) method.invoke(osBean);
                return (int) (totalMemory / 1048576); // байты -> мегабайты
            }
        } catch (Exception e) {
            log.warn("Failed to get physical memory size: {}", e.getMessage());
        }

        // Fallback: возвращаем память, доступную JVM
        return (int) (Runtime.getRuntime().maxMemory() / 1048576);
    }

    /**
     * Возвращает доступное для java кол-во ОЗУ в мегабайтах
     */
    public static int getAvailableRam(){
        if (JAVA_BIT != null && JAVA_BIT.equals("32")) return 1536;
        return RAM / 2;
    }

    public enum OS {
        WINDOWS, LINUX, MACOS, UNKNOWN
    }
}